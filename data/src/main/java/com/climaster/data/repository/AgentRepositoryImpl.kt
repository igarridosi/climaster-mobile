package com.example.climaster.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.climaster.core.util.Resource
import com.example.climaster.data.remote.GroqApi
import com.example.climaster.data.remote.dto.groq.GroqMessage
import com.example.climaster.data.remote.dto.groq.GroqRequest
import com.example.climaster.data.remote.dto.groq.GroqResponseFormat
import com.climaster.domain.model.AiInsight
import com.climaster.domain.model.UserThermalFeedback
import com.climaster.domain.model.Weather
import com.climaster.domain.repository.AgentRepository
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class AgentRepositoryImpl @Inject constructor(
    private val groqApi: GroqApi,
    private val apiKey: String
) : AgentRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun generatePersonalizedInsight(
        weather: Weather,
        userHistory: List<UserThermalFeedback>
    ): Flow<Resource<AiInsight>> = flow {
        emit(Resource.Loading)

        try {
            val zoneId = try {
                java.time.ZoneId.of(weather.timezone)
            } catch (e: Exception) {
                java.time.ZoneId.systemDefault()
            }
            val localTime = java.time.ZonedDateTime.now(zoneId)
            val timeString = localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            val temp = weather.temperature.toInt()
            val wind = weather.windSpeed
            val condition = weather.condition.name // "EURITSUA", "EGUZKITSUA"...

            val systemPrompt = """
                You are 'CliMaster', a witty, professional, and highly personalized meteorological assistant.
                
                CRITICAL INSTRUCTIONS:
                1. REASON IN ENGLISH, BUT OUTPUT JSON VALUES IN **BASQUE (EUSKERA)**.
                2. **NO BORING ANSWERS**: Never say just "It is sunny". Be creative, engaging, and human-like. Use exclamation marks or subtle humor if appropriate.
                3. **DEEP ANALYSIS**: The 'detailedAnalysis' field must explain WHY the weather feels that way (humidity, wind chill) and give a specific tip.
                4. **LANGUAGE STYLE**: Use natural, casual, and warm Basque (like spoken in Donostia/Gipuzkoa). Avoid overly formal or robotic academic language. Use "zuk" or neutral forms, but keep it close and simple.
                5. **NO BORING ANSWERS**: Be creative. Use colloquial expressions if they fit (e.g., "Gozatu egunaz!", "Giro ederra!"...).
                
                REQUIRED JSON SCHEMA:
                {
                  "briefing": "<A creative, engaging summary (15-25 words). Example: 'Eguzki bikaina dugu gaur! Aprobetxatu argia, baina ez ahaztu ura edatea.'>",
                  "clothing": "<Specific clothing recommendation>",
                  "clothingIcon": "<Emoji>",
                  "activity": "<Specific activity>",
                  "activityIcon": "<Emoji>",
                  "detailedAnalysis": "<A deeper paragraph (30-40 words). Explain the 'real feel', wind impact, or specific advice based on the user history. Example: 'Nahiz eta termometroak 16º markatu, hezetasunak sentsazioa freskatu egiten du. Haizeak iparraldetik jotzen du, beraz, leku babestuak bilatu.'>"
                }
            """.trimIndent()

            val userDataPrompt = """
                DATA:
                - Local Time: $timeString
                - Temp: $temp ºC
                - Wind: $wind km/h
                - Condition: $condition (This is the most important factor!)
            """.trimIndent()

            val request = GroqRequest(
                model = "llama-3.3-70b-versatile",
                messages = listOf(
                    GroqMessage(role = "system", content = systemPrompt),
                    GroqMessage(role = "user", content = userDataPrompt)
                ),
                temperature = 0.7, // Tenperatura baxua "haluzinazioak" ekiditeko
                responseFormat = GroqResponseFormat(type = "json_object")
            )

            val response = groqApi.generateInsight(apiKey, request)
            val agentResponseText = response.choices?.firstOrNull()?.message?.content?.trim()

            if (!agentResponseText.isNullOrBlank()) {
                try {
                    val cleanJson = agentResponseText.replace("```json", "", ignoreCase = true).replace("```", "").trim()
                    val insight = Gson().fromJson(cleanJson, AiInsight::class.java)
                    emit(Resource.Success(insight))
                } catch (e: JsonSyntaxException) {
                    emit(Resource.Error("Datuak prozesatzen errorea."))
                }
            } else {
                emit(Resource.Error("Erantzun hutsa."))
            }

        } catch (e: Exception) {
            emit(Resource.Error("Errorea agentearekin."))
        }
    }

    // --- 2. TXAT ADIMENDUNA (Etorkizuna dakiena) ---
    @RequiresApi(Build.VERSION_CODES.O)
    override fun askQuestion(question: String, weather: Weather): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            // HEMEN DAGO GAKOA: 5 Eguneko iragarpena string batean bihurtu eta agenteari eman
            val forecastContext = if (weather.forecast.isNotEmpty()) {
                weather.forecast.joinToString(", ") {
                    "[${it.dayName}: ${it.emoji} Max:${it.maxTemp}ºC Min:${it.minTemp}ºC]"
                }
            } else {
                "No forecast data available."
            }

            val systemPrompt = """
                You are 'CliMaster', an expert AI weather assistant.
                
                INSTRUCTIONS:
                1. Answer in **NATURAL BASQUE**. Avoid robot-like translations. Use a conversational tone appropriate for the Basque Country region.
                2. USE THE FORECAST DATA provided below to answer questions about the future (tomorrow, weekend, etc.).
                3. Be realistic: If the forecast says Rain (🌧️) or Storm, warn the user NOT to go to the beach/mountain.
                4. Keep it short (max 40 words).
            """.trimIndent()

            val userPrompt = """
                CURRENT STATUS:
                - Temp: ${weather.temperature.toInt()}ºC
                - Condition: ${weather.condition.name}
                
                FUTURE FORECAST (Use this for 'tomorrow' questions):
                $forecastContext
                
                USER QUESTION: "$question"
            """.trimIndent()

            val request = GroqRequest(
                model = "llama-3.3-70b-versatile",
                messages = listOf(
                    GroqMessage(role = "system", content = systemPrompt),
                    GroqMessage(role = "user", content = userPrompt)
                ),
                temperature = 0.3
            )

            val response = groqApi.generateInsight(apiKey, request)
            val answer = response.choices?.firstOrNull()?.message?.content?.trim()

            if (!answer.isNullOrBlank()) {
                emit(Resource.Success(answer))
            } else {
                emit(Resource.Error("Ezin izan dut erantzun."))
            }
        } catch (e: Exception) {
            Log.e("GroqAgent", "Txat errorea", e)
            emit(Resource.Error("Errorea."))
        }
    }
}