package com.river.walklog.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Baseline Profile 생성기.
 *
 * 실행 방법: ./gradlew :benchmark:connectedBenchmarkAndroidTest
 *
 * 생성된 프로파일은 app/src/main/baseline-prof.txt 에 자동으로 복사.
 *
 * 대상 기기 조건: API 28+
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = "com.river.walklog") {
        // 1. 콜드 스타트 → 홈 화면 진입
        startActivityAndWait()

        // 2. 홈 화면 스크롤 — RecyclerView/Column 렌더링 경로 포함
        homeScreenInteraction()

        // 3. 주간 리포트 진입 후 뒤로가기
        weeklyReportInteraction()

        // 4. 미션 상세 진입 후 뒤로가기
        missionDetailInteraction()
    }

    private fun MacrobenchmarkScope.homeScreenInteraction() {
        device.waitForIdle()
        // 홈 화면이 로드될 때까지 대기
        device.wait(Until.hasObject(By.scrollable(true)), 5_000)
        device.findObject(By.scrollable(true))?.run {
            fling(androidx.test.uiautomator.Direction.DOWN)
            fling(androidx.test.uiautomator.Direction.UP)
        }
    }

    private fun MacrobenchmarkScope.weeklyReportInteraction() {
        // 주간 리포트 카드 탭
        val reportCard = device.findObject(By.text("지난주 리포트"))
        if (reportCard != null) {
            reportCard.click()
            device.waitForIdle()
            device.wait(Until.hasObject(By.text("주간 리포트")), 3_000)
            device.pressBack()
            device.waitForIdle()
        }
    }

    private fun MacrobenchmarkScope.missionDetailInteraction() {
        // 미션 카드 탭
        val missionCard = device.findObject(By.text("오늘 미션"))
            ?: device.findObject(By.text("회복 미션"))
        if (missionCard != null) {
            missionCard.click()
            device.waitForIdle()
            device.wait(Until.hasObject(By.text("미션 상세")), 3_000)
            device.pressBack()
            device.waitForIdle()
        }
    }
}
