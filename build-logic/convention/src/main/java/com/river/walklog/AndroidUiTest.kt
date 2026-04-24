package com.river.walklog

import org.gradle.api.Project

/**
 * Robolectric 기반 Compose UI 테스트 옵션 설정.
 *
 */
internal fun Project.configureAndroidUiTestOptions() {
    androidExtension.testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}
