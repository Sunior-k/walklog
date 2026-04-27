package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.model.WeeklyStepSummary
import javax.inject.Inject

class GetWeeklyBestHourUseCase @Inject constructor(
    private val stepRepository: StepRepository,
) {
    suspend operator fun invoke(summary: WeeklyStepSummary): Int? {
        val hourlySteps = stepRepository.getHourlyStepsForRange(
            fromEpochDay = summary.weekStartEpochDay,
            toEpochDay = summary.weekStartEpochDay + 6L,
        )
        val hourlyTotals = FloatArray(HOURS_PER_DAY)
        hourlySteps.forEachIndexed { index, steps ->
            hourlyTotals[index % HOURS_PER_DAY] += steps
        }
        return hourlyTotals
            .withIndex()
            .maxByOrNull { it.value }
            ?.takeIf { it.value > 0f }
            ?.index
    }

    private companion object {
        const val HOURS_PER_DAY = 24
    }
}
