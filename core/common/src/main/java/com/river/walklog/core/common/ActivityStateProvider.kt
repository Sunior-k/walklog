package com.river.walklog.core.common

import kotlinx.coroutines.flow.StateFlow

interface ActivityStateProvider {
    val isStationary: StateFlow<Boolean>
}
