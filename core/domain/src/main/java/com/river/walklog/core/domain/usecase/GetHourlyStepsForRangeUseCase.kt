package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.StepRepository
import javax.inject.Inject

class GetHourlyStepsForRangeUseCase @Inject constructor(
    private val stepRepository: StepRepository,
) {
    suspend operator fun invoke(fromEpochDay: Long, toEpochDay: Long): FloatArray =
        stepRepository.getHourlyStepsForRange(fromEpochDay, toEpochDay)
}
