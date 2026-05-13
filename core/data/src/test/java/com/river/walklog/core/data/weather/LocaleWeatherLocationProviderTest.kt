package com.river.walklog.core.data.weather

import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals

class LocaleWeatherLocationProviderTest {

    private lateinit var savedLocale: Locale
    private lateinit var provider: LocaleWeatherLocationProvider

    @Before
    fun setUp() {
        savedLocale = Locale.getDefault()
        provider = LocaleWeatherLocationProvider()
    }

    @After
    fun tearDown() {
        Locale.setDefault(savedLocale)
    }

    // 국가 코드별 위치 매핑

    @Test
    fun `KR locale returns Seoul`() = runTest {
        Locale.setDefault(Locale("ko", "KR"))
        assertEquals(LocaleWeatherLocationProvider.SEOUL, provider.currentLocation())
    }

    @Test
    fun `JP locale returns Tokyo`() = runTest {
        Locale.setDefault(Locale("ja", "JP"))
        assertEquals(LocaleWeatherLocationProvider.TOKYO, provider.currentLocation())
    }

    @Test
    fun `US locale returns New York`() = runTest {
        Locale.setDefault(Locale("en", "US"))
        assertEquals(LocaleWeatherLocationProvider.NEW_YORK, provider.currentLocation())
    }

    @Test
    fun `unknown locale defaults to Seoul`() = runTest {
        Locale.setDefault(Locale("fr", "FR"))
        assertEquals(LocaleWeatherLocationProvider.SEOUL, provider.currentLocation())
    }

    // 상수 값 검증

    @Test
    fun `Seoul has correct coordinates`() {
        val seoul = LocaleWeatherLocationProvider.SEOUL
        assertEquals(37.5665, seoul.latitude)
        assertEquals(126.9780, seoul.longitude)
        assertEquals("KR", seoul.countryCode)
    }

    @Test
    fun `Tokyo has correct coordinates`() {
        val tokyo = LocaleWeatherLocationProvider.TOKYO
        assertEquals(35.6762, tokyo.latitude)
        assertEquals(139.6503, tokyo.longitude)
        assertEquals("JP", tokyo.countryCode)
    }

    @Test
    fun `New York has correct coordinates`() {
        val newYork = LocaleWeatherLocationProvider.NEW_YORK
        assertEquals(40.7128, newYork.latitude)
        assertEquals(-74.0060, newYork.longitude)
        assertEquals("US", newYork.countryCode)
    }
}
