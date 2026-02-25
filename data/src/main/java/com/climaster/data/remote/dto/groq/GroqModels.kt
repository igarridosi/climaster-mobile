package com.example.climaster.data.remote.dto.groq

import com.google.gson.annotations.SerializedName

data class GroqRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<GroqMessage>,
    @SerializedName("temperature") val temperature: Double? = null,
    @SerializedName("response_format") val responseFormat: GroqResponseFormat? = null // JSON behartzeko
)

data class GroqResponseFormat(
    @SerializedName("type") val type: String = "json_object"
)

data class GroqMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class GroqResponse(
    @SerializedName("choices") val choices: List<GroqChoice>?
)

data class GroqChoice(
    @SerializedName("message") val message: GroqMessage?
)