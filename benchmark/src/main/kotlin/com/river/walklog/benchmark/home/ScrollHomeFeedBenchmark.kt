package com.river.walklog.benchmark.home

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.benchmark.PACKAGE_NAME
import com.river.walklog.benchmark.startActivityAndAllowNotifications
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 홈 피드 스크롤 프레임 성능 측정 벤치마크.
 * FrameTimingMetric으로 None / Partial(Require) / Full 3가지 CompilationMode를 비교한다 (10 이터레이션, WARM 스타트).
 */
@RunWith(AndroidJUnit4::class)
class ScrollHomeFeedBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollFeedCompilationNone() = benchmark(CompilationMode.None())

    @Test
    fun scrollFeedBaselineProfile() = benchmark(
        CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require),
    )

    @Test
    fun scrollFeedFullyPrecompiled() = benchmark(CompilationMode.Full())

    private fun benchmark(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.WARM,
            iterations = 10,
            setupBlock = {
                pressHome()
                startActivityAndAllowNotifications()
            },
        ) {
            homeScrollFeedDownUp()
        }
    }
}
