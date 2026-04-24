package com.river.walklog.core.model

import org.junit.Test
import kotlin.test.assertEquals

class MissionTest {

    // ─── progressRatio ────────────────────────────────────────────────────

    @Test
    fun `progressRatio is currentSteps divided by targetSteps`() {
        val mission = mission(currentSteps = 3_000, targetSteps = 6_000)
        assertEquals(0.5f, mission.progressRatio)
    }

    @Test
    fun `progressRatio is 1f when currentSteps equals targetSteps`() {
        val mission = mission(currentSteps = 6_000, targetSteps = 6_000)
        assertEquals(1f, mission.progressRatio)
    }

    @Test
    fun `progressRatio is clamped to 1f when currentSteps exceed targetSteps`() {
        val mission = mission(currentSteps = 8_000, targetSteps = 6_000)
        assertEquals(1f, mission.progressRatio)
    }

    @Test
    fun `progressRatio is 0f when currentSteps is zero`() {
        val mission = mission(currentSteps = 0, targetSteps = 6_000)
        assertEquals(0f, mission.progressRatio)
    }

    @Test
    fun `progressRatio is 0f when targetSteps is zero`() {
        val mission = mission(currentSteps = 5_000, targetSteps = 0)
        assertEquals(0f, mission.progressRatio)
    }

    @Test
    fun `progressRatio is 0f when targetSteps is negative`() {
        val mission = mission(currentSteps = 5_000, targetSteps = -1)
        assertEquals(0f, mission.progressRatio)
    }

    // ─── Helper ───────────────────────────────────────────────────────────

    private fun mission(
        currentSteps: Int = 0,
        targetSteps: Int = 6_000,
    ) = Mission(
        id = 1L,
        type = MissionType.DAILY,
        title = "test",
        description = "test",
        targetSteps = targetSteps,
        currentSteps = currentSteps,
        rewardCash = 20,
        isCompleted = false,
        recommendedHour = 8,
    )
}
