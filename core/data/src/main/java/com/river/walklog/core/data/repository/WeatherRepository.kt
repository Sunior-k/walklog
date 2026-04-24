package com.river.walklog.core.data.repository

import com.river.walklog.core.model.WeatherSummary

interface WeatherRepository {
    suspend fun getCurrentWeather(forceRefresh: Boolean = false): WeatherSummary
}
