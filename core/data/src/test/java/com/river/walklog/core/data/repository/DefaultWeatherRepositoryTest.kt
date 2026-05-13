package com.river.walklog.core.data.repository

import com.river.walklog.core.data.weather.WeatherLocation
import com.river.walklog.core.data.weather.WeatherLocationProvider
import com.river.walklog.core.model.WeatherCondition
import com.river.walklog.core.network.WeatherNetworkDataSource
import com.river.walklog.core.network.model.NetworkWeatherCondition
import com.river.walklog.core.network.model.NetworkWeatherSummary
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultWeatherRepositoryTest {

    private lateinit var locationProvider: WeatherLocationProvider
    private lateinit var krDataSource: WeatherNetworkDataSource
    private lateinit var wildcardDataSource: WeatherNetworkDataSource

    @Before
    fun setUp() {
        locationProvider = mockk()
        krDataSource = mockk()
        wildcardDataSource = mockk()
    }

    // 첫 번째 호출

    @Test
    fun `first call fetches weather from matched datasource`() = runTest {
        coEvery { locationProvider.currentLocation() } returns seoulLocation()
        coEvery { krDataSource.getCurrentWeather(any(), any(), any()) } returns availableNetwork()
        val repository = createRepository(mapOf("KR" to krDataSource))

        val result = repository.getCurrentWeather()

        assertTrue(result.isAvailable)
        assertEquals("서울", result.locationName)
    }

    @Test
    fun `fetched weather fields are mapped to domain model`() = runTest {
        coEvery { locationProvider.currentLocation() } returns seoulLocation()
        coEvery {
            krDataSource.getCurrentWeather(
                any(),
                any(),
                any(),
            )
        } returns NetworkWeatherSummary(
            locationName = "서울",
            temperatureCelsius = 18,
            condition = NetworkWeatherCondition.PARTLY_CLOUDY,
            precipitationProbability = 20,
            humidity = 55,
            windSpeedMetersPerSecond = 3.0f,
            isAvailable = true,
        )
        val repository = createRepository(mapOf("KR" to krDataSource))

        val result = repository.getCurrentWeather()

        assertEquals(18, result.temperatureCelsius)
        assertEquals(WeatherCondition.PARTLY_CLOUDY, result.condition)
        assertEquals(20, result.precipitationProbability)
    }

    // 캐시

    @Test
    fun `second call without forceRefresh returns cached result`() = runTest {
        coEvery { locationProvider.currentLocation() } returns seoulLocation()
        coEvery { krDataSource.getCurrentWeather(any(), any(), any()) } returns availableNetwork()
        val repository = createRepository(mapOf("KR" to krDataSource))

        repository.getCurrentWeather()
        repository.getCurrentWeather()

        coVerify(exactly = 1) { krDataSource.getCurrentWeather(any(), any(), any()) }
    }

    @Test
    fun `forceRefresh true bypasses cache and refetches`() = runTest {
        coEvery { locationProvider.currentLocation() } returns seoulLocation()
        coEvery { krDataSource.getCurrentWeather(any(), any(), any()) } returns availableNetwork()
        val repository = createRepository(mapOf("KR" to krDataSource))

        repository.getCurrentWeather(forceRefresh = false)
        repository.getCurrentWeather(forceRefresh = true)

        coVerify(exactly = 2) { krDataSource.getCurrentWeather(any(), any(), any()) }
    }

    @Test
    fun `unavailable weather is not cached and refetched on next call`() = runTest {
        coEvery { locationProvider.currentLocation() } returns seoulLocation()
        coEvery { krDataSource.getCurrentWeather(any(), any(), any()) } returns unavailableNetwork()
        val repository = createRepository(mapOf("KR" to krDataSource))

        repository.getCurrentWeather()
        repository.getCurrentWeather()

        coVerify(exactly = 2) { krDataSource.getCurrentWeather(any(), any(), any()) }
    }

    @Test
    fun `stale cache is returned when refetch returns unavailable`() = runTest {
        coEvery { locationProvider.currentLocation() } returns seoulLocation()
        coEvery { krDataSource.getCurrentWeather(any(), any(), any()) } returnsMany listOf(
            availableNetwork(temperatureCelsius = 20),
            unavailableNetwork(),
        )
        val repository = createRepository(mapOf("KR" to krDataSource))

        repository.getCurrentWeather()
        val result = repository.getCurrentWeather(forceRefresh = true)

        assertEquals(20, result.temperatureCelsius)
        assertTrue(result.isAvailable)
    }

    // 데이터소스 선택

    @Test
    fun `country-specific datasource is preferred over wildcard`() = runTest {
        coEvery { locationProvider.currentLocation() } returns seoulLocation()
        coEvery { krDataSource.getCurrentWeather(any(), any(), any()) } returns availableNetwork()
        val repository = createRepository(mapOf("KR" to krDataSource, "*" to wildcardDataSource))

        repository.getCurrentWeather()

        coVerify(exactly = 1) { krDataSource.getCurrentWeather(any(), any(), any()) }
        coVerify(exactly = 0) { wildcardDataSource.getCurrentWeather(any(), any(), any()) }
    }

    @Test
    fun `falls back to wildcard datasource when no country match`() = runTest {
        coEvery { locationProvider.currentLocation() } returns seoulLocation()
        coEvery {
            wildcardDataSource.getCurrentWeather(
                any(),
                any(),
                any(),
            )
        } returns availableNetwork()
        val repository = createRepository(mapOf("*" to wildcardDataSource))

        repository.getCurrentWeather()

        coVerify(exactly = 1) { wildcardDataSource.getCurrentWeather(any(), any(), any()) }
    }

    @Test
    fun `returns unavailable when no datasource matches`() = runTest {
        coEvery { locationProvider.currentLocation() } returns seoulLocation()
        val repository = createRepository(emptyMap())

        val result = repository.getCurrentWeather()

        assertFalse(result.isAvailable)
        assertEquals("서울", result.locationName)
    }

    @Test
    fun `location name is passed to datasource`() = runTest {
        val location = seoulLocation(displayName = "서울특별시")
        coEvery { locationProvider.currentLocation() } returns location
        coEvery {
            krDataSource.getCurrentWeather(
                any(),
                any(),
                eq("서울특별시"),
            )
        } returns availableNetwork()
        val repository = createRepository(mapOf("KR" to krDataSource))

        repository.getCurrentWeather()

        coVerify { krDataSource.getCurrentWeather(any(), any(), "서울특별시") }
    }

    // helper

    private fun createRepository(
        dataSources: Map<String, WeatherNetworkDataSource>,
    ) = DefaultWeatherRepository(locationProvider, dataSources)

    private fun seoulLocation(displayName: String = "서울") = WeatherLocation(
        latitude = 37.5665,
        longitude = 126.9780,
        displayName = displayName,
        countryCode = "KR",
    )

    private fun availableNetwork(
        locationName: String = "서울",
        temperatureCelsius: Int? = 20,
    ) = NetworkWeatherSummary(
        locationName = locationName,
        temperatureCelsius = temperatureCelsius,
        condition = NetworkWeatherCondition.CLEAR,
        precipitationProbability = 10,
        humidity = 50,
        windSpeedMetersPerSecond = 2.0f,
        isAvailable = true,
    )

    private fun unavailableNetwork(locationName: String = "서울") =
        NetworkWeatherSummary.unavailable(locationName)
}
