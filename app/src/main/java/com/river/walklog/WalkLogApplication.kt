package com.river.walklog

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class WalkLogApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        configureCrashlytics()
    }

    /**
     * Crashlytics 초기 설정.
     *
     * - Debug 빌드: 수집 비활성화 — 개발 중 발생하는 오류가 대시보드를 오염시키지 않도록 구현.
     * - Release 빌드: 수집 활성화 + 앱 버전 정보를 Custom Key로 기록해 크래시 발생 시점의 버전을 즉시 파악할 수 있다.
     *
     * 참고: https://firebase.google.com/docs/crashlytics/customize-crash-reports
     */
    private fun configureCrashlytics() {
        val crashlytics = FirebaseCrashlytics.getInstance()

        crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        if (!BuildConfig.DEBUG) {
            crashlytics.setCustomKey("app_version_name", BuildConfig.VERSION_NAME)
            crashlytics.setCustomKey("app_version_code", BuildConfig.VERSION_CODE)
        }
    }
}
