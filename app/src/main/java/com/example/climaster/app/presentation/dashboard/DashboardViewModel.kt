package com.example.climaster.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.climaster.core.util.Resource
import com.climaster.domain.model.Weather
import com.climaster.domain.usecase.GetWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.climaster.domain.model.ThermalSensation
import com.climaster.domain.model.UserThermalFeedback
import com.climaster.domain.repository.UserFeedbackRepository
import java.util.UUID
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getWeatherUseCase: GetWeatherUseCase,
    private val feedbackRepository: UserFeedbackRepository
) : ViewModel() {

    private val _weatherState = MutableStateFlow<Resource<Weather>>(Resource.Loading)
    val weatherState: StateFlow<Resource<Weather>> = _weatherState

    // Gomendio motorra
    val recommendationText: StateFlow<String> = _weatherState.map { state ->
        if (state is Resource.Success) {
            val temp = state.data.temperature
            val condition = state.data.condition

            when {
                temp < 10 -> "Oso hotz dago. Txamarra lodia eta eskularruak eraman!"
                temp in 10.0..18.0 -> "Giro freskoa. Jertse bat nahikoa izan daiteke."
                temp > 25 -> "Bero handia. Ur asko edan eta itzaletan egon."
                condition == com.climaster.domain.model.WeatherCondition.RAINY -> "Euria ari du. Ez ahaztu aterkia!"
                else -> "Giro atsegina. Gozatu egunaz!"
            }
        } else {
            "Datuak aztertzen..."
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, "Kargatzen...")

    init {
        loadWeather(43.31, -1.98)
    }

    fun loadWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            getWeatherUseCase(lat, lon).collect { result ->
                _weatherState.value = result
            }
        }
    }

    fun submitThermalFeedback(sensation: ThermalSensation) {
        val currentWeather = (_weatherState.value as? Resource.Success)?.data ?: return

        viewModelScope.launch {
            val feedback = UserThermalFeedback(
                id = UUID.randomUUID().toString(),
                sensation = sensation,
                actualTemp = currentWeather.temperature,
                humidity = currentWeather.humidity
            )
            feedbackRepository.submitFeedback(feedback)
            println("Feedback gordeta: $sensation")
        }
    }

    // Funtzio berria: Hiriak bilatzeko (Prototipo UXrako)
    fun searchCity(cityName: String) {
        // Benetako proiektu batean hemen Geocoding API bat deituko genuke
        // (adib. api.openweathermap.org/geo/1.0/direct?q=London)
        // UX-a probatzeko, hiri nagusien mapa bat erabiliko dugu:
        val cities = mapOf(
            "madrid" to Pair(40.4168, -3.7038),
            "donostia" to Pair(43.3183, -1.9812),
            "bilbao" to Pair(43.2630, -2.9350),
            "vitoria" to Pair(42.8467, -2.6716),
            "london" to Pair(51.5074, -0.1278),
            "new york" to Pair(40.7128, -74.0060),
            "tokyo" to Pair(35.6762, 139.6503),
            "caracal" to Pair(44.1157446, 24.3424754),
            "munich" to Pair(53.2275529, -114.0500485),
        )

        val coords = cities[cityName.lowercase().trim()]
        if (coords != null) {
            loadWeather(coords.first, coords.second)
        }
    }
}