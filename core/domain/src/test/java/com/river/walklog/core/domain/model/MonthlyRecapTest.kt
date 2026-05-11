package com.river.walklog.core.domain.model

import com.river.walklog.core.model.MonthlyRecap
import org.junit.Test
import kotlin.test.assertEquals

class MonthlyRecapTest {

    private fun recap(
        totalSteps: Int = 0,
        averageStepsPerDay: Int = 0,
        achievedDays: Int = 0,
        totalDays: Int = 30,
        estimatedCalories: Int = 0,
    ) = MonthlyRecap(
        year = 2025,
        month = 3,
        totalSteps = totalSteps,
        averageStepsPerDay = averageStepsPerDay,
        bestDay = null,
        achievedDays = achievedDays,
        totalDays = totalDays,
        longestStreak = 0,
        activeDays = 0,
        estimatedCalories = estimatedCalories,
        dailyCounts = emptyList(),
    )

    // achievementRate

    @Test
    fun `achievementRate is achievedDays divided by totalDays`() {
        val r = recap(achievedDays = 15, totalDays = 30)
        assertEquals(0.5f, r.achievementRate)
    }

    @Test
    fun `achievementRate is 0f when totalDays is zero`() {
        val r = recap(achievedDays = 0, totalDays = 0)
        assertEquals(0f, r.achievementRate)
    }

    @Test
    fun `achievementRate is 1f when all days are achieved`() {
        val r = recap(achievedDays = 30, totalDays = 30)
        assertEquals(1f, r.achievementRate)
    }

    // distanceKm

    @Test
    fun `distanceKm uses 0_75m stride length`() {
        // 10000 steps × 0.00075 km = 7.5 → truncated to 7
        assertEquals(7, recap(totalSteps = 10_000).distanceKm)
    }

    @Test
    fun `distanceKm is zero for no steps`() {
        assertEquals(0, recap(totalSteps = 0).distanceKm)
    }
}
