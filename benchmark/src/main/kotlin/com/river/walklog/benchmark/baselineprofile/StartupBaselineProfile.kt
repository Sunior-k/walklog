package com.river.walklog.benchmark.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.benchmark.PACKAGE_NAME
import com.river.walklog.benchmark.startActivityAndAllowNotifications
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 앱 시작 경로 Baseline Profile 생성기.
 * includeInStartupProfile = true 로 baseline-prof.txt(AOT 힌트)와 startup-prof.txt(Dex 배치 최적화)를 함께 생성한다.
 */
@RunWith(AndroidJUnit4::class)
class StartupBaselineProfile {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = PACKAGE_NAME,
        includeInStartupProfile = true,
    ) {
        startActivityAndAllowNotifications()
    }
}
