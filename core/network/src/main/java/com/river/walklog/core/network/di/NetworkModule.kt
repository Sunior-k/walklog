package com.river.walklog.core.network.di

import com.river.walklog.core.network.KmaWeatherNetworkDataSource
import com.river.walklog.core.network.WeatherNetworkDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindWeatherNetworkDataSource(
        impl: KmaWeatherNetworkDataSource,
    ): WeatherNetworkDataSource

    companion object {
        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()
    }
}
