package com.river.walklog.core.network.di

import com.river.walklog.core.network.KmaWeatherNetworkDataSource
import com.river.walklog.core.network.OpenMeteoWeatherNetworkDataSource
import com.river.walklog.core.network.WeatherNetworkDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @IntoMap
    @StringKey("KR")
    abstract fun bindKmaWeatherDataSource(
        impl: KmaWeatherNetworkDataSource,
    ): WeatherNetworkDataSource

    @Binds
    @IntoMap
    @StringKey("*")
    abstract fun bindOpenMeteoWeatherDataSource(
        impl: OpenMeteoWeatherNetworkDataSource,
    ): WeatherNetworkDataSource

    companion object {
        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()
    }
}
