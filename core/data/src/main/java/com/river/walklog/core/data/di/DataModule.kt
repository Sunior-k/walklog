package com.river.walklog.core.data.di

import com.river.walklog.core.data.repository.DataStoreUserSettingsRepository
import com.river.walklog.core.data.repository.DefaultActivityStateRepository
import com.river.walklog.core.data.repository.DefaultWalkingInsightsRepository
import com.river.walklog.core.data.repository.DefaultWeatherRepository
import com.river.walklog.core.data.repository.OfflineFirstStepRepository
import com.river.walklog.core.data.repository.ActivityStateRepository
import com.river.walklog.core.data.repository.CouponRepository
import com.river.walklog.core.data.repository.FirestoreCouponRepository
import com.river.walklog.core.data.repository.FirestorePromoCodeRepository
import com.river.walklog.core.data.repository.FirestoreRewardCatalogRepository
import com.river.walklog.core.data.repository.PointsLedgerRepository
import com.river.walklog.core.data.repository.PromoCodeRepository
import com.river.walklog.core.data.repository.RewardCatalogRepository
import com.river.walklog.core.data.repository.RewardRedemptionRepository
import com.river.walklog.core.data.repository.RoomPointsLedgerRepository
import com.river.walklog.core.data.repository.RoomRewardRedemptionRepository
import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.data.repository.UserSettingsRepository
import com.river.walklog.core.data.repository.WalkingInsightsRepository
import com.river.walklog.core.data.repository.WeatherRepository
import com.river.walklog.core.data.weather.GpsWeatherLocationProvider
import com.river.walklog.core.data.weather.LocaleWeatherLocationProvider
import com.river.walklog.core.data.weather.WeatherLocationProvider
import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    abstract fun bindActivityStateRepository(impl: DefaultActivityStateRepository): ActivityStateRepository

    @Binds
    @Singleton
    abstract fun bindWalkingInsightsRepository(impl: DefaultWalkingInsightsRepository): WalkingInsightsRepository

    @Binds
    @Singleton
    abstract fun bindWeatherLocationProvider(impl: GpsWeatherLocationProvider): WeatherLocationProvider

    @Binds
    @Singleton
    abstract fun bindRewardRedemptionRepository(impl: RoomRewardRedemptionRepository): RewardRedemptionRepository

    @Binds
    @Singleton
    abstract fun bindPointsLedgerRepository(impl: RoomPointsLedgerRepository): PointsLedgerRepository

    @Binds
    @Singleton
    abstract fun bindCouponRepository(impl: FirestoreCouponRepository): CouponRepository

    @Binds
    @Singleton
    abstract fun bindRewardCatalogRepository(impl: FirestoreRewardCatalogRepository): RewardCatalogRepository

    @Binds
    @Singleton
    abstract fun bindPromoCodeRepository(impl: FirestorePromoCodeRepository): PromoCodeRepository

    companion object {
        @Provides
        @Singleton
        fun provideLocaleWeatherLocationProvider(): LocaleWeatherLocationProvider =
            LocaleWeatherLocationProvider()

        @Provides
        @Singleton
        fun provideHealthConnectClient(@ApplicationContext context: Context): HealthConnectClient =
            HealthConnectClient.getOrCreate(context)

        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    }
}
