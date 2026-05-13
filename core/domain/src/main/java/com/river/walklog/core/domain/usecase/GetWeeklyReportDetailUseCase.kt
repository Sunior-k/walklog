package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.WeeklyReportDetail
import com.river.walklog.core.model.WeeklyStepSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetWeeklyReportDetailUseCase @Inject constructor(
    private val stepRepository: StepRepository,
    private val getWeeklyBestHour: GetWeeklyBestHourUseCase,
) {
    operator fun invoke(weekStartEpochDay: Long): Flow<WeeklyReportDetail> =
        stepRepository.getWeeklyStepSummary(weekStartEpochDay)
            .map { summary ->
                val bestHour = getWeeklyBestHour(summary)
                build(summary, bestHour)
            }

    private suspend fun build(summary: WeeklyStepSummary, bestHour: Int?): WeeklyReportDetail {
        val stepMap = summary.dailyCounts.associateBy { it.dateEpochDay }
        val weekCounts = (0L..6L).map { offset ->
            val epochDay = summary.weekStartEpochDay + offset
            stepMap[epochDay] ?: DailyStepCount(dateEpochDay = epochDay, steps = 0)
        }

        if (weekCounts.none { it.steps > 0 }) {
            return WeeklyReportDetail.empty(summary.weekStartEpochDay, weekCounts)
        }

        val achievedDays = weekCounts.count { it.steps >= it.targetSteps }

        return WeeklyReportDetail(
            weekStartEpochDay = summary.weekStartEpochDay,
            weekCounts = weekCounts,
            totalSteps = summary.dailyCounts.sumOf { it.steps },
            achievedDays = achievedDays,
            totalDays = 7,
            achievementRate = achievedDays / 7f,
            bestDayEpochDay = weekCounts.maxByOrNull { it.steps }?.dateEpochDay,
            bestHour = bestHour,
            longestAchievedStreak = computeLongestStreak(weekCounts),
            isEmpty = false,
        )
    }

    private fun computeLongestStreak(counts: List<DailyStepCount>): Int =
        counts.fold(0 to 0) { (longest, current), day ->
            if (day.steps >= day.targetSteps) {
                val next = current + 1
                maxOf(longest, next) to next
            } else {
                longest to 0
            }
        }.first
}
