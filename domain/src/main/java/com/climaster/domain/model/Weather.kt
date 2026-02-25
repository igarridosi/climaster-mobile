package com.climaster.domain.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime

data class Weather @RequiresApi(Build.VERSION_CODES.O) constructor(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val condition: WeatherCondition,
    val cityName: String,
    val timezone: String,
    val lastUpdated: LocalDateTime = LocalDateTime.now(),
    val forecast: List<DailyForecast> = emptyList()
)
