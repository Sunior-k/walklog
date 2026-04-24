package com.river.walklog.core.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [CrashReporter] 구현체.
 *
 * Release 빌드에서만 데이터가 실제로 전송.
 * Debug 빌드는 [WalkLogApplication.configureCrashlytics] 에서
 * [FirebaseCrashlytics.setCrashlyticsCollectionEnabled](false) 로 비활성화되어 있으므로
 * 이 구현체를 그대로 주입해도 데이터가 서버로 전송되지 않음.
 *
 * ※ [FirebaseCrashlytics.getInstance] 는 싱글턴이므로 여기서 프로퍼티로 캐싱한다.
 */
@Singleton
class CrashlyticsReporter @Inject constructor() : CrashReporter {

    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun setKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }
}
