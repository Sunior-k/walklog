package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.StepRepository
import javax.inject.Inject

class IsHealthConnectAvailableUseCase @Inject constructor(
    private val stepRepository: StepRepository,
) {
    operator fun invoke(): Boolean = stepRepository.isHealthConnectAvailable
}
