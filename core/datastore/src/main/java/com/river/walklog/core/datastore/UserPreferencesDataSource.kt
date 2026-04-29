package com.river.walklog.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.river.walklog.core.model.ThemeMode
import com.river.walklog.core.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences",
)

private val Context.walkLogUserPreferencesDataStore: DataStore<Preferences>
    get() = dataStore

suspend fun Context.readStoredThemeMode(): ThemeMode =
    walkLogUserPreferencesDataStore.data
        .map { preferences ->
            preferences[UserPreferencesDataSource.Keys.THEME_MODE].toThemeMode()
        }
        .first()

@Singleton
class UserPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.walkLogUserPreferencesDataStore

    val settings: Flow<UserSettings> = dataStore.data
        .catch { e ->
            if (e is IOException) {
                emit(emptyPreferences())
            } else {
                throw e
            }
        }
        .map { prefs ->
            UserSettings(
                isOnboardingCompleted = prefs[Keys.IS_ONBOARDING_COMPLETED] ?: false,
                nickname = prefs[Keys.NICKNAME] ?: "",
                totalPoints = prefs[Keys.TOTAL_POINTS] ?: 0,
                dailyStepGoal = prefs[Keys.DAILY_STEP_GOAL] ?: 10_000,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
                recoveryMissionSteps = prefs[Keys.RECOVERY_MISSION_STEPS] ?: 6_000,
                themeMode = prefs[Keys.THEME_MODE].toThemeMode(),
                lastDailyMissionAwardedDate = prefs[Keys.LAST_DAILY_MISSION_AWARDED_DATE] ?: "",
                lastRecoveryMissionAwardedDate = prefs[Keys.LAST_RECOVERY_MISSION_AWARDED_DATE] ?: "",
            )
        }

    suspend fun setOnboardingCompleted() {
        dataStore.edit { it[Keys.IS_ONBOARDING_COMPLETED] = true }
    }

    suspend fun setDailyStepGoal(steps: Int) {
        dataStore.edit { it[Keys.DAILY_STEP_GOAL] = steps }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setRecoveryMissionSteps(steps: Int) {
        dataStore.edit { it[Keys.RECOVERY_MISSION_STEPS] = steps }
    }

    suspend fun setNickname(nickname: String) {
        dataStore.edit { it[Keys.NICKNAME] = nickname }
    }

    suspend fun addPoints(delta: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.TOTAL_POINTS] = (prefs[Keys.TOTAL_POINTS] ?: 0) + delta
        }
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = themeMode.name }
    }

    suspend fun setLastDailyMissionAwardedDate(date: String) {
        dataStore.edit { it[Keys.LAST_DAILY_MISSION_AWARDED_DATE] = date }
    }

    suspend fun setLastRecoveryMissionAwardedDate(date: String) {
        dataStore.edit { it[Keys.LAST_RECOVERY_MISSION_AWARDED_DATE] = date }
    }

    object Keys {
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val NICKNAME = stringPreferencesKey("nickname")
        val TOTAL_POINTS = intPreferencesKey("total_points")
        val DAILY_STEP_GOAL = intPreferencesKey("daily_step_goal")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val RECOVERY_MISSION_STEPS = intPreferencesKey("recovery_mission_steps")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LAST_DAILY_MISSION_AWARDED_DATE = stringPreferencesKey("last_daily_mission_awarded_date")
        val LAST_RECOVERY_MISSION_AWARDED_DATE = stringPreferencesKey("last_recovery_mission_awarded_date")
    }
}

fun String?.toThemeMode(): ThemeMode =
    this
        ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
        ?: ThemeMode.SYSTEM
