package com.example.climaster.widget

import com.climaster.domain.location.LocationTracker
import com.climaster.domain.repository.WeatherRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun weatherRepository(): WeatherRepository
    fun locationTracker(): LocationTracker
}