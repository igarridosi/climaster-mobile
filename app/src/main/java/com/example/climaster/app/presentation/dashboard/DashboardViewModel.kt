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

@HiltViewModel // Hau ezinbestekoa da Hilt-ek jakiteko ViewModel bat dela
class DashboardViewModel @Inject constructor(
    private val getWeatherUseCase: GetWeatherUseCase,
    private val feedbackRepository: UserFeedbackRepository
) : ViewModel() {

    // UI Egoera (StateFlow)
    private val _weatherState = MutableStateFlow<Resource<Weather>>(Resource.Loading)
    val weatherState: StateFlow<Resource<Weather>> = _weatherState

    init {
        // Hasieratzean eguraldia kargatu (Adibidez: Bilbo)
        loadWeather(43.2627, -2.9253)
    }

    fun loadWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            getWeatherUseCase(lat, lon).collect { result ->
                _weatherState.value = result
            }
        }
    }

    // Funtzio berria: Feedback-a bidaltzeko
    fun submitThermalFeedback(sensation: ThermalSensation) {
        val currentWeather = (weatherState.value as? Resource.Success)?.data ?: return

        viewModelScope.launch {
            val feedback = UserThermalFeedback(
                id = UUID.randomUUID().toString(),
                sensation = sensation,
                actualTemp = currentWeather.temperature,
                humidity = currentWeather.humidity
                // timestamp automatikoki jartzen da
            )

            // Repositorioari deitu (Offline-first gordeko da)
            feedbackRepository.submitFeedback(feedback)

            // Hemen UI-ari abisatu genezake (adibidez, Snackbar bat erakusteko)
            // Baina oraingoz logean utziko dugu
            println("Feedback gordeta: $sensation")
        }
    }
}