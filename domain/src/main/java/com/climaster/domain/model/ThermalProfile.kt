package com.climaster.domain.model

import java.time.LocalDateTime

enum class ThermalSensation {
    FREEZING, // Iozten
    COLD,     // Hotz
    COMFORTABLE, // Eroso
    WARM,     // Bero
    HOT       // Kixkaltzen
}

data class UserThermalFeedback(
    val id: String, // UUID
    val sensation: ThermalSensation,
    val actualTemp: Double,
    val humidity: Int,
    val timestamp: LocalDateTime = LocalDateTime.now()
)