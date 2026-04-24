package com.river.walklog.core.domain.model

import com.river.walklog.core.model.MonthlyRecap
import org.junit.Test
import kotlin.test.assertEquals

class MonthlyRecapTest {

    // ─── Fixture ───────────────────────────────────────────────────────────

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

    // ─── achievementRate ───────────────────────────────────────────────────

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

    // ─── walkerPersona ─────────────────────────────────────────────────────

    @Test
    fun `walkerPersona is 완벽한 워커 for avg 10000 or more`() {
        assertEquals("완벽한 워커", recap(averageStepsPerDay = 10_000).walkerPersona)
        assertEquals("완벽한 워커", recap(averageStepsPerDay = 15_000).walkerPersona)
    }

    @Test
    fun `walkerPersona is 꾸준한 달성자 for avg 7000 to 9999`() {
        assertEquals("꾸준한 달성자", recap(averageStepsPerDay = 7_000).walkerPersona)
        assertEquals("꾸준한 달성자", recap(averageStepsPerDay = 9_999).walkerPersona)
    }

    @Test
    fun `walkerPersona is 성실한 도전자 for avg 5000 to 6999`() {
        assertEquals("성실한 도전자", recap(averageStepsPerDay = 5_000).walkerPersona)
        assertEquals("성실한 도전자", recap(averageStepsPerDay = 6_999).walkerPersona)
    }

    @Test
    fun `walkerPersona is 가능성 있는 시작자 for avg 3000 to 4999`() {
        assertEquals("가능성 있는 시작자", recap(averageStepsPerDay = 3_000).walkerPersona)
        assertEquals("가능성 있는 시작자", recap(averageStepsPerDay = 4_999).walkerPersona)
    }

    @Test
    fun `walkerPersona is 걷기 입문자 for avg below 3000`() {
        assertEquals("걷기 입문자", recap(averageStepsPerDay = 0).walkerPersona)
        assertEquals("걷기 입문자", recap(averageStepsPerDay = 2_999).walkerPersona)
    }

    // ─── distanceKm ────────────────────────────────────────────────────────

    @Test
    fun `distanceKm uses 0_75m stride length`() {
        // 10000 steps × 0.00075 km = 7.5 → truncated to 7
        assertEquals(7, recap(totalSteps = 10_000).distanceKm)
    }

    @Test
    fun `distanceKm is zero for no steps`() {
        assertEquals(0, recap(totalSteps = 0).distanceKm)
    }

    // ─── monthLabel ────────────────────────────────────────────────────────

    @Test
    fun `monthLabel formats as N월`() {
        assertEquals("3월", recap().monthLabel)
    }
}
