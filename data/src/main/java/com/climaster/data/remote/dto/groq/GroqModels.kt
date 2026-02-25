package com.example.climaster.data.remote.dto.groq

import com.google.gson.annotations.SerializedName

/**
 * Enhanced Request class with support for JSON Mode and Sampling Control.
 */
data class GroqRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<GroqMessage>,
    @SerializedName("temperature") val temperature: Double? = 0.5,
    @SerializedName("max_tokens") val maxTokens: Int? = 1024,
    @SerializedName("top_p") val topP: Double? = 1.0,
    @SerializedName("stream") val stream: Boolean = false,
    @SerializedName("response_format") val responseFormat: GroqResponseFormat? = null,
    @SerializedName("seed") val seed: Int? = null // For deterministic testing
)

data class GroqResponseFormat(
    @SerializedName("type") val type: String = "json_object"
)

data class GroqMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

/**
 * Comprehensive Response structure including Usage tracking.
 */
data class GroqResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("choices") val choices: List<GroqChoice>?,
    @SerializedName("usage") val usage: GroqUsage? // Highly recommended for monitoring
)

data class GroqChoice(
    @SerializedName("message") val message: GroqMessage?,
    @SerializedName("finish_reason") val finishReason: String?
)

data class GroqUsage(
    @SerializedName("prompt_tokens") val promptTokens: Int,
    @SerializedName("completion_tokens") val completionTokens: Int,
    @SerializedName("total_tokens") val totalTokens: Int
)