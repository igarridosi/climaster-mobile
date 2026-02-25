package com.example.climaster.data.remote

import com.example.climaster.data.remote.dto.groq.GroqRequest
import com.example.climaster.data.remote.dto.groq.GroqResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface GroqApi {
    @Headers("Content-Type: application/json") // HAU EZINBESTEKOA DA GROQ-ENTZAT
    @POST("chat/completions")
    suspend fun generateInsight(
        @Header("Authorization") apiKey: String,
        @Body request: GroqRequest
    ): GroqResponse
}