package com.river.walklog.feature.home.notification

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindWalkingReminderScheduler(
        impl: AlarmManagerWalkingReminderScheduler,
    ): WalkingReminderScheduler
}
