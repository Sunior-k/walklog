package com.river.walklog.core.data.di

import com.river.walklog.core.data.repository.DataStoreUserSettingsRepository
import com.river.walklog.core.data.repository.DefaultWeatherRepository
import com.river.walklog.core.data.repository.OfflineFirstStepRepository
import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.data.repository.WeatherRepository
import com.river.walklog.core.data.weather.GpsWeatherLocationProvider
import com.river.walklog.core.data.weather.LocaleWeatherLocationProvider
import com.river.walklog.core.data.weather.WeatherLocationProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindStepRepository(impl: OfflineFirstStepRepository): StepRepository

    @Binds
    @Singleton
    abstract fun bindUserSettingsRepository(impl: DataStoreUserSettingsRepository): UserSettingsRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: DefaultWeatherRepository): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindWeatherLocationProvider(impl: GpsWeatherLocationProvider): WeatherLocationProvider

    companion object {
        @Provides
        @Singleton
        fun provideLocaleWeatherLocationProvider(): LocaleWeatherLocationProvider =
            LocaleWeatherLocationProvider()
    }
}
