package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.model.DailyStepCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetCurrentStreakUseCase @Inject constructor(
    private val stepRepository: StepRepository,
) {
    operator fun invoke(today: LocalDate = LocalDate.now()): Flow<Int> {
        val start = today.withDayOfMonth(1)
        return stepRepository.getStepCountsForRange(
            fromEpochDay = start.toEpochDay(),
            toEpochDay = today.toEpochDay(),
        ).map { dailyCounts -> compute(dailyCounts, today) }
    }

    private fun compute(dailyCounts: List<DailyStepCount>, today: LocalDate): Int {
        val todayEpochDay = today.toEpochDay()
        val countsByDay = dailyCounts.associateBy { it.dateEpochDay }
        var day = todayEpochDay
        if (countsByDay[day]?.let { it.steps >= it.targetSteps } != true) {
            day--
        }
        var streak = 0
        while (countsByDay[day]?.let { it.steps >= it.targetSteps } == true) {
            streak++
            day--
        }
        return streak
    }
}
