package com.river.walklog.feature.home

import com.river.walklog.core.model.WeatherCondition
import com.river.walklog.core.model.WeatherSummary
import org.junit.Test
import kotlin.test.assertEquals

class WeatherExtensionsTest {

    // conditionRes

    @Test
    fun conditionRes_returnsCorrectStringId_forClear() {
        assertEquals(R.string.weather_condition_clear, WeatherCondition.CLEAR.conditionRes())
    }

    @Test
    fun conditionRes_returnsCorrectStringId_forPartlyCloudy() {
        assertEquals(R.string.weather_condition_partly_cloudy, WeatherCondition.PARTLY_CLOUDY.conditionRes())
    }

    @Test
    fun conditionRes_returnsCorrectStringId_forCloudy() {
        assertEquals(R.string.weather_condition_cloudy, WeatherCondition.CLOUDY.conditionRes())
    }

    @Test
    fun conditionRes_returnsCorrectStringId_forRain() {
        assertEquals(R.string.weather_condition_rain, WeatherCondition.RAIN.conditionRes())
    }

    @Test
    fun conditionRes_returnsCorrectStringId_forRainSnow() {
        assertEquals(R.string.weather_condition_rain_snow, WeatherCondition.RAIN_SNOW.conditionRes())
    }

    @Test
    fun conditionRes_returnsCorrectStringId_forSnow() {
        assertEquals(R.string.weather_condition_snow, WeatherCondition.SNOW.conditionRes())
    }

    @Test
    fun conditionRes_returnsCorrectStringId_forShower() {
        assertEquals(R.string.weather_condition_shower, WeatherCondition.SHOWER.conditionRes())
    }

    @Test
    fun conditionRes_returnsCorrectStringId_forUnknown() {
        assertEquals(R.string.weather_condition_unknown, WeatherCondition.UNKNOWN.conditionRes())
    }

    // walkingAdviceRes — unavailable

    @Test
    fun walkingAdviceRes_returnsUnavailable_whenNotAvailable() {
        assertEquals(R.string.weather_advice_unavailable, WeatherSummary.unavailable().walkingAdviceRes())
    }

    // walkingAdviceRes — snow, rain, shower (checked before wind/temp)

    @Test
    fun walkingAdviceRes_returnsSnow_whenConditionIsSnow() {
        assertEquals(R.string.weather_advice_snow, weather(condition = WeatherCondition.SNOW).walkingAdviceRes())
    }

    @Test
    fun walkingAdviceRes_returnsRain_whenConditionIsRain() {
        assertEquals(R.string.weather_advice_rain, weather(condition = WeatherCondition.RAIN).walkingAdviceRes())
    }

    @Test
    fun walkingAdviceRes_returnsShower_whenConditionIsShower() {
        assertEquals(R.string.weather_advice_shower, weather(condition = WeatherCondition.SHOWER).walkingAdviceRes())
    }

    // walkingAdviceRes — wind (>= 8 m/s)

    @Test
    fun walkingAdviceRes_returnsWind_whenWindAtLeast8() {
        assertEquals(R.string.weather_advice_wind, weather(wind = 8f).walkingAdviceRes())
    }

    @Test
    fun walkingAdviceRes_returnsWind_whenWindAbove8() {
        assertEquals(R.string.weather_advice_wind, weather(wind = 12f).walkingAdviceRes())
    }

    @Test
    fun walkingAdviceRes_doesNotReturnWind_whenWindBelow8() {
        val advice = weather(wind = 7.9f, temp = 15).walkingAdviceRes()
        assert(advice != R.string.weather_advice_wind)
    }

    // walkingAdviceRes — cold (<= 0 °C)

    @Test
    fun walkingAdviceRes_returnsCold_whenTempIsZero() {
        assertEquals(R.string.weather_advice_cold, weather(temp = 0).walkingAdviceRes())
    }

    @Test
    fun walkingAdviceRes_returnsCold_whenTempBelowZero() {
        assertEquals(R.string.weather_advice_cold, weather(temp = -5).walkingAdviceRes())
    }

    // walkingAdviceRes — hot (>= 30 °C)

    @Test
    fun walkingAdviceRes_returnsHot_whenTempIs30() {
        assertEquals(R.string.weather_advice_hot, weather(temp = 30).walkingAdviceRes())
    }

    @Test
    fun walkingAdviceRes_returnsHot_whenTempAbove30() {
        assertEquals(R.string.weather_advice_hot, weather(temp = 35).walkingAdviceRes())
    }

    // walkingAdviceRes — clear (after wind/cold/hot checks pass)

    @Test
    fun walkingAdviceRes_returnsClear_whenClearAndMildWeather() {
        assertEquals(R.string.weather_advice_clear, weather(condition = WeatherCondition.CLEAR, temp = 20).walkingAdviceRes())
    }

    // walkingAdviceRes — default

    @Test
    fun walkingAdviceRes_returnsDefault_forCloudyMildWeather() {
        assertEquals(R.string.weather_advice_default, weather(condition = WeatherCondition.CLOUDY, temp = 20).walkingAdviceRes())
    }

    @Test
    fun walkingAdviceRes_returnsDefault_forPartlyCloudyMildWeather() {
        assertEquals(R.string.weather_advice_default, weather(condition = WeatherCondition.PARTLY_CLOUDY, temp = 15).walkingAdviceRes())
    }

    private fun weather(
        condition: WeatherCondition = WeatherCondition.CLEAR,
        temp: Int? = 20,
        wind: Float? = null,
    ) = WeatherSummary(
        locationName = "Seoul",
        temperatureCelsius = temp,
        condition = condition,
        precipitationProbability = null,
        humidity = null,
        windSpeedMetersPerSecond = wind,
        isAvailable = true,
    )
}
