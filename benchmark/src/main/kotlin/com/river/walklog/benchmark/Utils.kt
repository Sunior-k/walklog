package com.river.walklog.benchmark

import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import java.io.ByteArrayOutputStream

/**
 * Macrobenchmark 공통 상수 및 UiDevice 확장 함수 모음.
 * fling, waitAndFind, clickText 등 UIAutomator 조작 헬퍼를 제공한다.
 */
const val PACKAGE_NAME = "com.river.walklog"

internal const val WAIT_TIMEOUT_MS = 5_000L
internal const val SHORT_TIMEOUT_MS = 1_000L

fun UiDevice.flingElementDownUp(element: UiObject2) {
    val margin = element.visibleBounds.width() / 10
    element.setGestureMargin(margin)
    element.fling(Direction.DOWN)
    waitForIdle()
    element.fling(Direction.UP)
    waitForIdle()
}

fun UiDevice.waitAndFindObject(selector: BySelector, timeout: Long): UiObject2 {
    if (!wait(Until.hasObject(selector), timeout)) {
        throw AssertionError("Element not found after ${timeout}ms: $selector")
    }
    return findObject(selector)
}

fun UiDevice.dumpWindowHierarchy(): String {
    val buffer = ByteArrayOutputStream()
    dumpWindowHierarchy(buffer)
    return buffer.toString()
}

internal fun UiDevice.clickText(text: String, timeoutMs: Long = WAIT_TIMEOUT_MS): Boolean {
    if (!wait(Until.hasObject(By.text(text)), timeoutMs)) return false
    return retryOnStale {
        findObject(By.text(text))?.click()
        waitForIdle()
        true
    } ?: false
}

internal fun <T> retryOnStale(block: () -> T): T? {
    repeat(3) {
        try {
            return block()
        } catch (_: StaleObjectException) {
            Thread.sleep(250)
        }
    }
    return null
}
