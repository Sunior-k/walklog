package com.river.walklog.core.data.model

import com.river.walklog.core.model.WeatherCondition
import com.river.walklog.core.model.WeatherSummary
import com.river.walklog.core.network.model.NetworkWeatherCondition
import com.river.walklog.core.network.model.NetworkWeatherSummary

fun NetworkWeatherSummary.asExternalModel(): WeatherSummary = WeatherSummary(
    locationName = locationName,
    temperatureCelsius = temperatureCelsius,
    condition = condition.asExternalModel(),
    precipitationProbability = precipitationProbability,
    humidity = humidity,
    windSpeedMetersPerSecond = windSpeedMetersPerSecond,
    isAvailable = isAvailable,
)

fun NetworkWeatherCondition.asExternalModel(): WeatherCondition = when (this) {
    NetworkWeatherCondition.CLEAR -> WeatherCondition.CLEAR
    NetworkWeatherCondition.PARTLY_CLOUDY -> WeatherCondition.PARTLY_CLOUDY
    NetworkWeatherCondition.CLOUDY -> WeatherCondition.CLOUDY
    NetworkWeatherCondition.RAIN -> WeatherCondition.RAIN
    NetworkWeatherCondition.RAIN_SNOW -> WeatherCondition.RAIN_SNOW
    NetworkWeatherCondition.SNOW -> WeatherCondition.SNOW
    NetworkWeatherCondition.SHOWER -> WeatherCondition.SHOWER
    NetworkWeatherCondition.UNKNOWN -> WeatherCondition.UNKNOWN
}
