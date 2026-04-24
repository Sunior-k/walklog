package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.MonthlyRecap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetMonthlyRecapUseCase @Inject constructor(
    private val stepRepository: StepRepository,
) {
    operator fun invoke(year: Int, month: Int): Flow<MonthlyRecap> {
        val start = LocalDate.of(year, month, 1)
        val end = start.plusMonths(1).minusDays(1)
        return stepRepository.getStepCountsForRange(
            fromEpochDay = start.toEpochDay(),
            toEpochDay = end.toEpochDay(),
        ).map { dailyCounts -> buildRecap(year, month, dailyCounts) }
    }

    private fun buildRecap(year: Int, month: Int, dailyCounts: List<DailyStepCount>): MonthlyRecap {
        val totalSteps = dailyCounts.sumOf { it.steps }
        val activeDays = dailyCounts.count { it.steps > 0 }
        val averageStepsPerDay = if (activeDays > 0) totalSteps / activeDays else 0
        val achievedDays = dailyCounts.count { it.isAchieved }
        val bestDay = dailyCounts.maxByOrNull { it.steps }?.takeIf { it.steps > 0 }
        val longestStreak = computeLongestStreak(dailyCounts)
        val estimatedCalories = (totalSteps * 0.04f).toInt()

        return MonthlyRecap(
            year = year,
            month = month,
            totalSteps = totalSteps,
            averageStepsPerDay = averageStepsPerDay,
            bestDay = bestDay,
            achievedDays = achievedDays,
            totalDays = dailyCounts.size,
            longestStreak = longestStreak,
            activeDays = activeDays,
            estimatedCalories = estimatedCalories,
            dailyCounts = dailyCounts,
        )
    }

    private fun computeLongestStreak(dailyCounts: List<DailyStepCount>): Int {
        var longest = 0
        var current = 0
        for (day in dailyCounts) {
            if (day.isAchieved) {
                current++
                if (current > longest) longest = current
            } else {
                current = 0
            }
        }
        return longest
    }
}
