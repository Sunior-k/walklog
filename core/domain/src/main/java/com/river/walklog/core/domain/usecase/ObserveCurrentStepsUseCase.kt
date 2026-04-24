package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.StepRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCurrentStepsUseCase @Inject constructor(
    private val stepRepository: StepRepository,
) {
    operator fun invoke(): Flow<Int> = stepRepository.observeCurrentSteps()
}
