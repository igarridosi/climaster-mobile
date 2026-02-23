package com.climaster.domain.usecase

import com.climaster.core.util.Resource
import com.climaster.domain.model.Weather
import com.climaster.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
// Geroago hemen @Inject jarriko dugu, baina oraingoz horrela utziko dugu
class GetWeatherUseCase(
    private val repository: WeatherRepository
) {
    // 'invoke' operadoreari esker, klasea funtzio bat bezala deitu daiteke:
    // val result = getWeatherUseCase(lat, lon)
    operator fun invoke(lat: Double, lon: Double): Flow<Resource<Weather>> {
        // Hemen negozio logika gehigarria jarri genezake (adib. koordenatuak balidatu)
        return repository.getWeather(lat, lon)
    }
}