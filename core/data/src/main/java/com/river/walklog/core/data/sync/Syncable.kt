package com.river.walklog.core.data.sync

interface Syncable {
    suspend fun sync(): Boolean
}
