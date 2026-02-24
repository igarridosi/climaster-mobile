package com.example.climaster.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.climaster.core.util.Resource
import com.climaster.data.remote.GroqApi
import com.climaster.data.remote.dto.groq.GroqMessage
import com.climaster.data.remote.dto.groq.GroqRequest
import com.climaster.domain.model.UserThermalFeedback
import com.climaster.domain.model.Weather
import com.climaster.domain.repository.AgentRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class AgentRepositoryImpl @Inject constructor(
    private val groqApi: GroqApi
) : AgentRepository {
    //val apiKey = properties.getProperty("API_KEY") ?: ""

    // Groq-eko zure API gakoa.
    // GEROAGO hau babestu beharko dugu (local.properties)
    private val groqApiKey = "Bearer apiKey"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun generatePersonalizedInsight(
        weather: Weather,
        userHistory: List<UserThermalFeedback>
    ): Flow<Resource<String>> = flow {

        emit(Resource.Loading)

        try {
            // --- 1. PROMPT-A BATERATU (Llama3-rekin bateragarria eta segurua) ---
            val prompt = """
                [INST] 
                Zure rola: CliMaster aplikazioko agente meteorologo aditua.
                Hizkuntza: Beti Euskaraz hitz egin.
                Tonua: Atsegina, lagunkoia, eta zuzena.
                Helburua: Eguraldiaren datu gordinak jakintza pertsonalizatu bihurtzea.
                Arauak:
                - Eman esaldi MOTZ bat (max 20 hitz), umore ukitu bat izan dezakeena.
                - Ez errepikatu datu agerikoak (ez esan "15 gradu daude"). Esan zer esan nahi duten.
                - Analizatu erabiltzailearen historia termikoa ('feedback') ondorio pertsonalizatuak ateratzeko.

                Hona hemen uneko egoera:
                ${ // Datuen JSONa hemen eraikiko dugu eskuz
                buildString {
                    append("{")
                    append("\"eguraldia\":{")
                    append("\"tenperatura\":${weather.temperature},")
                    append("\"haizea_kmh\":${weather.windSpeed},")
                    append("\"hezetasuna_percent\":${weather.humidity},")
                    append("\"egoera\":\"${weather.condition.name}\"")
                    append("},")
                    append("\"ordua\":\"${weather.lastUpdated.format(DateTimeFormatter.ofPattern("HH:mm"))}\"")

                    if (userHistory.isNotEmpty()) {
                        append(",")
                        append("\"erabiltzailearen_feedback_historikoa\":[")
                        append(userHistory.joinToString(separator = ",") {
                            """{"temp_gordeta":${it.actualTemp},"sentsazioa":"${it.sensation.name}"}"""
                        })
                        append("]")
                    }
                    append("}")
                }
            }
                
                Orain, eman aholku pertsonalizatua. Adibidez, erabiltzaileak 15 gradurekin 'HOTZ' esan badu, ondorioztatu hotzbera dela.
                [/INST]
            """.trimIndent()

            // 2. GROQ APIari eskaera egin (Mezu BAKAR batekin)
            val request = GroqRequest(
                messages = listOf(
                    GroqMessage(
                        role = "user",
                        content = prompt
                    )
                )
            )

            // DEBUG: Egiaztatu zer bidaltzen ari garen
            Log.d("GroqAgent", "Bidalitako JSON eskaera: ${Gson().toJson(request)}")

            val response = groqApi.generateInsight(groqApiKey, request)

            val agentResponse = response.choices.firstOrNull()?.message?.content
            if (agentResponse != null) {
                emit(Resource.Success(agentResponse))
            } else {
                emit(Resource.Error("Agenteak ezin izan du erantzunik sortu."))
            }

        } catch (e: Exception) {
            Log.e("GroqAgent", "Agenteari deitzean errorea gertatu da", e)
            emit(Resource.Error("Errorea Agentearekin komunikatzean: ${e.message}"))
        }
    }
}