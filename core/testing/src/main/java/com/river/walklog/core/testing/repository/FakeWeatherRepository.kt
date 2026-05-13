package com.river.walklog.core.testing.repository

import com.river.walklog.core.data.repository.WeatherRepository
import com.river.walklog.core.model.WeatherSummary

class FakeWeatherRepository(
    initialWeather: WeatherSummary = WeatherSummary.unavailable(),
) : WeatherRepository {

    var forceRefreshRequests = 0
        private set

    private var weather = initialWeather

    override suspend fun getCurrentWeather(forceRefresh: Boolean): WeatherSummary {
        if (forceRefresh) forceRefreshRequests++
        return weather
    }

    fun setWeather(weather: WeatherSummary) {
        this.weather = weather
    }
}
