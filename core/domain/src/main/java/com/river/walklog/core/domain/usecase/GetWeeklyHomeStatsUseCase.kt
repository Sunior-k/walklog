package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.model.WeeklyHomeStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetWeeklyHomeStatsUseCase @Inject constructor(
    private val getWeeklyStepSummary: GetWeeklyStepSummaryUseCase,
    private val userSettingsRepository: UserSettingsRepository,
) {
    operator fun invoke(): Flow<WeeklyHomeStats> =
        getWeeklyStepSummary().combine(userSettingsRepository.settings) { summary, settings ->
            val targetSteps = settings.dailyStepGoal
            val achievementPct = if (summary.dailyCounts.isEmpty() || targetSteps <= 0) 0
            else summary.dailyCounts.count { it.steps >= targetSteps } * 100 / summary.dailyCounts.size
            WeeklyHomeStats(
                totalSteps = summary.dailyCounts.sumOf { it.steps },
                achievementPct = achievementPct,
                bestDayEpochDay = summary.dailyCounts.maxByOrNull { it.steps }?.dateEpochDay,
            )
        }
}
