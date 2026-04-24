package com.river.walklog.core.domain.model

import com.river.walklog.core.model.DailyStepCount
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DailyStepCountTest {

    @Test
    fun `isAchieved is true when steps equal targetSteps`() {
        val count = DailyStepCount(dateEpochDay = 0L, steps = 6_000, targetSteps = 6_000)
        assertTrue(count.isAchieved)
    }

    @Test
    fun `isAchieved is true when steps exceed targetSteps`() {
        val count = DailyStepCount(dateEpochDay = 0L, steps = 8_000, targetSteps = 6_000)
        assertTrue(count.isAchieved)
    }

    @Test
    fun `isAchieved is false when steps are below targetSteps`() {
        val count = DailyStepCount(dateEpochDay = 0L, steps = 5_999, targetSteps = 6_000)
        assertFalse(count.isAchieved)
    }

    @Test
    fun `default targetSteps is DEFAULT_TARGET_STEPS`() {
        val count = DailyStepCount(dateEpochDay = 0L, steps = 0)
        assertEquals(DailyStepCount.DEFAULT_TARGET_STEPS, count.targetSteps)
    }

    @Test
    fun `DEFAULT_TARGET_STEPS constant is 6000`() {
        assertEquals(6_000, DailyStepCount.DEFAULT_TARGET_STEPS)
    }
}
