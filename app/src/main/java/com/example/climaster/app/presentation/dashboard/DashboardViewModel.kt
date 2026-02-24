package com.example.climaster.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.climaster.core.util.Resource
import com.climaster.domain.model.ThermalSensation
import com.climaster.domain.model.UserThermalFeedback
import com.climaster.domain.model.Weather
import com.climaster.domain.repository.UserFeedbackRepository
import com.climaster.domain.usecase.GetWeatherUseCase
import com.example.climaster.data.remote.GeocodingApi
import com.example.climaster.domain.model.LocationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getWeatherUseCase: GetWeatherUseCase,
    private val feedbackRepository: UserFeedbackRepository,
    private val geocodingApi: GeocodingApi
) : ViewModel() {

    private val _weatherState = MutableStateFlow<Resource<Weather>>(Resource.Loading)
    val weatherState: StateFlow<Resource<Weather>> = _weatherState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<Resource<List<LocationResult>>>(Resource.Success(emptyList()))
    val searchResults: StateFlow<Resource<List<LocationResult>>> = _searchResults

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

    private var searchJob: Job? = null

    init {
        loadWeather(43.31, -1.98, "Donostia")
    }

    // PARAMETRO BERRIA: customName
    fun loadWeather(lat: Double, lon: Double, customName: String? = null) {
        viewModelScope.launch {
            _weatherState.value = Resource.Loading
            getWeatherUseCase(lat, lon).collect { result ->
                if (result is Resource.Success && customName != null) {
                    // APIak "Donostia, Euskadi, España" ematen badu, lehenengo hitza bakarrik hartu:
                    val cleanName = customName.split(",").first().trim()
                    // Gure izen propioa injektatu (APIarena alde batera utziz)
                    _weatherState.value = Resource.Success(result.data.copy(cityName = cleanName))
                } else {
                    _weatherState.value = result
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.length < 3) {
            _searchResults.value = Resource.Success(emptyList())
            return
        }

        searchJob = viewModelScope.launch {
            // 1. SARTU BEREHALA LOADING EGOERAN idazten hasten den unean!
            _searchResults.value = Resource.Loading

            // 2. Orain itxaron 600ms (Debounce) APIari deitu aurretik
            delay(600)

            try {
                val results = geocodingApi.searchLocations(query = query)
                val mappedList = results.map {
                    LocationResult(it.displayName, it.lat.toDouble(), it.lon.toDouble())
                }
                _searchResults.value = Resource.Success(mappedList)
            } catch (e: Exception) {
                _searchResults.value = Resource.Error("Errorea bilaketan")
            }
        }
    }

    // ORAIN IZENA ERE PASATZEN DUGU
    fun selectLocation(name: String, lat: Double, lon: Double) {
        loadWeather(lat, lon, name)
        _searchQuery.value = ""
        _searchResults.value = Resource.Success(emptyList())
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
        }
    }
}