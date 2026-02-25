package com.climaster.domain.model

enum class WeatherCondition {
    EGUZKITSUA,       // Eguzki garbia, 'clear-day'
    HODEITARTEAK,    // Hodei gutxi, eguzkia ere ikusten da, 'partly-cloudy-day'
    HODEITSUA,        // Zeru guztiz estalia, 'cloudy'
    EURITSUA,         // Euria, 'rain'
    EURIELURRA,      // Elur bustia, 'sleet'
    EKAITSUA,         // Trumoiak, tximistak, 'thunderstorm'
    ELURTSUA,         // Elurra, 'snow'
    HAIZETSUA,        // Haizea da protagonista, 'wind'
    LAINOTSUA,        // Behe-lainoa, 'fog'
    EZEZAGUNA         // Mapatu ezin izan den egoera, segurtasunagatik
}