package com.climaster.data.remote.dto

import com.google.gson.annotations.SerializedName

// One Call API 3.0-ren erantzun nagusia
data class WeatherDto(
    @SerializedName("current") val current: CurrentWeatherDto,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double
)

data class CurrentWeatherDto(
    @SerializedName("temp") val temp: Double,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("wind_speed") val windSpeed: Double,
    @SerializedName("weather") val weather: List<WeatherDescriptionDto>,
    @SerializedName("dt") val dt: Long // Data (timestamp)
)

data class WeatherDescriptionDto(
    val main: String, // "Rain", "Clear", etc.
    val description: String,
    val icon: String
)
