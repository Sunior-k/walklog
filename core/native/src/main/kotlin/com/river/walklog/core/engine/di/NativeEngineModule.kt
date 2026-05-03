package com.river.walklog.core.engine.di

import android.content.Context
import com.river.walklog.core.common.ActivityStateProvider
import com.river.walklog.core.common.dispatcher.WalkLogDispatchers
import com.river.walklog.core.engine.ActivityClassifier
import com.river.walklog.core.engine.ActivitySensorCollector
import com.river.walklog.core.engine.WalkingInsightsEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NativeEngineModule {

    @Provides
    @Singleton
    fun provideWalkingInsightsEngine(): WalkingInsightsEngine = WalkingInsightsEngine()

    @Provides
    @Singleton
    fun provideActivityClassifier(
        @ApplicationContext context: Context,
        sensorCollector: ActivitySensorCollector,
        dispatchers: WalkLogDispatchers,
    ): ActivityClassifier = ActivityClassifier(context, sensorCollector, dispatchers)

    @Provides
    @Singleton
    fun provideActivityStateProvider(classifier: ActivityClassifier): ActivityStateProvider = classifier
}
