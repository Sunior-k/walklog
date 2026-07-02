package com.river.walklog.core.common.di

import com.river.walklog.core.common.dispatcher.WalkLogDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopesModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun providesApplicationCoroutineScope(
        dispatchers: WalkLogDispatchers,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatchers.default)
}
