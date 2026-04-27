package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.model.WeeklyReportArchiveEntry
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
    ): Flow<List<WeeklyReportArchiveEntry>> {
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
                WeeklyReportArchiveEntry(
                    summary = summary,
                    isLocked = weekStart == currentWeekStart,
                    unlockDate = weekStart.plusWeeks(1),
                )
            }
        }
    }

    private companion object {
        const val DEFAULT_WEEK_COUNT = 12
    }
}
