package com.river.walklog.benchmark.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.benchmark.PACKAGE_NAME
import com.river.walklog.benchmark.home.homeOpenMissionDetailIfVisible
import com.river.walklog.benchmark.home.homeOpenWeeklyReportIfVisible
import com.river.walklog.benchmark.home.homeScrollFeedDownUp
import com.river.walklog.benchmark.startActivityAndAllowNotifications
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 홈 화면 탐색 경로 Baseline Profile 생성기.
 * 홈 스크롤·주간 리포트·미션 상세 진입을 수행해 해당 경로를 baseline-prof.txt에 추가 기록한다.
 */
@RunWith(AndroidJUnit4::class)
class HomeBaselineProfile {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = PACKAGE_NAME) {
        startActivityAndAllowNotifications()
        homeScrollFeedDownUp()
        homeOpenWeeklyReportIfVisible()
        homeOpenMissionDetailIfVisible()
    }
}
