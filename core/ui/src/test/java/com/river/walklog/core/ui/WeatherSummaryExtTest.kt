package com.river.walklog.core.ui

import com.river.walklog.core.model.WeatherCondition
import com.river.walklog.core.model.WeatherSummary
import org.junit.Test
import kotlin.test.assertEquals

class WeatherSummaryExtTest {

    @Test
    fun `temperatureText returns formatted value when present`() {
        assertEquals("25°", summary(temperatureCelsius = 25).temperatureText)
    }

    @Test
    fun `temperatureText returns dash when null`() {
        assertEquals("-", summary(temperatureCelsius = null).temperatureText)
    }

    @Test
    fun `temperatureText handles zero degrees`() {
        assertEquals("0°", summary(temperatureCelsius = 0).temperatureText)
    }

    @Test
    fun `temperatureText handles negative temperature`() {
        assertEquals("-5°", summary(temperatureCelsius = -5).temperatureText)
    }

    @Test
    fun `temperatureText uses integer value without decimal`() {
        assertEquals("20°", summary(temperatureCelsius = 20).temperatureText)
    }

    private fun summary(temperatureCelsius: Int?) = WeatherSummary(
        locationName = "Seoul",
        temperatureCelsius = temperatureCelsius,
        condition = WeatherCondition.CLEAR,
        precipitationProbability = null,
        humidity = null,
        windSpeedMetersPerSecond = null,
    )
}
