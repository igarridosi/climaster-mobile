package com.example.climaster.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.climaster.core.util.Resource
import com.climaster.data.local.WeatherDao
import com.climaster.data.local.WeatherEntity
import com.climaster.data.mapper.toEntity
import com.climaster.domain.model.DailyForecast
import com.climaster.domain.model.HourlyForecast
import com.climaster.domain.model.Weather
import com.climaster.domain.model.WeatherCondition
import com.climaster.domain.repository.WeatherRepository
import com.example.climaster.data.remote.WeatherApi
import com.example.climaster.data.remote.dto.ForecastResponseDto
import com.example.climaster.data.remote.dto.PirateDailyDataDto
import com.example.climaster.data.remote.dto.PirateHourlyDataDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi,
    private val dao: WeatherDao
) : WeatherRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getWeather(lat: Double, lon: Double): Flow<Resource<Weather>> = flow {
        emit(Resource.Loading)

        try {
            Log.d("WeatherRepo", "PirateWeather API deia egiten...")

            // Dei bakarra egiten dugu orain (Current + Forecast dena batera)
            val response = api.getWeather(
                apiKey = "fT1DSf1D3YBVGKumkvOvlftvCdOy9tJf",
                lat = lat,
                lon = lon
            )

            // Datuak erauzi
            val current = response.currently
            val daily = response.daily.data
            val hourly = response.hourly.data

            // Mapeatu Domain eredura
            val condition = mapIconToCondition(current.icon)

            // Oharra: PirateWeather-ek ez du hiri izenik ematen response-an.
            // "Unknown City" jarriko dugu behin-behinean, baina ViewModel-ak
            // Geocoding-etik jasotako izena gainidazten duenez, ez da arazorik.

            val weatherDomain = Weather(
                temperature = current.temperature,
                feelsLike = current.temperature, // PirateWeather-ek ez du "feels_like" garbi uneko datuetan (forecast-en bai)
                humidity = (current.humidity * 100).toInt(), // 0.88 -> 88%
                windSpeed = current.windSpeed,
                condition = condition,
                cityName = response.timezone.substringAfter("/").replace("_", " "), // "Europe/Madrid" -> "Madrid"
                timezone = response.timezone,
                lastUpdated = LocalDateTime.now(),
                forecast = processDailyForecast(daily),
                hourlyForecast = processHourlyForecast(hourly, response.timezone)
            )

            // Cachean gorde (Entity-ra bihurtu lehenik)
            // Oharra: Hemen Entity zaharra erabiltzen jarraituko dugu sinplifikatzeko
            // Benetako produkzioan Entity berri bat beharko litzateke.
            val entity = WeatherEntity(
                temperature = weatherDomain.temperature,
                condition = weatherDomain.condition.name,
                humidity = weatherDomain.humidity,
                windSpeed = weatherDomain.windSpeed,
                cityName = weatherDomain.cityName,
                lastUpdated = System.currentTimeMillis()
            )
            dao.clearWeather()
            dao.insertWeather(entity)

            emit(Resource.Success(weatherDomain))

        } catch (e: HttpException) {
            Log.e("WeatherRepo", "HTTP Errorea: ${e.code()}")
            emit(Resource.Error("API Errorea: ${e.code()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Ez dago konexiorik."))
        } catch (e: Exception) {
            Log.e("WeatherRepo", "Errore ezezaguna", e)
            emit(Resource.Error("Errorea: ${e.message}"))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processDailyForecast(dailyData: List<PirateDailyDataDto>): List<DailyForecast> {
        // Lehenengoa gaur da, beraz hurrengo 5ak hartzen ditugu (drop 1)
        return dailyData.drop(1).take(5).map { item ->
            val date = Instant.ofEpochSecond(item.time).atZone(ZoneId.systemDefault()).toLocalDate()
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("eu", "ES")).replace(".", "")

            val condition = mapIconToCondition(item.icon)
            val emoji = iconStringToEmoji(item.icon)

            DailyForecast(
                dayName = dayName.replaceFirstChar { it.uppercase() },
                emoji = emoji,
                maxTemp = Math.round(item.temperatureHigh).toInt(), // <--- Math.round erabili zehaztasunerako
                minTemp = Math.round(item.temperatureLow).toInt()
            )
        }
    }

    private fun mapIconToCondition(icon: String): WeatherCondition {
        return when {
            icon == "clear-day" || icon == "clear-night" -> WeatherCondition.OSKARBIA
            icon.contains("partly-cloudy") -> WeatherCondition.HODEITARTEAK
            icon.contains("cloudy") -> WeatherCondition.HODEITSUA
            icon.contains("rain") -> WeatherCondition.EURITSUA
            icon.contains("sleet") -> WeatherCondition.EURIELURRA
            icon.contains("snow") -> WeatherCondition.ELURTSUA
            icon.contains("wind") -> WeatherCondition.HAIZETSUA
            icon.contains("fog") -> WeatherCondition.LAINOTSUA
            icon.contains("thunder") -> WeatherCondition.EKAITSUA
            else -> WeatherCondition.EZEZAGUNA
        }
    }

    private fun iconStringToEmoji(icon: String): String {
        return when (icon) {
            "clear-day" -> "☀️"
            "clear-night" -> "🌙" // <--- GAUA
            "partly-cloudy-day" -> "⛅"
            "partly-cloudy-night" -> "☁️" // <--- GAUA ETA HODEIAK
            "cloudy" -> "☁️"
            "rain" -> "🌧️"
            "sleet" -> "🌨️"
            "snow" -> "❄️"
            "wind" -> "💨"
            "fog" -> "🌫️"
            else -> if (icon.contains("thunder")) "⛈️" else "🌦️"
        }
    }

    private fun getBasqueDayName(dayOfWeek: Int): String {
        return when(dayOfWeek) {
            1 -> "Asl"  // Astelehena
            2 -> "Asr"  // Asteartea
            3 -> "Asz"  // Asteazkena
            4 -> "Osg"  // Osteguna
            5 -> "Osr"  // Ostirala
            6 -> "Lr"  // Larunbata
            7 -> "Ig"  // Igandea
            else -> ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processHourlyForecast(hourlyData: List<PirateHourlyDataDto>, timezone: String): List<HourlyForecast> {
        val zoneId = ZoneId.of(timezone)
        val now = Instant.now().epochSecond

        return hourlyData
            .filter { it.time >= now } // Uneko ordutik aurrera bakarrik
            .take(24) // Hurrengo 24 orduak bakarrik hartu
            .map { item ->
                // Unix time -> Bertako ordua ("14:00")
                val timeStr = Instant.ofEpochSecond(item.time).atZone(zoneId).format(
                    DateTimeFormatter.ofPattern("HH:00"))
                val cond = mapIconToCondition(item.icon)
                val emoji = iconStringToEmoji(item.icon)

                HourlyForecast(timeStr, emoji, Math.round(item.temperature).toInt())
            }
    }
}