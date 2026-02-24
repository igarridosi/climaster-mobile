package com.climaster.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WeatherEntity::class, UserFeedbackEntity::class],
    version = 1
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract val weatherDao: WeatherDao
    abstract val userFeedbackDao: UserFeedbackDao
}