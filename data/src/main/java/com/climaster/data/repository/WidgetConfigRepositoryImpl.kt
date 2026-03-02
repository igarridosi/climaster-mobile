package com.example.climaster.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences // <--- HAU DA ONA
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.climaster.core.util.Resource
import com.climaster.domain.repository.WidgetConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

// DataStore instantzia bakarra sortzen dugu
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_settings")

class WidgetConfigRepositoryImpl @Inject constructor(
    private val context: Context
) : WidgetConfigRepository {

    private val WIDGET_JSON_KEY = stringPreferencesKey("widget_json_config")

    private val client = OkHttpClient()

    override suspend fun saveWidgetConfigJson(jsonString: String) {
        context.dataStore.edit { preferences ->
            preferences[WIDGET_JSON_KEY] = jsonString
        }
    }

    override fun getWidgetConfigJson(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[WIDGET_JSON_KEY]
        }
    }

    override suspend fun fetchAndSaveWidgetConfig(url: String): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Konexioa HTTP arrunta izan ohi da sare lokalean (Cleartext baimenduta egon behar da Manifestuan, edo IP zehatza erabili)
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val json = response.body?.string() ?: return@use Resource.Error("Erantzun hutsa")
                        saveWidgetConfigJson(json) // Gorde DataStore-n
                        Resource.Success(Unit)
                    } else {
                        Resource.Error("HTTP Errorea: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Resource.Error("Sare errorea: ${e.message}")
            }
        }
    }
}