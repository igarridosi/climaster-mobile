package com.climaster.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("SELECT * FROM weather_table WHERE id = 0")
    fun getWeather(): Flow<WeatherEntity?> // Flow itzultzen du, aldaketak entzuteko

    @Query("DELETE FROM weather_table")
    suspend fun clearWeather()
}