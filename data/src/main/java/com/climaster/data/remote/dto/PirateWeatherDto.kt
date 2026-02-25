package com.example.climaster.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PirateWeatherResponse(
    @SerializedName("currently") val currently: PirateCurrentDto,
    @SerializedName("daily") val daily: PirateDailyDto,
    @SerializedName("timezone") val timezone: String
)

data class PirateCurrentDto(
    @SerializedName("temperature") val temperature: Double,
    @SerializedName("humidity") val humidity: Double, // 0.0 - 1.0 formatuan dator (ez %)
    @SerializedName("windSpeed") val windSpeed: Double,
    @SerializedName("icon") val icon: String, // "rain", "clear-day", etab.
    @SerializedName("summary") val summary: String
)

data class PirateDailyDto(
    @SerializedName("data") val data: List<PirateDailyDataDto>
)

data class PirateDailyDataDto(
    @SerializedName("time") val time: Long,
    @SerializedName("temperatureHigh") val temperatureHigh: Double,
    @SerializedName("temperatureLow") val temperatureLow: Double,
    @SerializedName("icon") val icon: String
)