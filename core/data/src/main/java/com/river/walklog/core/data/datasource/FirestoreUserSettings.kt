package com.river.walklog.core.data.datasource

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Firestore 문서 구조: users/{userId}/data/settings
 *
 * Kotlin data class 대신 일반 class + var 사용 이유:
 * - Firestore 역직렬화는 Java 리플렉션으로 no-arg 생성자를 필요로 함
 * - data class는 모든 프로퍼티에 기본값이 있어도 합성 no-arg 생성자가 생성되지만,
 *   `is` 접두사 Boolean 프로퍼티는 바이트코드에서 isXxx() getter가 중복 생성되어 오동작
 * - var + 기본값 조합이 Firestore 공식 권장 패턴 (직렬화/역직렬화 모두 안전)
 *
 * 필드명은 Kotlin 프로퍼티명(camelCase)과 1:1 매핑.
 * 동기화 제외 항목: themeMode, notificationsEnabled (기기별 설정)
 */
@IgnoreExtraProperties
class FirestoreUserSettings {
    var nickname: String = ""
    var totalPoints: Int = 0
    var dailyStepGoal: Int = 10_000
    var recoveryMissionSteps: Int = 6_000
    var onboardingCompleted: Boolean = false
    var lastDailyMissionAwardedDate: String = ""
    var lastRecoveryMissionAwardedDate: String = ""
    var updatedAt: Long = 0L
}
