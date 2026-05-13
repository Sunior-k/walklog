package com.river.walklog.core.data.model

import com.river.walklog.core.model.WeatherCondition
import com.river.walklog.core.network.model.NetworkWeatherCondition
import com.river.walklog.core.network.model.NetworkWeatherSummary
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NetworkWeatherSummaryMappingTest {

    // asExternalModel - 필드 매핑

    @Test
    fun `asExternalModel maps locationName`() {
        val result = network(locationName = "서울").asExternalModel()
        assertEquals("서울", result.locationName)
    }

    @Test
    fun `asExternalModel maps temperatureCelsius`() {
        val result = network(temperatureCelsius = 25).asExternalModel()
        assertEquals(25, result.temperatureCelsius)
    }

    @Test
    fun `asExternalModel maps null temperatureCelsius`() {
        val result = network(temperatureCelsius = null).asExternalModel()
        assertNull(result.temperatureCelsius)
    }

    @Test
    fun `asExternalModel maps precipitationProbability`() {
        val result = network(precipitationProbability = 30).asExternalModel()
        assertEquals(30, result.precipitationProbability)
    }

    @Test
    fun `asExternalModel maps humidity`() {
        val result = network(humidity = 70).asExternalModel()
        assertEquals(70, result.humidity)
    }

    @Test
    fun `asExternalModel maps windSpeedMetersPerSecond`() {
        val result = network(windSpeedMetersPerSecond = 4.5f).asExternalModel()
        assertEquals(4.5f, result.windSpeedMetersPerSecond)
    }

    @Test
    fun `asExternalModel maps isAvailable true`() {
        val result = network(isAvailable = true).asExternalModel()
        assertTrue(result.isAvailable)
    }

    @Test
    fun `asExternalModel maps isAvailable false`() {
        val result = network(isAvailable = false).asExternalModel()
        assertFalse(result.isAvailable)
    }

    // 날씨 상태 매핑

    @Test
    fun `CLEAR maps to WeatherCondition CLEAR`() {
        assertEquals(WeatherCondition.CLEAR, NetworkWeatherCondition.CLEAR.asExternalModel())
    }

    @Test
    fun `PARTLY_CLOUDY maps to WeatherCondition PARTLY_CLOUDY`() {
        assertEquals(
            WeatherCondition.PARTLY_CLOUDY,
            NetworkWeatherCondition.PARTLY_CLOUDY.asExternalModel(),
        )
    }

    @Test
    fun `CLOUDY maps to WeatherCondition CLOUDY`() {
        assertEquals(WeatherCondition.CLOUDY, NetworkWeatherCondition.CLOUDY.asExternalModel())
    }

    @Test
    fun `RAIN maps to WeatherCondition RAIN`() {
        assertEquals(WeatherCondition.RAIN, NetworkWeatherCondition.RAIN.asExternalModel())
    }

    @Test
    fun `RAIN_SNOW maps to WeatherCondition RAIN_SNOW`() {
        assertEquals(
            WeatherCondition.RAIN_SNOW,
            NetworkWeatherCondition.RAIN_SNOW.asExternalModel(),
        )
    }

    @Test
    fun `SNOW maps to WeatherCondition SNOW`() {
        assertEquals(WeatherCondition.SNOW, NetworkWeatherCondition.SNOW.asExternalModel())
    }

    @Test
    fun `SHOWER maps to WeatherCondition SHOWER`() {
        assertEquals(WeatherCondition.SHOWER, NetworkWeatherCondition.SHOWER.asExternalModel())
    }

    @Test
    fun `UNKNOWN maps to WeatherCondition UNKNOWN`() {
        assertEquals(WeatherCondition.UNKNOWN, NetworkWeatherCondition.UNKNOWN.asExternalModel())
    }

    @Test
    fun `all NetworkWeatherCondition values have a corresponding mapping`() {
        NetworkWeatherCondition.entries.forEach { networkCondition ->
            val domainCondition = networkCondition.asExternalModel()
            assertEquals(networkCondition.name, domainCondition.name)
        }
    }

    // unavailable factory

    @Test
    fun `unavailable network maps to unavailable domain`() {
        val result = NetworkWeatherSummary.unavailable("도쿄").asExternalModel()
        assertFalse(result.isAvailable)
        assertEquals("도쿄", result.locationName)
        assertEquals(WeatherCondition.UNKNOWN, result.condition)
        assertNull(result.temperatureCelsius)
    }

    // helper

    private fun network(
        locationName: String = "서울",
        temperatureCelsius: Int? = 20,
        condition: NetworkWeatherCondition = NetworkWeatherCondition.CLEAR,
        precipitationProbability: Int? = 10,
        humidity: Int? = 60,
        windSpeedMetersPerSecond: Float? = 2.0f,
        isAvailable: Boolean = true,
    ) = NetworkWeatherSummary(
        locationName = locationName,
        temperatureCelsius = temperatureCelsius,
        condition = condition,
        precipitationProbability = precipitationProbability,
        humidity = humidity,
        windSpeedMetersPerSecond = windSpeedMetersPerSecond,
        isAvailable = isAvailable,
    )
}
