package com.climaster.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getWeather(lat: Double, lon: Double): Flow<Resource<Weather>> = flow {
        emit(Resource.Loading)
        println("DEBUG: Datuak eskatzen hasten...")

        // 1. Saiatu datu lokalak irakurtzen lehenik (Offline First)
        // Oharra: Hemen logika sinplea egingo dugu, Flow batetik irakurtzea konplexuagoa delako
        // Baina printzipioa da: Datu zaharrak erakutsi -> Sarea deitu -> Datu berriak gorde

        try {
            // 1. API Deia
            println("DEBUG: API deia egiten...")
            val remoteData = api.getCurrentWeather(lat, lon)
            println("DEBUG: API erantzuna jaso da: $remoteData")

            // 2. DB Garbitu eta Gorde
            dao.clearWeather()
            dao.insertWeather(remoteData.toEntity())
            println("DEBUG: Datu-basean gordeta")

            // 3. Igorri Datuak (Hau falta bazen, loading geratuko da!)
            val newWeather = remoteData.toEntity().toDomain()
            emit(Resource.Success(newWeather))
            println("DEBUG: Success igorrita")

        } catch (e: Exception) {
            // GARRANTZITSUA: Exception orokorra harrapatu (ez bakarrik HTTP)
            e.printStackTrace() // Logcat-en errorea gorriz ikusteko
            println("DEBUG: ERROREA GERTATU DA: ${e.message}")

            // UI-ari errorea bidali pantailan ikusteko
            emit(Resource.Error("Errorea: ${e.localizedMessage}"))
        }

        // Azkenik, DB-ko datuak igorri (bazeuden edo berriak badira)
        // Oharra: Hau hobetu daiteke `networkBoundResource` patroiarekin
    }
}