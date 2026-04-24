package com.river.walklog.core.analytics

/**
 * Crashlytics 추상화 인터페이스.
 *
 * Release 빌드에서는 [CrashlyticsReporter], 테스트에서는 Test Double을 주입.
 *
 * 사용 가이드:
 * - [recordException] : try-catch 에서 잡은 non-fatal 예외 → 대시보드 Issues 탭에 집계됨
 * - [log]            : 크래시 직전 흐름을 재현하는 브레드크럼 → 대시보드 Logs 탭에서 확인
 * - [setKey]         : 크래시 발생 시점의 앱 상태 → 대시보드 Keys 탭에서 확인.
 *                      마지막으로 설정된 값이 리포트에 포함됨.
 */
interface CrashReporter {

    fun recordException(throwable: Throwable)
    fun log(message: String)
    fun setKey(key: String, value: String)
    fun setKey(key: String, value: Int)
    fun setKey(key: String, value: Boolean)
}
