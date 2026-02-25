package com.example.climaster.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.climaster.core.util.Resource
import com.example.climaster.data.remote.GroqApi
import com.example.climaster.data.remote.dto.groq.GroqMessage
import com.example.climaster.data.remote.dto.groq.GroqRequest
import com.climaster.domain.model.UserThermalFeedback
import com.climaster.domain.model.Weather
import com.climaster.domain.repository.AgentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import com.climaster.domain.model.AiInsight
import com.example.climaster.data.remote.dto.groq.GroqResponseFormat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

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
            // 1. Data Processing (Prepared in English for better AI comprehension)
            val time = weather.lastUpdated.format(DateTimeFormatter.ofPattern("HH:mm"))
            val temp = weather.temperature.toInt()
            val wind = weather.windSpeed
            val condition = weather.condition.name // Make sure this returns an English string if possible (e.g., "RAINY")

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

            // 4. API Request using Roles
            val request = GroqRequest(
                model = "llama-3.3-70b-versatile",
                messages = listOf(
                    GroqMessage(role = "system", content = systemPrompt),
                    GroqMessage(role = "user", content = userDataPrompt)
                ),
                temperature = 0.3, // Keep it grounded
                responseFormat = GroqResponseFormat(type = "json_object") // Force JSON
            )

            // 5. API Call & Error Handling
            Log.d("GroqAgent", "Deia egiten ari da...")
            val response = groqApi.generateInsight(apiKey, request)
            val agentResponseText = response.choices?.firstOrNull()?.message?.content?.trim()

            if (!agentResponseText.isNullOrBlank()) {
                try {
                    // Defensive cleaning (always keep this, LLaMA can occasionally be stubborn)
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
            // HAU DA GAKOA: 400 erroreak zergatik huts egiten duen erakutsiko digu Logcat-en
            val errorBody = e.response()?.errorBody()?.string() ?: "Ezezaguna"
            Log.e("GroqAgent", "HTTP ERROREA ${e.code()}: $errorBody")
            emit(Resource.Error("Zerbitzari errorea: Kodea ${e.code()}"))

        } catch (e: Exception) {
            Log.e("GroqAgent", "Konektibitate errorea: ${e.message}", e)
            emit(Resource.Error("Ezin da konektatu agentearekin."))
        }
    }
}