package com.river.walklog.feature.recap

import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.MonthlyRecap
import org.junit.Test
import kotlin.test.assertEquals

class RecapExtensionsTest {

    // walkerPersonaRes

    @Test
    fun walkerPersonaRes_returnsPerfectWalker_whenAverageAtLeast10000() {
        assertEquals(R.string.persona_perfect_walker, recap(averageStepsPerDay = 10_000).walkerPersonaRes())
    }

    @Test
    fun walkerPersonaRes_returnsPerfectWalker_whenAverageAbove10000() {
        assertEquals(R.string.persona_perfect_walker, recap(averageStepsPerDay = 15_000).walkerPersonaRes())
    }

    @Test
    fun walkerPersonaRes_returnsSteadyAchiever_whenAverageBetween7000And9999() {
        assertEquals(R.string.persona_steady_achiever, recap(averageStepsPerDay = 7_000).walkerPersonaRes())
        assertEquals(R.string.persona_steady_achiever, recap(averageStepsPerDay = 9_999).walkerPersonaRes())
    }

    @Test
    fun walkerPersonaRes_returnsDiligentChallenger_whenAverageBetween5000And6999() {
        assertEquals(R.string.persona_diligent_challenger, recap(averageStepsPerDay = 5_000).walkerPersonaRes())
        assertEquals(R.string.persona_diligent_challenger, recap(averageStepsPerDay = 6_999).walkerPersonaRes())
    }

    @Test
    fun walkerPersonaRes_returnsPromisingBeginner_whenAverageBetween3000And4999() {
        assertEquals(R.string.persona_promising_beginner, recap(averageStepsPerDay = 3_000).walkerPersonaRes())
        assertEquals(R.string.persona_promising_beginner, recap(averageStepsPerDay = 4_999).walkerPersonaRes())
    }

    @Test
    fun walkerPersonaRes_returnsNewcomer_whenAverageBelow3000() {
        assertEquals(R.string.persona_newcomer, recap(averageStepsPerDay = 0).walkerPersonaRes())
        assertEquals(R.string.persona_newcomer, recap(averageStepsPerDay = 2_999).walkerPersonaRes())
    }

    // walkerPersonaDescRes

    @Test
    fun walkerPersonaDescRes_returnsPerfectWalkerDesc_whenAverageAtLeast10000() {
        assertEquals(R.string.persona_desc_perfect_walker, recap(averageStepsPerDay = 10_000).walkerPersonaDescRes())
    }

    @Test
    fun walkerPersonaDescRes_returnsSteadyAchieverDesc_whenAverageBetween7000And9999() {
        assertEquals(R.string.persona_desc_steady_achiever, recap(averageStepsPerDay = 7_500).walkerPersonaDescRes())
    }

    @Test
    fun walkerPersonaDescRes_returnsDiligentChallengerDesc_whenAverageBetween5000And6999() {
        assertEquals(R.string.persona_desc_diligent_challenger, recap(averageStepsPerDay = 5_500).walkerPersonaDescRes())
    }

    @Test
    fun walkerPersonaDescRes_returnsPromisingBeginnerDesc_whenAverageBetween3000And4999() {
        assertEquals(R.string.persona_desc_promising_beginner, recap(averageStepsPerDay = 4_000).walkerPersonaDescRes())
    }

    @Test
    fun walkerPersonaDescRes_returnsNewcomerDesc_whenAverageBelow3000() {
        assertEquals(R.string.persona_desc_newcomer, recap(averageStepsPerDay = 1_000).walkerPersonaDescRes())
    }

    private fun recap(averageStepsPerDay: Int) = MonthlyRecap(
        year = 2025,
        month = 3,
        totalSteps = averageStepsPerDay * 30,
        averageStepsPerDay = averageStepsPerDay,
        bestDay = null,
        achievedDays = 0,
        totalDays = 30,
        longestStreak = 0,
        activeDays = 0,
        estimatedCalories = 0,
        dailyCounts = emptyList(),
    )
}
