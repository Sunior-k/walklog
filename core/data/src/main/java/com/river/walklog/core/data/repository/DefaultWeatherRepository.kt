package com.river.walklog.core.data.repository

import com.river.walklog.core.data.model.asExternalModel
import com.river.walklog.core.data.weather.WeatherLocationProvider
import com.river.walklog.core.model.WeatherSummary
import com.river.walklog.core.network.WeatherNetworkDataSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class DefaultWeatherRepository @Inject constructor(
    private val locationProvider: WeatherLocationProvider,
    private val dataSources: Map<String, @JvmSuppressWildcards WeatherNetworkDataSource>,
) : WeatherRepository {

    private val mutex = Mutex()
    private var cached: WeatherSummary? = null

    override suspend fun getCurrentWeather(forceRefresh: Boolean): WeatherSummary = mutex.withLock {
        cached?.takeUnless { forceRefresh }?.let { return@withLock it }

        val location = locationProvider.currentLocation()
        val source = dataSources[location.countryCode] ?: dataSources[WILDCARD_KEY]
            ?: return@withLock WeatherSummary.unavailable(location.displayName)

        val weather = source.getCurrentWeather(
            latitude = location.latitude,
            longitude = location.longitude,
            locationName = location.displayName,
        ).asExternalModel()

        if (weather.isAvailable) cached = weather
        cached ?: weather
    }

    companion object {
        private const val WILDCARD_KEY = "*"
    }
}
