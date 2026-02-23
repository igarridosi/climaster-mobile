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

@HiltViewModel // Hau ezinbestekoa da Hilt-ek jakiteko ViewModel bat dela
class DashboardViewModel @Inject constructor(
    private val getWeatherUseCase: GetWeatherUseCase
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
}