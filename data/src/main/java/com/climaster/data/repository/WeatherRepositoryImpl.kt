package com.climaster.data.repository

import com.climaster.core.util.Resource
import com.climaster.data.local.WeatherDao
import com.climaster.data.mapper.toDomain
import com.climaster.data.mapper.toEntity
import com.climaster.data.remote.WeatherApi
import com.climaster.domain.model.Weather
import com.climaster.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject // Hilt geroago gehituko dugu

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi,
    private val dao: WeatherDao
) : WeatherRepository {

    override fun getWeather(lat: Double, lon: Double): Flow<Resource<Weather>> = flow {
        emit(Resource.Loading)

        // 1. Saiatu datu lokalak irakurtzen lehenik (Offline First)
        // Oharra: Hemen logika sinplea egingo dugu, Flow batetik irakurtzea konplexuagoa delako
        // Baina printzipioa da: Datu zaharrak erakutsi -> Sarea deitu -> Datu berriak gorde

        try {
            // Sarea deitu
            val remoteData = api.getCurrentWeather(lat, lon)

            // Datu-basea garbitu eta berria gorde
            dao.clearWeather()
            dao.insertWeather(remoteData.toEntity())

            // Orain datu berriak igorri (DB-tik irakurrita, Source of Truth bakarra izateko)
            // Baina Flow denez, DAO-k automatikoki eguneratuko luke.
            // Hemen zuzenean DB-tik irakurri beharko genuke

        } catch (e: HttpException) {
            emit(Resource.Error("Zerbitzariaren errorea: ${e.message}"))
        } catch (e: IOException) {
            emit(Resource.Error("Ez dago konexiorik. Datu lokalak erakusten..."))
        }

        // Azkenik, DB-ko datuak igorri (bazeuden edo berriak badira)
        // Oharra: Hau hobetu daiteke `networkBoundResource` patroiarekin
    }
}