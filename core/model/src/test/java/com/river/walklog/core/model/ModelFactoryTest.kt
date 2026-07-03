package com.river.walklog.core.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ModelFactoryTest {

    // WeatherSummary.unavailable

    @Test
    fun `WeatherSummary_unavailable sets isAvailable to false`() {
        assertFalse(WeatherSummary.unavailable().isAvailable)
    }

    @Test
    fun `WeatherSummary_unavailable sets condition to UNKNOWN`() {
        assertEquals(WeatherCondition.UNKNOWN, WeatherSummary.unavailable().condition)
    }

    @Test
    fun `WeatherSummary_unavailable sets all numeric fields to null`() {
        val summary = WeatherSummary.unavailable()
        assertNull(summary.temperatureCelsius)
        assertNull(summary.precipitationProbability)
        assertNull(summary.humidity)
        assertNull(summary.windSpeedMetersPerSecond)
    }

    @Test
    fun `WeatherSummary_unavailable uses provided locationName`() {
        assertEquals("Seoul", WeatherSummary.unavailable("Seoul").locationName)
    }

    @Test
    fun `WeatherSummary_unavailable uses empty locationName by default`() {
        assertEquals("", WeatherSummary.unavailable().locationName)
    }

    // WeeklyReportDetail.empty

    @Test
    fun `WeeklyReportDetail_empty sets isEmpty to true`() {
        assertTrue(WeeklyReportDetail.empty(0L).isEmpty)
    }

    @Test
    fun `WeeklyReportDetail_empty sets totalSteps to 0`() {
        assertEquals(0, WeeklyReportDetail.empty(0L).totalSteps)
    }

    @Test
    fun `WeeklyReportDetail_empty sets achievedDays to 0`() {
        assertEquals(0, WeeklyReportDetail.empty(0L).achievedDays)
    }

    @Test
    fun `WeeklyReportDetail_empty sets totalDays to 7`() {
        assertEquals(7, WeeklyReportDetail.empty(0L).totalDays)
    }

    @Test
    fun `WeeklyReportDetail_empty preserves weekStartEpochDay`() {
        assertEquals(19_000L, WeeklyReportDetail.empty(19_000L).weekStartEpochDay)
    }

    @Test
    fun `WeeklyReportDetail_empty sets bestDayEpochDay to null`() {
        assertNull(WeeklyReportDetail.empty(0L).bestDayEpochDay)
    }

    @Test
    fun `WeeklyReportDetail_empty sets bestHour to null`() {
        assertNull(WeeklyReportDetail.empty(0L).bestHour)
    }

    @Test
    fun `WeeklyReportDetail_empty uses provided weekCounts`() {
        val counts = listOf(DailyStepCount(dateEpochDay = 0L, steps = 1_000))
        assertEquals(counts, WeeklyReportDetail.empty(0L, counts).weekCounts)
    }

    // DailyStepCount defaults

    @Test
    fun `DailyStepCount_DEFAULT_TARGET_STEPS is 6000`() {
        assertEquals(6_000, DailyStepCount.DEFAULT_TARGET_STEPS)
    }

    @Test
    fun `DailyStepCount uses DEFAULT_TARGET_STEPS when not specified`() {
        assertEquals(DailyStepCount.DEFAULT_TARGET_STEPS, DailyStepCount(0L, 1_000).targetSteps)
    }

    // WeatherCondition enum coverage

    @Test
    fun `WeatherCondition contains all expected values`() {
        val names = WeatherCondition.values().map { it.name }
        assertTrue("CLEAR" in names)
        assertTrue("PARTLY_CLOUDY" in names)
        assertTrue("CLOUDY" in names)
        assertTrue("RAIN" in names)
        assertTrue("RAIN_SNOW" in names)
        assertTrue("SNOW" in names)
        assertTrue("SHOWER" in names)
        assertTrue("UNKNOWN" in names)
    }

    // ThemeMode enum coverage

    @Test
    fun `ThemeMode contains SYSTEM LIGHT DARK`() {
        val names = ThemeMode.values().map { it.name }
        assertTrue("SYSTEM" in names)
        assertTrue("LIGHT" in names)
        assertTrue("DARK" in names)
    }
}
