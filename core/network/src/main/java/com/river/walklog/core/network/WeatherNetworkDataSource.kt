package com.river.walklog.core.network

import com.river.walklog.core.network.model.NetworkWeatherSummary

interface WeatherNetworkDataSource {
    suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double,
        locationName: String,
    ): NetworkWeatherSummary
}
