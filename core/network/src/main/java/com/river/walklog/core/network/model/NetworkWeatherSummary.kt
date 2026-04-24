package com.river.walklog.core.network.model

data class NetworkWeatherSummary(
    val locationName: String,
    val temperatureCelsius: Int?,
    val condition: NetworkWeatherCondition,
    val precipitationProbability: Int?,
    val humidity: Int?,
    val windSpeedMetersPerSecond: Float?,
    val isAvailable: Boolean = true,
) {
    companion object {
        fun unavailable(locationName: String): NetworkWeatherSummary = NetworkWeatherSummary(
            locationName = locationName,
            temperatureCelsius = null,
            condition = NetworkWeatherCondition.UNKNOWN,
            precipitationProbability = null,
            humidity = null,
            windSpeedMetersPerSecond = null,
            isAvailable = false,
        )
    }
}

enum class NetworkWeatherCondition {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    RAIN,
    RAIN_SNOW,
    SNOW,
    SHOWER,
    UNKNOWN,
}
