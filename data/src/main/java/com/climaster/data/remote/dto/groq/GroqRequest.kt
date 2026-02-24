package com.climaster.data.remote.dto.groq

data class GroqRequest(
    val model: String = "llama3-8b-8192", // Edo "mixtral-8x7b-32768"
    val messages: List<GroqMessage>,
    val temperature: Double = 0.7, // Sormen maila
    val max_tokens: Int = 150 // Erantzun motzak nahi ditugu
)

data class GroqMessage(
    val role: String, // "system" edo "user"
    val content: String
)