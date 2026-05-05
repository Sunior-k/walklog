package com.river.walklog.benchmark.home

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.river.walklog.benchmark.SHORT_TIMEOUT_MS
import com.river.walklog.benchmark.WAIT_TIMEOUT_MS
import com.river.walklog.benchmark.clickText
import com.river.walklog.benchmark.flingElementDownUp
import com.river.walklog.benchmark.waitAndFindObject

/**
 * 홈 화면 전용 MacrobenchmarkScope 확장 함수 모음.
 * 피드 스크롤, 주간 리포트·미션 상세 진입 후 복귀 액션을 제공한다.
 */
fun MacrobenchmarkScope.homeScrollFeedDownUp() {
    val feedList = device.waitAndFindObject(By.scrollable(true), WAIT_TIMEOUT_MS)
    device.flingElementDownUp(feedList)
}

fun MacrobenchmarkScope.homeOpenWeeklyReportIfVisible() {
    if (device.clickText("지난주 리포트", SHORT_TIMEOUT_MS)) {
        device.wait(Until.hasObject(By.text("주간 리포트")), WAIT_TIMEOUT_MS)
        device.pressBack()
        device.waitForIdle()
    }
}

fun MacrobenchmarkScope.homeOpenMissionDetailIfVisible() {
    if (device.clickText("오늘 미션", SHORT_TIMEOUT_MS) ||
        device.clickText("회복 미션", SHORT_TIMEOUT_MS)
    ) {
        device.wait(Until.hasObject(By.text("미션 상세")), WAIT_TIMEOUT_MS)
        device.pressBack()
        device.waitForIdle()
    }
}
