package com.climaster.domain.model

import java.time.LocalDateTime

data class MicroClimateReport(
    val reportId: String,
    val latitude: Double,
    val longitude: Double,
    val condition: WeatherCondition, // Lehendik daukagun Enum-a berrerabili
    val comment: String?,
    val timestamp: LocalDateTime,
    val userReputationScore: Int // Gamifikazio pixka bat
)