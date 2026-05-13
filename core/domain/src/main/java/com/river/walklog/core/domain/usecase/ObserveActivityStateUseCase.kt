package com.river.walklog.core.domain.usecase

import com.river.walklog.core.data.repository.ActivityStateRepository
import com.river.walklog.core.model.ActivityState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

class ObserveActivityStateUseCase @Inject constructor(
    private val activityStateRepository: ActivityStateRepository,
) {
    operator fun invoke(): Flow<ActivityState> =
        if (!activityStateRepository.isModelAvailable) emptyFlow()
        else activityStateRepository.observeActivityState()
}
