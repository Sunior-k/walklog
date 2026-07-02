package com.river.walklog.sync.work.di

import com.river.walklog.core.data.sync.SyncManager
import com.river.walklog.sync.work.WorkManagerSyncManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindsSyncManager(impl: WorkManagerSyncManager): SyncManager
}
