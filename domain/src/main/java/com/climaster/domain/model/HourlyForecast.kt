package com.climaster.domain.model

data class HourlyForecast(
    val time: String, // Adib: "14:00"
    val emoji: String,
    val temp: Int
)