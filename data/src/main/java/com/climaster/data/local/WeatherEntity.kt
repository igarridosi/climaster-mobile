package com.climaster.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_table")
data class WeatherEntity(
    @PrimaryKey val id: Int = 0, // Bakarra gordeko dugu oraingoz
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val cityName: String,
    val lastUpdated: Long // Timestamp
)
