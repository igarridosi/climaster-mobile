package com.climaster.data.remote

import com.climaster.data.remote.dto.WeatherDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String = "ZURE_API_KEY", // Geroago ezkutatu beharko dugu
        @Query("units") units: String = "metric"
    ): WeatherDto
}