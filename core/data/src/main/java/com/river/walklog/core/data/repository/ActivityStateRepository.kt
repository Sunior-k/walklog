package com.river.walklog.core.data.repository

import com.river.walklog.core.model.ActivityState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ActivityStateRepository {
    val isModelAvailable: Boolean
    val isStationary: StateFlow<Boolean>

    fun observeActivityState(): Flow<ActivityState>
}
