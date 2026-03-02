package com.climaster.domain.repository

import com.climaster.core.util.Resource
import kotlinx.coroutines.flow.Flow

interface WidgetConfigRepository {
    suspend fun saveWidgetConfigJson(jsonString: String)
    suspend fun fetchAndSaveWidgetConfig(url: String): Resource<Unit>
    fun getWidgetConfigJson(): Flow<String?>
}