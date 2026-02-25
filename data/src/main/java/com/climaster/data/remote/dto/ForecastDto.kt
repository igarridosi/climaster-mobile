package com.example.climaster.data.remote.dto

import com.climaster.data.remote.dto.WeatherDescriptionDto
import com.google.gson.annotations.SerializedName

data class ForecastResponseDto(
    @SerializedName("list") val list: List<ForecastItemDto>
)

data class ForecastItemDto(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: MainForecastDto,
    @SerializedName("weather") val weather: List<WeatherDescriptionDto>,
    @SerializedName("dt_txt") val dtTxt: String // Formatoa: "2026-02-25 15:00:00"
)

data class MainForecastDto(
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double
)