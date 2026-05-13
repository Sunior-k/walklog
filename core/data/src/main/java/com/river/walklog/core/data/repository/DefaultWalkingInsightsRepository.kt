package com.river.walklog.core.data.repository

import com.river.walklog.core.engine.WalkingInsightsEngine
import com.river.walklog.core.engine.WalkingInsightsResult
import com.river.walklog.core.model.WalkingInsightsAnalysis
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultWalkingInsightsRepository @Inject constructor(
    private val walkingInsightsEngine: WalkingInsightsEngine,
) : WalkingInsightsRepository {
    override fun analyzeHourlySteps(
        hourlySteps: FloatArray,
        targetStepsPerDay: Int,
        currentHour: Int,
    ): WalkingInsightsAnalysis =
        walkingInsightsEngine.analyze(
            hourlySteps = hourlySteps,
            targetStepsPerDay = targetStepsPerDay,
            currentHour = currentHour,
        ).asExternalModel()
}

private fun WalkingInsightsResult.asExternalModel(): WalkingInsightsAnalysis =
    WalkingInsightsAnalysis(
        peakHour = peakHour,
        weeklyTrend = weeklyTrend,
        recoveryDifficulty = recoveryDifficulty,
        streakRisk = streakRisk,
    )
