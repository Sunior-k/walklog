package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.model.WeeklyArchiveSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class GetWeeklyReportArchiveUseCase @Inject constructor(
    private val stepRepository: StepRepository,
) {
    operator fun invoke(
        anchorDate: LocalDate = LocalDate.now(),
        weekCount: Int = DEFAULT_WEEK_COUNT,
    ): Flow<List<WeeklyArchiveSummary>> {
        val currentWeekStart = anchorDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekStarts = (0 until weekCount.coerceAtLeast(1)).map { index ->
            currentWeekStart.minusWeeks(index.toLong())
        }
        val flows = weekStarts.map { weekStart ->
            stepRepository.getWeeklyStepSummary(weekStart.toEpochDay())
        }

        return combine(flows) { summaries ->
            summaries.mapIndexed { index, summary ->
                val weekStart = weekStarts[index]
                val isLocked = weekStart == currentWeekStart
                val totalSteps = summary.dailyCounts.sumOf { it.steps }
                val achievedDays = summary.dailyCounts.count { it.steps >= it.targetSteps }
                val totalDays = summary.dailyCounts.size.coerceAtLeast(7)
                val achievementPct = if (summary.dailyCounts.isEmpty()) 0
                    else achievedDays * 100 / totalDays
                WeeklyArchiveSummary(
                    weekStartEpochDay = weekStart.toEpochDay(),
                    isLocked = isLocked,
                    unlockDate = weekStart.plusWeeks(1),
                    totalSteps = totalSteps,
                    achievedDays = achievedDays,
                    totalDays = totalDays,
                    achievementPct = achievementPct,
                    achievementRate = achievementPct / 100f,
                )
            }.filter { it.isLocked || it.totalSteps > 0 }
        }
    }

    private companion object {
        const val DEFAULT_WEEK_COUNT = 12
    }
}
