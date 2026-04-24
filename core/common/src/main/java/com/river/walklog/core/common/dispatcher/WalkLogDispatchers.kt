package com.river.walklog.core.common.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

data class WalkLogDispatchers(
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher,
    val main: CoroutineDispatcher,
)
