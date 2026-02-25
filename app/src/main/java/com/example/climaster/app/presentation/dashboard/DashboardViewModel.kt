package com.example.climaster.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.climaster.core.util.Resource
import com.climaster.domain.model.AiInsight
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
import com.climaster.domain.usecase.GenerateInsightUseCase

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getWeatherUseCase: GetWeatherUseCase,
    private val feedbackRepository: UserFeedbackRepository,
    private val geocodingApi: GeocodingApi,
    private val generateInsightUseCase: GenerateInsightUseCase
) : ViewModel() {

    private val _weatherState = MutableStateFlow<Resource<Weather>>(Resource.Loading)
    val weatherState: StateFlow<Resource<Weather>> = _weatherState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<Resource<List<LocationResult>>>(Resource.Success(emptyList()))
    val searchResults: StateFlow<Resource<List<LocationResult>>> = _searchResults

    private val _recommendationState = MutableStateFlow<Resource<AiInsight>>(Resource.Loading)
    val recommendationState: StateFlow<Resource<AiInsight>> = _recommendationState

    private var searchJob: Job? = null

    init {
        loadWeather(43.31, -1.98, "Donostia")
    }

    fun loadWeather(lat: Double, lon: Double, customName: String? = null) {
        viewModelScope.launch {
            _weatherState.value = Resource.Loading
            getWeatherUseCase(lat, lon).collect { result ->
                if (result is Resource.Success) {
                    val weatherData = result.data
                    // Izena pertsonalizatu
                    val finalData = if (customName != null) {
                        weatherData.copy(cityName = customName.split(",").first().trim())
                    } else {
                        weatherData
                    }
                    _weatherState.value = Resource.Success(finalData)

                    // --- MAGIA HEMEN DAGO ---
                    // Eguraldia ondo kargatu denean, AGENTEARI deitu
                    fetchAgentInsight(finalData)

                } else if (result is Resource.Error) {
                    _weatherState.value = result // Errorea pasatu UI-ari
                }
            }
        }
    }

    private fun fetchAgentInsight(weather: Weather) {
        viewModelScope.launch {
            generateInsightUseCase(weather).collect { insightResult ->
                _recommendationState.value = insightResult
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