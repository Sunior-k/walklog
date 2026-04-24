package com.river.walklog

import org.gradle.api.Project

/**
 * Android 모듈의 로컬 단위 테스트 옵션 설정.
 */
internal fun Project.configureAndroidTestOptions() {
    androidExtension.testOptions {
        unitTests.isReturnDefaultValues = true
    }
}
