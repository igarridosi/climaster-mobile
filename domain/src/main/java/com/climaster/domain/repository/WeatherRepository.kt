package com.climaster.domain.repository

import com.climaster.core.util.Resource
import com.climaster.domain.model.Weather
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    // Flow erabiltzen dugu datuak denbora errealean eguneratu ahal izateko
    // Latitud eta Longitud jasotzen ditu, eta Resource<Weather> itzultzen du
    fun getWeather(lat: Double, lon: Double): Flow<Resource<Weather>>
}