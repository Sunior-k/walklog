package com.river.walklog.core.network

import com.river.walklog.core.network.model.NetworkWeatherSummary

interface WeatherNetworkDataSource {
    suspend fun getCurrentWeather(
        nx: Int,
        ny: Int,
        locationName: String,
    ): NetworkWeatherSummary
}
