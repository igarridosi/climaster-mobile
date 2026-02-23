package com.climaster.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.climaster.data.local.WeatherEntity
import com.climaster.data.remote.dto.WeatherDto
import com.climaster.domain.model.Weather
import com.climaster.domain.model.WeatherCondition
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

// DTO -> Entity (Saretik DB-ra)
fun WeatherDto.toEntity(): WeatherEntity {
    return WeatherEntity(
        temperature = main.temp,
        condition = weather.firstOrNull()?.main ?: "Unknown",
        humidity = main.humidity,
        windSpeed = wind.speed,
        cityName = cityName,
        lastUpdated = System.currentTimeMillis()
    )
}

// Entity -> Domain (DB-tik UI-ra)
@RequiresApi(Build.VERSION_CODES.O)
fun WeatherEntity.toDomain(): Weather {
    return Weather(
        temperature = temperature,
        feelsLike = temperature, // API-ak ez badu ematen, berdina jarri
        humidity = humidity,
        windSpeed = windSpeed,
        condition = parseCondition(condition),
        cityName = cityName,
        lastUpdated = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(lastUpdated),
            ZoneId.systemDefault()
        )
    )
}

// String -> Enum bihurtzailea
private fun parseCondition(condition: String): WeatherCondition {
    return when(condition.lowercase()) {
        "clear" -> WeatherCondition.SUNNY
        "clouds" -> WeatherCondition.CLOUDY
        "rain", "drizzle" -> WeatherCondition.RAINY
        "thunderstorm" -> WeatherCondition.STORMY
        "snow" -> WeatherCondition.SNOWY
        else -> WeatherCondition.UNKNOWN
    }
}