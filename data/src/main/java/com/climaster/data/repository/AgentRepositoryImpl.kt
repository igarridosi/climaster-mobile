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
            // 1. Data Processing
            val time = weather.lastUpdated.format(DateTimeFormatter.ofPattern("HH:mm"))
            val temp = weather.temperature.toInt()
            val wind = weather.windSpeed
            val condition = weather.condition.name

            val historyStr = if (userHistory.isNotEmpty()) {
                "User thermal history at various temperatures: " +
                        userHistory.joinToString(", ") { "${it.actualTemp}ºC -> ${it.sensation.name}" }
            } else {
                "No prior thermal history for this user."
            }

            // 2. System Prompt: The Architect's Rules (English)
            val systemPrompt = """
                You are an expert meteorological and lifestyle assistant.
                Your task is to analyze weather data and user thermal history to provide personalized advice.
                
                CRITICAL INSTRUCTIONS:
                1. REASON IN ENGLISH, BUT ALL TEXT VALUES IN THE JSON MUST BE IN BASQUE (EUSKERA).
                2. You must output ONLY a raw, valid JSON object. 
                3. DO NOT wrap the JSON in markdown blocks (no ```json). 
                4. Do not include greetings, explanations, or any text outside the JSON.
                
                REQUIRED JSON SCHEMA:
                {
                  "briefing": "<Short friendly sentence in Euskera (max 20 words) considering time and weather>",
                  "clothing": "<Recommended clothing items in Euskera>",
                  "clothingIcon": "<A single clothing emoji>",
                  "activity": "<A recommended activity in Euskera>",
                  "activityIcon": "<A single activity emoji>"
                }
            """.trimIndent()

            // 3. User Prompt: The Dynamic Data
            val userDataPrompt = """
                DATA:
                - Local Time: $time
                - Temperature: $temp ºC
                - Wind Speed: $wind km/h
                - Weather Condition: $condition
                - $historyStr
            """.trimIndent()

            // 4. API Request using JSON Format
            val request = GroqRequest(
                model = "llama-3.3-70b-versatile",
                messages = listOf(
                    GroqMessage(role = "system", content = systemPrompt),
                    GroqMessage(role = "user", content = userDataPrompt)
                ),
                temperature = 0.3,
                responseFormat = GroqResponseFormat(type = "json_object")
            )

            // 5. API Call & Error Handling
            val response = groqApi.generateInsight(apiKey, request)
            val agentResponseText = response.choices?.firstOrNull()?.message?.content?.trim()

            if (!agentResponseText.isNullOrBlank()) {
                try {
                    val cleanJson = agentResponseText.replace("```json", "", ignoreCase = true)
                        .replace("```", "")
                        .trim()

                    val insight = Gson().fromJson(cleanJson, AiInsight::class.java)
                    emit(Resource.Success(insight))
                } catch (e: JsonSyntaxException) {
                    Log.e("GroqAgent", "JSON parseo errorea. Raw text: $agentResponseText")
                    emit(Resource.Error("Ezin izan da AI-ren erantzuna prozesatu."))
                }
            } else {
                emit(Resource.Error("Erantzun hutsa jaso da Groq-etik."))
            }

        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: "Ezezaguna"
            Log.e("GroqAgent", "HTTP ERROREA ${e.code()}: $errorBody")
            emit(Resource.Error("Zerbitzari errorea: Kodea ${e.code()}"))
        } catch (e: Exception) {
            Log.e("GroqAgent", "Konektibitate errorea: ${e.message}", e)
            emit(Resource.Error("Ezin da konektatu agentearekin."))
        }
    }

    // ----------------------------------------------------------------------
    // 4. PUNTUA: Hizkuntza Naturaleko Kontsultak (Ask the Agent)
    // Hemen ere Ingelesezko arrazoibidea aplikatu dugu Euskarazko erantzunentzat
    // ----------------------------------------------------------------------
    override fun askQuestion(question: String, weather: Weather): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val systemPrompt = """
                You are 'CliMaster', a helpful and expert AI meteorological assistant.
                
                CRITICAL INSTRUCTIONS:
                1. Read the user's question and weather data, and think/reason in English.
                2. Output your final response to the user EXCLUSIVELY in BASQUE (Euskera).
                3. Keep the answer direct, friendly, and short (maximum 40 words).
                4. Do not use markdown formatting like **bold** or *italics*.
            """.trimIndent()

            val userPrompt = """
                CURRENT WEATHER CONTEXT:
                - Temperature: ${weather.temperature.toInt()}ºC
                - Wind: ${weather.windSpeed} km/h
                - Condition: ${weather.condition.name}
                
                USER QUESTION: "$question"
                
                ANSWER (IN BASQUE):
            """.trimIndent()

            // Oharra: Hemen ez dugu JSON behartzen, testu arrunta nahi dugulako
            val request = GroqRequest(
                model = "llama-3.3-70b-versatile", // Llama 3.3 erabiltzen dugu hemen ere koherentziarako
                messages = listOf(
                    GroqMessage(role = "system", content = systemPrompt),
                    GroqMessage(role = "user", content = userPrompt)
                ),
                temperature = 0.5 // Pixka bat sormen gehiago txat-erako
            )

            val response = groqApi.generateInsight(apiKey, request)

            val answer = response.choices?.firstOrNull()?.message?.content?.trim()
            if (!answer.isNullOrBlank()) {
                emit(Resource.Success(answer))
            } else {
                emit(Resource.Error("Ezin izan dut erantzun."))
            }
        } catch (e: Exception) {
            Log.e("GroqAgent", "Txat errorea: ${e.message}", e)
            emit(Resource.Error("Errorea galderari erantzutean."))
        }
    }
}