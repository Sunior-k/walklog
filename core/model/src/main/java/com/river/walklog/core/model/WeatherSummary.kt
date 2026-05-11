package com.river.walklog.core.model

data class WeatherSummary(
    val locationName: String,
    val temperatureCelsius: Int?,
    val condition: WeatherCondition,
    val precipitationProbability: Int?,
    val humidity: Int?,
    val windSpeedMetersPerSecond: Float?,
    val isAvailable: Boolean = true,
) {
    val temperatureText: String
        get() = temperatureCelsius?.let { "$it°" } ?: "-"

    companion object {
        fun unavailable(locationName: String = "서울"): WeatherSummary = WeatherSummary(
            locationName = locationName,
            temperatureCelsius = null,
            condition = WeatherCondition.UNKNOWN,
            precipitationProbability = null,
            humidity = null,
            windSpeedMetersPerSecond = null,
            isAvailable = false,
        )
    }
}

enum class WeatherCondition {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    RAIN,
    RAIN_SNOW,
    SNOW,
    SHOWER,
    UNKNOWN,
}
