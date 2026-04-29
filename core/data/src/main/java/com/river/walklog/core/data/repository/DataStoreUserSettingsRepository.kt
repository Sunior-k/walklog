package com.river.walklog.core.data.repository

import com.river.walklog.core.datastore.UserPreferencesDataSource
import com.river.walklog.core.model.ThemeMode
import com.river.walklog.core.model.UserSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreUserSettingsRepository @Inject constructor(
    private val dataSource: UserPreferencesDataSource,
) : UserSettingsRepository {

    override val settings: Flow<UserSettings> = dataSource.settings

    override suspend fun setOnboardingCompleted() = dataSource.setOnboardingCompleted()
    override suspend fun setDailyStepGoal(steps: Int) = dataSource.setDailyStepGoal(steps)
    override suspend fun setNotificationsEnabled(enabled: Boolean) = dataSource.setNotificationsEnabled(enabled)
    override suspend fun setRecoveryMissionSteps(steps: Int) = dataSource.setRecoveryMissionSteps(steps)
    override suspend fun setNickname(nickname: String) = dataSource.setNickname(nickname)
    override suspend fun addPoints(delta: Int) = dataSource.addPoints(delta)
    override suspend fun setThemeMode(themeMode: ThemeMode) = dataSource.setThemeMode(themeMode)
    override suspend fun setLastDailyMissionAwardedDate(date: String) = dataSource.setLastDailyMissionAwardedDate(date)
    override suspend fun setLastRecoveryMissionAwardedDate(date: String) = dataSource.setLastRecoveryMissionAwardedDate(date)
}
