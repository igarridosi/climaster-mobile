package com.climaster.domain.model

data class DailyForecast(
    val dayName: String, // Adib: "Aste", "Ost"
    val emoji: String,   // Adib: "☀️"
    val maxTemp: Int,
    val minTemp: Int
)