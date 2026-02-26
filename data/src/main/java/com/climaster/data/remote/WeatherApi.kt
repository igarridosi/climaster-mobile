package com.example.climaster.data.remote

import com.example.climaster.data.remote.dto.PirateWeatherResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherApi {
    // Pirate Weather Endpoint-a
    @GET("forecast/{apiKey}/{lat},{lon}")
    suspend fun getWeather(
        @Path("apiKey") apiKey: String,
        @Path("lat") lat: Double,
        @Path("lon") lon: Double,
        @Query("units") units: String = "ca", // "ca" = Celsius eta km/h
        @Query("exclude") exclude: String = "minutely,alerts" // Datu soberakinak kendu
    ): PirateWeatherResponse
}