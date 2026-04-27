package com.river.walklog.core.domain.model

import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.WeeklyStepSummary
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WeeklyStepSummaryTest {

    // ─── Fixtures ──────────────────────────────────────────────────────────

    private fun stepCount(
        dateOffset: Long = 0L,
        steps: Int = 0,
        targetSteps: Int = 6_000,
    ) = DailyStepCount(
        dateEpochDay = 19_000L + dateOffset,
        steps = steps,
        targetSteps = targetSteps,
    )

    // ─── totalSteps ────────────────────────────────────────────────────────

    @Test
    fun `totalSteps is sum of all daily steps`() {
        val summary = WeeklyStepSummary(
            weekStartEpochDay = 19_000L,
            dailyCounts = listOf(
                stepCount(0, steps = 3_000),
                stepCount(1, steps = 5_000),
                stepCount(2, steps = 2_000),
            ),
        )
        assertEquals(10_000, summary.totalSteps)
    }

    @Test
    fun `totalSteps is zero when all days have no steps`() {
        val summary = WeeklyStepSummary(
            weekStartEpochDay = 19_000L,
            dailyCounts = listOf(stepCount(steps = 0), stepCount(steps = 0)),
        )
        assertEquals(0, summary.totalSteps)
    }

    @Test
    fun `totalSteps is zero for empty week`() {
        val summary = WeeklyStepSummary.empty(weekStartEpochDay = 19_000L)
        assertEquals(0, summary.totalSteps)
    }

    // ─── averageSteps ──────────────────────────────────────────────────────

    @Test
    fun `averageSteps divides totalSteps by number of days`() {
        val summary = WeeklyStepSummary(
            weekStartEpochDay = 19_000L,
            dailyCounts = listOf(
                stepCount(steps = 4_000),
                stepCount(steps = 8_000),
            ),
        )
        assertEquals(6_000, summary.averageSteps)
    }

    @Test
    fun `averageSteps is zero for empty week`() {
        val summary = WeeklyStepSummary.empty(weekStartEpochDay = 19_000L)
        assertEquals(0, summary.averageSteps)
    }

    @Test
    fun `averageSteps truncates fractional result`() {
        val summary = WeeklyStepSummary(
            weekStartEpochDay = 19_000L,
            dailyCounts = listOf(
                stepCount(steps = 1_000),
                stepCount(steps = 1_001),
                stepCount(steps = 1_002),
            ),
        )
        // 3003 / 3 = 1001
        assertEquals(1_001, summary.averageSteps)
    }

    // ─── achievementRate ───────────────────────────────────────────────────

    @Test
    fun `achievementRate is ratio of achieved days to total days`() {
        val summary = WeeklyStepSummary(
            weekStartEpochDay = 19_000L,
            dailyCounts = listOf(
                stepCount(steps = 6_000, targetSteps = 6_000), // achieved
                stepCount(steps = 7_000, targetSteps = 6_000), // achieved
                stepCount(steps = 5_999, targetSteps = 6_000), // not achieved
                stepCount(steps = 0, targetSteps = 6_000), // not achieved
            ),
        )
        assertEquals(0.5f, summary.achievementRate)
    }

    @Test
    fun `achievementRate is 1f when all days are achieved`() {
        val summary = WeeklyStepSummary(
            weekStartEpochDay = 19_000L,
            dailyCounts = listOf(
                stepCount(steps = 6_000, targetSteps = 6_000),
                stepCount(steps = 6_000, targetSteps = 6_000),
            ),
        )
        assertEquals(1f, summary.achievementRate)
    }

    @Test
    fun `achievementRate is 0f for empty week`() {
        val summary = WeeklyStepSummary.empty(weekStartEpochDay = 19_000L)
        assertEquals(0f, summary.achievementRate)
    }

    // ─── bestDay ───────────────────────────────────────────────────────────

    @Test
    fun `bestDay returns day with most steps`() {
        val best = stepCount(dateOffset = 2, steps = 12_000)
        val summary = WeeklyStepSummary(
            weekStartEpochDay = 19_000L,
            dailyCounts = listOf(
                stepCount(dateOffset = 0, steps = 3_000),
                stepCount(dateOffset = 1, steps = 7_000),
                best,
            ),
        )
        assertEquals(best, summary.bestDay)
    }

    @Test
    fun `bestDay is null for empty week`() {
        val summary = WeeklyStepSummary.empty(weekStartEpochDay = 19_000L)
        assertNull(summary.bestDay)
    }

    // ─── longestAchievedStreak ─────────────────────────────────────────────

    @Test
    fun `longestAchievedStreak is max consecutive achieved days`() {
        val summary = WeeklyStepSummary(
            weekStartEpochDay = 19_000L,
            dailyCounts = listOf(
                stepCount(0, steps = 6_000),
                stepCount(1, steps = 7_000),
                stepCount(2, steps = 0),
                stepCount(3, steps = 6_000),
                stepCount(4, steps = 6_000),
                stepCount(5, steps = 6_000),
                stepCount(6, steps = 3_000),
            ),
        )

        assertEquals(3, summary.longestAchievedStreak)
    }

    @Test
    fun `longestAchievedStreak is zero for empty week`() {
        val summary = WeeklyStepSummary.empty(weekStartEpochDay = 19_000L)
        assertEquals(0, summary.longestAchievedStreak)
    }
}
