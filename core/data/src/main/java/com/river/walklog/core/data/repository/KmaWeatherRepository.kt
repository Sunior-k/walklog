package com.river.walklog.core.data.repository

import com.river.walklog.core.data.weather.WeatherLocationProvider
import com.river.walklog.core.model.WeatherCondition
import com.river.walklog.core.model.WeatherSummary
import com.river.walklog.core.network.WeatherNetworkDataSource
import com.river.walklog.core.network.model.NetworkWeatherCondition
import com.river.walklog.core.network.model.NetworkWeatherSummary
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class KmaWeatherRepository @Inject constructor(
    private val locationProvider: WeatherLocationProvider,
    private val weatherNetworkDataSource: WeatherNetworkDataSource,
) : WeatherRepository {
    private val weatherMutex = Mutex()
    private var cachedWeather: WeatherSummary? = null

    override suspend fun getCurrentWeather(forceRefresh: Boolean): WeatherSummary = weatherMutex.withLock {
        cachedWeather?.takeUnless { forceRefresh }?.let { return@withLock it }

        val location = locationProvider.currentLocation()
        val weather = weatherNetworkDataSource.getCurrentWeather(
            nx = location.nx,
            ny = location.ny,
            locationName = location.name,
        ).asExternalModel()

        if (weather.isAvailable) {
            cachedWeather = weather
        }
        return@withLock cachedWeather ?: weather
    }

    private fun NetworkWeatherSummary.asExternalModel(): WeatherSummary = WeatherSummary(
        locationName = locationName,
        temperatureCelsius = temperatureCelsius,
        condition = condition.asExternalModel(),
        precipitationProbability = precipitationProbability,
        humidity = humidity,
        windSpeedMetersPerSecond = windSpeedMetersPerSecond,
        isAvailable = isAvailable,
    )

    private fun NetworkWeatherCondition.asExternalModel(): WeatherCondition = when (this) {
        NetworkWeatherCondition.CLEAR -> WeatherCondition.CLEAR
        NetworkWeatherCondition.PARTLY_CLOUDY -> WeatherCondition.PARTLY_CLOUDY
        NetworkWeatherCondition.CLOUDY -> WeatherCondition.CLOUDY
        NetworkWeatherCondition.RAIN -> WeatherCondition.RAIN
        NetworkWeatherCondition.RAIN_SNOW -> WeatherCondition.RAIN_SNOW
        NetworkWeatherCondition.SNOW -> WeatherCondition.SNOW
        NetworkWeatherCondition.SHOWER -> WeatherCondition.SHOWER
        NetworkWeatherCondition.UNKNOWN -> WeatherCondition.UNKNOWN
    }
}
