package com.river.walklog.core.analytics

object CrashKeys {

    const val VERSION_NAME = "version_name"
    const val VERSION_CODE = "version_code"

    // 현재 화면 (화면 진입마다 갱신)
    const val SCREEN = "screen"

    // 걸음 수 센서 상태 (권한·가용성 변경 시 갱신)
    const val SENSOR_STATUS = "sensor_status"

    // 실시간 걸음 수 (센서 이벤트마다 갱신 — 크래시 직전 값 기록)
    const val CURRENT_STEPS = "current_steps"
    const val TARGET_STEPS = "target_steps"

    // 위젯 Worker
    const val WIDGET_INSTANCE_COUNT = "widget_instance_count"
    const val WORKER_RUN_ATTEMPT = "worker_run_attempt"

    object Screens {
        const val HOME = "home"
        const val WEEKLY_REPORT = "weekly_report"
        const val MISSION_DETAIL = "mission_detail"
        const val RECAP = "recap"
        const val FORECAST = "forecast"
        const val ONBOARDING = "onboarding"
        const val SETTINGS = "settings"
        const val HISTORY = "history"
    }

    object SensorValues {
        const val LOADING = "loading"
        const val AVAILABLE = "available"
        const val UNAVAILABLE = "unavailable"
        const val PERMISSION_REQUIRED = "permission_required"
        const val PERMISSION_DENIED = "permission_denied"
    }
}
