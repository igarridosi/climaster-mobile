package com.climaster.domain.model

import java.time.LocalDateTime

data class Weather(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val condition: WeatherCondition,
    val cityName: String,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
)
