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
    // One Call API-ak ez du hiriaren izena ematen zuzenean.
    // Timezone erabiltzen dugu (adib: "Europe/Madrid") edo izen generiko bat.
    // Etorkizunean Geocoding API erabiliko dugu izena lortzeko.
    val locationName = timezone.substringAfterLast("/").replace("_", " ")

    return WeatherEntity(
        temperature = current.temp,
        condition = current.weather.firstOrNull()?.main ?: "Unknown",
        humidity = current.humidity,
        windSpeed = current.windSpeed,
        cityName = locationName, // "Madrid" agertuko da koordenatu horiekin
        lastUpdated = System.currentTimeMillis()
    )
}

// String -> Enum bihurtzailea
private fun parseCondition(condition: String): WeatherCondition {
    return when(condition.lowercase()) {
        "clear" -> WeatherCondition.OSKARBIA
        "clouds" -> WeatherCondition.HODEITSUA
        "rain", "drizzle" -> WeatherCondition.EURITSUA
        "thunderstorm" -> WeatherCondition.EKAITSUA
        "snow" -> WeatherCondition.ELURTSUA
        else -> WeatherCondition.EZEZAGUNA
    }
}