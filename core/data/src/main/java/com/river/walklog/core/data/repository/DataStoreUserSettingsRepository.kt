package com.river.walklog.core.data.repository

import com.river.walklog.core.common.dispatcher.WalkLogDispatchers
import com.river.walklog.core.data.datasource.FirestoreUserSettings
import com.river.walklog.core.data.datasource.FirestoreUserSettingsDataSource
import com.river.walklog.core.data.sync.Syncable
import com.river.walklog.core.datastore.UserPreferencesDataSource
import com.river.walklog.core.model.ThemeMode
import com.river.walklog.core.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreUserSettingsRepository @Inject constructor(
    private val dataSource: UserPreferencesDataSource,
    private val firestoreDataSource: FirestoreUserSettingsDataSource,
    private val dispatchers: WalkLogDispatchers,
) : UserSettingsRepository, Syncable {

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
    override suspend fun setUserId(uid: String) = dataSource.setUserId(uid)

    /**
     * WorkManager(UserSettingsSyncWorker)가 호출.
     * 1. Firestore → Local 복원 (재설치 후 로그인 시)
     * 2. Local → Firestore 업로드 (최신 상태 동기화)
     */
    override suspend fun sync(): Boolean = withContext(dispatchers.io) {
        runCatching {
            val local = dataSource.settings.first()
            if (local.userId.isEmpty()) return@runCatching true

            val remote = firestoreDataSource.getSettings(local.userId)
            if (remote != null) {
                dataSource.applySettings(mergeUserSettings(local, remote))
            }

            val latest = dataSource.settings.first()
            firestoreDataSource.updateSettings(local.userId, latest.toFirestore())

            true
        }.getOrDefault(false)
    }
}

internal fun mergeUserSettings(local: UserSettings, remote: FirestoreUserSettings): UserSettings =
    local.copy(
        nickname = remote.nickname.takeIf { it.isNotBlank() } ?: local.nickname,
        dailyStepGoal = remote.dailyStepGoal.takeIf { it > 0 } ?: local.dailyStepGoal,
        recoveryMissionSteps = remote.recoveryMissionSteps.takeIf { it > 0 } ?: local.recoveryMissionSteps,
        totalPoints = maxOf(local.totalPoints, remote.totalPoints),
        lastDailyMissionAwardedDate = remote.lastDailyMissionAwardedDate.takeIf { it.isNotBlank() }
            ?: local.lastDailyMissionAwardedDate,
        lastRecoveryMissionAwardedDate = remote.lastRecoveryMissionAwardedDate.takeIf { it.isNotBlank() }
            ?: local.lastRecoveryMissionAwardedDate,
        isOnboardingCompleted = local.isOnboardingCompleted || remote.onboardingCompleted,
    )

private fun UserSettings.toFirestore() = FirestoreUserSettings().apply {
    nickname = this@toFirestore.nickname
    totalPoints = this@toFirestore.totalPoints
    dailyStepGoal = this@toFirestore.dailyStepGoal
    recoveryMissionSteps = this@toFirestore.recoveryMissionSteps
    onboardingCompleted = this@toFirestore.isOnboardingCompleted
    lastDailyMissionAwardedDate = this@toFirestore.lastDailyMissionAwardedDate
    lastRecoveryMissionAwardedDate = this@toFirestore.lastRecoveryMissionAwardedDate
    updatedAt = System.currentTimeMillis()
}
