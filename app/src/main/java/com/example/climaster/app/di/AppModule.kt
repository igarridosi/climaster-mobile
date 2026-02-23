package com.example.climaster.app.di

import android.app.Application
import androidx.room.Room
import com.climaster.data.local.WeatherDao
import com.climaster.data.local.WeatherDatabase
import com.climaster.data.remote.WeatherApi
import com.climaster.data.repository.WeatherRepositoryImpl
import com.climaster.domain.repository.WeatherRepository
import com.climaster.domain.usecase.GetWeatherUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Aplikazio osoan biziko diren instantziak
object AppModule {

    // --- NETWORK (RETROFIT) ---
    @Provides
    @Singleton
    fun provideWeatherApi(): WeatherApi {
        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/") // Base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }

    // --- DATABASE (ROOM) ---
    @Provides
    @Singleton
    fun provideWeatherDatabase(app: Application): WeatherDatabase {
        return Room.databaseBuilder(
            app,
            WeatherDatabase::class.java,
            "weather_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideWeatherDao(db: WeatherDatabase): WeatherDao {
        return db.weatherDao
    }

    // --- REPOSITORY (INTERFACE -> IMPL) ---
    @Provides
    @Singleton
    fun provideWeatherRepository(
        api: WeatherApi,
        dao: WeatherDao
    ): WeatherRepository {
        // Hemen esaten diogu Hilt-i: "Norbaitek WeatherRepository eskatzen duenean,
        // eman WeatherRepositoryImpl instantzia bat, api eta dao erabiliz".
        return WeatherRepositoryImpl(api, dao)
    }

    // --- USE CASES ---
    @Provides
    @Singleton
    fun provideGetWeatherUseCase(repository: WeatherRepository): GetWeatherUseCase {
        return GetWeatherUseCase(repository)
    }
}