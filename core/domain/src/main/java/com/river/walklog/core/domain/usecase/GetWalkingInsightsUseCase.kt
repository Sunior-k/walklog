package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.data.repository.WalkingInsightsRepository
import com.river.walklog.core.model.WalkingInsights
import java.time.LocalDate
import javax.inject.Inject

class GetWalkingInsightsUseCase @Inject constructor(
    private val stepRepository: StepRepository,
    private val walkingInsightsRepository: WalkingInsightsRepository,
) {
    suspend operator fun invoke(targetStepsPerDay: Int, currentHour: Int): WalkingInsights? {
        val today = LocalDate.now()
        val toEpochDay = today.toEpochDay()
        val hourlySteps = stepRepository.getHourlyStepsForRange(toEpochDay - 6, toEpochDay)

        val daysWithData = (0..6).count { dayOffset ->
            val start = dayOffset * HOURS_PER_DAY
            (start until start + HOURS_PER_DAY).any { hourlySteps[it] > 0f }
        }
        if (daysWithData < MIN_DAYS_FOR_INSIGHTS) return null

        val result = walkingInsightsRepository.analyzeHourlySteps(
            hourlySteps = hourlySteps,
            targetStepsPerDay = targetStepsPerDay,
            currentHour = currentHour,
        )

        val totalDays = hourlySteps.size / HOURS_PER_DAY
        val averageStepsAtPeakHour = if (totalDays > 0 && result.peakHour in 0 until HOURS_PER_DAY) {
            var total = 0f
            for (d in 0 until totalDays) total += hourlySteps[d * HOURS_PER_DAY + result.peakHour]
            (total / totalDays).toInt()
        } else null
        val hourlyAverages = (0 until HOURS_PER_DAY).map { hour ->
            var total = 0f
            for (d in 0 until totalDays) total += hourlySteps[d * HOURS_PER_DAY + hour]
            total / totalDays
        }
        val activeDays = (0 until totalDays).count { d ->
            (0 until HOURS_PER_DAY).any { h -> hourlySteps[d * HOURS_PER_DAY + h] > 0f }
        }

        return WalkingInsights(
            peakHour = result.peakHour,
            streakRisk = result.streakRisk,
            averageStepsAtPeakHour = averageStepsAtPeakHour,
            hourlyAverages = hourlyAverages,
            activeDays = activeDays,
            totalDays = totalDays,
        )
    }

    private companion object {
        const val HOURS_PER_DAY = 24
        const val MIN_DAYS_FOR_INSIGHTS = 3
    }
}
