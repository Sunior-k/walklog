package com.river.walklog.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until

/**
 * 화면에 무관하게 공통으로 사용하는 MacrobenchmarkScope 확장 함수 모음.
 * 앱 실행·온보딩 처리·권한 다이얼로그 처리를 담당하며, setupBlock과 collect 블록에서 공통으로 호출된다.
 */
fun MacrobenchmarkScope.startActivityAndAllowNotifications() {
    startActivityAndWait()
    completeOnboardingIfNeeded()
    device.waitForIdle()
}

fun MacrobenchmarkScope.completeOnboardingIfNeeded() {
    if (!device.wait(Until.hasObject(By.text("어떻게 불러드릴까요?")), WAIT_TIMEOUT_MS)) return

    device.wait(Until.hasObject(By.clazz("android.widget.EditText")), WAIT_TIMEOUT_MS)
    retryOnStale {
        device.findObject(By.clazz("android.widget.EditText"))?.setText("워커")
    }
    device.clickText("다음")

    if (device.wait(Until.hasObject(By.text("걸음 수 연동하기")), WAIT_TIMEOUT_MS)) {
        device.clickText("권한 허용하기")
        handlePermissionScreen()
    }

    if (device.wait(Until.hasObject(By.text("목표를 세워요")), WAIT_TIMEOUT_MS)) {
        device.clickText("다음")
    }

    if (device.wait(Until.hasObject(By.text("딱 맞는 시간에 알림을")), WAIT_TIMEOUT_MS)) {
        device.clickText("시작하기")
        handlePermissionScreen()
    }

    device.wait(Until.gone(By.text("딱 맞는 시간에 알림을")), WAIT_TIMEOUT_MS)
    device.waitForIdle()
}

private fun MacrobenchmarkScope.handlePermissionScreen() {
    device.waitForIdle()
    if (device.clickText("모두 허용", SHORT_TIMEOUT_MS)) {
        device.clickText("허용", 2_000L)
        return
    }
    if (device.clickText("허용", SHORT_TIMEOUT_MS)) return
    if (device.clickText("앱 사용 중에만 허용", SHORT_TIMEOUT_MS)) return
    if (device.clickText("Allow", SHORT_TIMEOUT_MS)) return
    if (device.clickText("Allow all", SHORT_TIMEOUT_MS)) {
        device.clickText("Allow", 2_000L)
        return
    }
    device.pressBack()
    device.waitForIdle()
}
