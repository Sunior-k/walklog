package com.river.walklog.core.data.repository

import com.river.walklog.core.engine.ActivityClassifier
import com.river.walklog.core.engine.EngineActivityState
import com.river.walklog.core.model.ActivityState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultActivityStateRepository @Inject constructor(
    private val activityClassifier: ActivityClassifier,
) : ActivityStateRepository {
    override val isModelAvailable: Boolean
        get() = activityClassifier.isModelAvailable

    override val isStationary: StateFlow<Boolean>
        get() = activityClassifier.isStationary

    override fun observeActivityState(): Flow<ActivityState> =
        activityClassifier.observeActivityState().map(EngineActivityState::asExternalModel)
}

private fun EngineActivityState.asExternalModel(): ActivityState =
    when (this) {
        EngineActivityState.WALKING -> ActivityState.WALKING
        EngineActivityState.STATIONARY -> ActivityState.STATIONARY
        EngineActivityState.UNKNOWN -> ActivityState.UNKNOWN
    }
