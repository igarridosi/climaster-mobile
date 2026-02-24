package com.climaster.data.remote

import com.climaster.data.remote.dto.groq.GroqRequest
import com.climaster.data.remote.dto.groq.GroqResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApi {
    @POST("chat/completions")
    suspend fun generateInsight(
        @Header("Authorization") apiKey: String, // "Bearer <API_KEY>"
        @Body request: GroqRequest
    ): GroqResponse
}