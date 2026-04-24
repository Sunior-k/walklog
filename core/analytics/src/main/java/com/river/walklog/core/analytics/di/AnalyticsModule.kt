package com.river.walklog.core.analytics.di

import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.analytics.CrashlyticsReporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindCrashReporter(impl: CrashlyticsReporter): CrashReporter
}
