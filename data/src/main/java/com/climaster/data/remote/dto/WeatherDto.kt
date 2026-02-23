package com.climaster.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WeatherDto(
    @SerializedName("main") val main: MainDto,
    @SerializedName("weather") val weather: List<WeatherDescriptionDto>,
    @SerializedName("wind") val wind: WindDto,
    @SerializedName("name") val cityName: String
)

data class MainDto(
    val temp: Double,
    val feels_like: Double,
    val humidity: Int
)

data class WeatherDescriptionDto(
    val main: String, // "Rain", "Clear", etc.
    val description: String
)

data class WindDto(
    val speed: Double
)
