package com.river.walklog.feature.mission

import androidx.annotation.StringRes
import com.river.walklog.core.model.WeatherCondition
import com.river.walklog.core.model.WeatherSummary

@StringRes
fun WeatherCondition.conditionRes(): Int = when (this) {
    WeatherCondition.CLEAR -> R.string.weather_condition_clear
    WeatherCondition.PARTLY_CLOUDY -> R.string.weather_condition_partly_cloudy
    WeatherCondition.CLOUDY -> R.string.weather_condition_cloudy
    WeatherCondition.RAIN -> R.string.weather_condition_rain
    WeatherCondition.RAIN_SNOW -> R.string.weather_condition_rain_snow
    WeatherCondition.SNOW -> R.string.weather_condition_snow
    WeatherCondition.SHOWER -> R.string.weather_condition_shower
    WeatherCondition.UNKNOWN -> R.string.weather_condition_unknown
}

@StringRes
fun WeatherSummary.walkingAdviceRes(): Int {
    val wind = windSpeedMetersPerSecond
    val temp = temperatureCelsius
    return when {
        !isAvailable -> R.string.weather_advice_unavailable
        condition == WeatherCondition.SNOW -> R.string.weather_advice_snow
        condition == WeatherCondition.RAIN -> R.string.weather_advice_rain
        condition == WeatherCondition.SHOWER -> R.string.weather_advice_shower
        wind != null && wind >= 8f -> R.string.weather_advice_wind
        temp != null && temp <= 0 -> R.string.weather_advice_cold
        temp != null && temp >= 30 -> R.string.weather_advice_hot
        condition == WeatherCondition.CLEAR -> R.string.weather_advice_clear
        else -> R.string.weather_advice_default
    }
}
