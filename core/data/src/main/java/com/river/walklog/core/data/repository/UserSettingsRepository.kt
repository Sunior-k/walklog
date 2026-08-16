package com.river.walklog.core.data.repository

import com.river.walklog.core.model.PremiumVisualMode
import com.river.walklog.core.model.ThemeMode
import com.river.walklog.core.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun setOnboardingCompleted()
    suspend fun setDailyStepGoal(steps: Int)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setRecoveryMissionSteps(steps: Int)
    suspend fun setNickname(nickname: String)
    suspend fun addPoints(delta: Int)

    /**
     * 잔액 확인과 차감을 원자적으로 수행. 잔액이 부족하면 차감 없이 false.
     * [addPoints]를 read-then-write로 조합하면 동시 호출 시 이중 차감이 가능하므로
     * 리워드 교환처럼 잔액 검증이 필요한 차감에는 반드시 이 함수를 사용한다.
     */
    suspend fun trySpendPoints(amount: Int): Boolean
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun setLastDailyMissionAwardedDate(date: String)
    suspend fun setLastRecoveryMissionAwardedDate(date: String)

    /**
     * 오늘 아직 지급되지 않았으면 지급 완료로 원자적으로 마킹하고 true, 이미 지급됐으면 false.
     * [trySpendPoints]와 동일한 이유 — 마킹과 포인트 지급을 read-then-write로 분리하면
     * 동시 호출 시 하루 중복 지급이 가능하므로, 마킹 자체를 원자적 연산으로 먼저 확정한다.
     */
    suspend fun tryMarkDailyMissionAwarded(date: String): Boolean
    suspend fun tryMarkRecoveryMissionAwarded(date: String): Boolean
    suspend fun setUserId(uid: String)
    suspend fun setActiveThemePack(active: Boolean)
    suspend fun setPremiumVisualMode(mode: PremiumVisualMode)
    suspend fun sync(): Boolean
}
