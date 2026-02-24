package com.example.climaster.data.remote

import com.example.climaster.data.remote.dto.GeocodingDto
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {
    @GET("search")
    suspend fun searchLocations(
        @Query("q") query: String,
        // SARTU HEMEN ZURE BENETAKO API KEY-A:
        @Query("api_key") apiKey: String = "699d7d5aa6587567928082had81ed01"
    ): List<GeocodingDto>
}