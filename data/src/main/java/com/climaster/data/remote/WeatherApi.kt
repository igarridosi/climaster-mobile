package com.climaster.data.remote

import com.climaster.data.remote.dto.WeatherDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    // ALDAKETA: Endpoint berria 'onecall' da
    @GET("onecall")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String = "51e154b61c72032ef18f3b7eea32a959", // Zure gakoa hemen
        @Query("units") units: String = "metric",
        @Query("exclude") exclude: String = "minutely,alerts" // Datu batzuk baztertzeko (aukerakoa)
    ): WeatherDto
}