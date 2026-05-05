package com.river.walklog.benchmark.startup

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.benchmark.BenchmarkMetrics
import com.river.walklog.benchmark.PACKAGE_NAME
import com.river.walklog.benchmark.startActivityAndAllowNotifications
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 콜드 스타트 시간 측정 벤치마크.
 * BenchmarkMetrics(StartupTimingMetric + JIT + ClassInit)로 None / Partial(Disable) / Partial(Require) / Full
 * 4가지 CompilationMode를 비교한다 (20 이터레이션, COLD 스타트).
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupWithoutPreCompilation() = benchmark(CompilationMode.None())

    @Test
    fun startupWithPartialCompilationAndDisabledBaselineProfile() = benchmark(
        CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Disable, warmupIterations = 1),
    )

    @Test
    fun startupPrecompiledWithBaselineProfile() = benchmark(
        CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require),
    )

    @Test
    fun startupFullyPrecompiled() = benchmark(CompilationMode.Full())

    private fun benchmark(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = BenchmarkMetrics.allMetrics,
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            iterations = 20,
            setupBlock = {
                pressHome()
                startActivityAndAllowNotifications()
                pressHome()
            },
        ) {
            startActivityAndWait()
            device.waitForIdle()
        }
    }
}
