package com.river.walklog.core.testing.repository

import com.river.walklog.core.data.repository.ActivityStateRepository
import com.river.walklog.core.model.ActivityState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeActivityStateRepository : ActivityStateRepository {

    override var isModelAvailable: Boolean = true

    private val activityState = MutableStateFlow(ActivityState.UNKNOWN)
    override val isStationary: StateFlow<Boolean> = MutableStateFlow(false)

    override fun observeActivityState(): Flow<ActivityState> = activityState

    fun setActivityState(state: ActivityState) {
        activityState.value = state
    }
}
