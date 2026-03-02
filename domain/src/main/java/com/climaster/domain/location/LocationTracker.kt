package com.climaster.domain.location

data class UserLocation(val lat: Double, val lon: Double, val cityName: String?)

interface LocationTracker {
    suspend fun getCurrentLocation(): UserLocation?
}