package com.example.climaster.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GeocodingDto(
    @SerializedName("display_name") val displayName: String,
    @SerializedName("lat") val lat: String, // API honek String gisa itzultzen ditu
    @SerializedName("lon") val lon: String
)