package com.river.walklog.core.database.di

import android.content.Context
import androidx.room.Room
import com.river.walklog.core.database.WalkLogDatabase
import com.river.walklog.core.database.dao.DailyStepDao
import com.river.walklog.core.database.dao.PointsLedgerDao
import com.river.walklog.core.database.dao.RewardRedemptionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWalkLogDatabase(
        @ApplicationContext context: Context,
    ): WalkLogDatabase = Room.databaseBuilder(
        context,
        WalkLogDatabase::class.java,
        WalkLogDatabase.DATABASE_NAME,
    )
        .addMigrations(WalkLogDatabase.MIGRATION_1_2, WalkLogDatabase.MIGRATION_2_3, WalkLogDatabase.MIGRATION_3_4)
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideDailyStepDao(db: WalkLogDatabase): DailyStepDao = db.dailyStepDao()

    @Provides
    fun provideRewardRedemptionDao(db: WalkLogDatabase): RewardRedemptionDao = db.rewardRedemptionDao()

    @Provides
    fun providePointsLedgerDao(db: WalkLogDatabase): PointsLedgerDao = db.pointsLedgerDao()
}
