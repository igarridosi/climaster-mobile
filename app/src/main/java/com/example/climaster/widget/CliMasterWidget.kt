package com.example.climaster.widget

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.climaster.core.util.Resource
import com.climaster.domain.model.Weather
import com.example.climaster.data.repository.dataStore
import com.google.gson.Gson
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

class CliMasterWidget : GlanceAppWidget() {

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("RestrictedApi")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // 1. REPOSITORIOA LORTU (Hilt EntryPoint bidez)
        val appContext = context.applicationContext
        /*
        val weatherRepo = EntryPointAccessors.fromApplication(
            appContext,
            WidgetEntryPoint::class.java
        ).weatherRepository()
*/
        // Lortu EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(appContext, WidgetEntryPoint::class.java)
        val weatherRepo = entryPoint.weatherRepository()
        val locationTracker = entryPoint.locationTracker()

        provideContent {
            // Egoerak kargatu
            var weatherData by remember { mutableStateOf<Weather?>(null) }
            var widgetConfig by remember { mutableStateOf<WidgetConfig?>(null) }

            LaunchedEffect(Unit) {
                // A. JSON Konfigurazioa irakurri
                val prefs = context.dataStore.data.first()
                val jsonString = prefs[androidx.datastore.preferences.core.stringPreferencesKey("widget_json_config")]
                if (jsonString != null) {
                    try {
                        widgetConfig = Gson().fromJson(jsonString, WidgetConfig::class.java)
                    } catch (e: Exception) { e.printStackTrace() }
                }

                // B. Eguraldia eguneratu (Donostia defektuz, edo GPS erabili etorkizunean)
                weatherRepo.getWeather(43.31, -1.98).collect { result ->
                    if (result is Resource.Success) {
                        weatherData = result.data
                    }
                }

                val location = locationTracker.getCurrentLocation()

                if (location != null) {
                    weatherRepo.getWeather(location.lat, location.lon).collect { result ->
                        if (result is Resource.Success) {
                            // Izen zuzena jarri (GPSak emandakoa)
                            val finalData = if (location.cityName != null) {
                                result.data.copy(cityName = location.cityName!!)
                            } else result.data

                            weatherData = finalData
                        }
                    }
                } else {
                    // Fallback: Kokapena ez badabil, Donostia
                    weatherRepo.getWeather(43.31, -1.98).collect { result ->
                        if (result is Resource.Success) weatherData = result.data
                    }
                }
            }

            GlanceTheme {
                if (widgetConfig != null && weatherData != null) {
                    // Dena prest dagoenean, marraztu!
                    RenderWidget(widgetConfig!!, weatherData!!)
                } else {
                    // Kargatzen...
                    Box(
                        modifier = GlanceModifier.fillMaxSize().background(Color(0xFF1E3C72)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Kargatzen...", style = TextStyle(color = ColorProvider(Color.White)))
                    }
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    fun RenderWidget(config: WidgetConfig, weather: Weather) {
        // Kolore hamartarra (Hex) prozesatu
        val bgColor = try {
            Color(android.graphics.Color.parseColor(config.background.color))
        } catch (e: Exception) { Color(0x33FFFFFF) }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(bgColor)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LOOP MAGIKOA: JSON-eko zerrenda irakurri eta marraztu
            config.layout.forEach { element ->
                when (element.type) {
                    "current_temp" -> {
                        Text(
                            text = "${weather.temperature.toInt()}°",
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = element.fontSize.sp,
                                textAlign = getAlign(element.alignment)
                            )
                        )
                    }
                    "current_condition_text" -> {
                        Text(
                            text = weather.condition.name,
                            style = TextStyle(
                                color = ColorProvider(Color.White.copy(alpha = 0.9f)),
                                fontSize = element.fontSize.sp,
                                textAlign = getAlign(element.alignment)
                            )
                        )
                    }
                    "horizontal_divider" -> {
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        Box(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.3f))) {}
                        Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                    "humidity" -> {
                        Text(
                            text = "💧 ${weather.humidity}%",
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = element.fontSize.sp,
                                textAlign = getAlign(element.alignment)
                            ),
                            modifier = GlanceModifier.fillMaxWidth()
                        )
                    }
                    "wind_speed" -> {
                        Text(
                            text = "💨 ${weather.windSpeed} km/h",
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = element.fontSize.sp,
                                textAlign = getAlign(element.alignment)
                            ),
                            modifier = GlanceModifier.fillMaxWidth()
                        )
                    }
                    "daily_forecast_row" -> {
                        // Iragarpen txikia marraztu
                        Row(modifier = GlanceModifier.fillMaxWidth().padding(top = 8.dp)) {
                            weather.forecast.take(element.days).forEach { day ->
                                Column(modifier = GlanceModifier.defaultWeight()) {
                                    Text(day.dayName, style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp))
                                    Text(day.emoji, style = TextStyle(fontSize = 16.sp))
                                    Text("${day.maxTemp}°", style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getAlign(align: String): TextAlign {
        return when (align) {
            "center" -> TextAlign.Center
            "right" -> TextAlign.End
            else -> TextAlign.Start
        }
    }
}