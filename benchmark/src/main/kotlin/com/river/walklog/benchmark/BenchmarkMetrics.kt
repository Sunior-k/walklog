@file:OptIn(ExperimentalMetricApi::class)

package com.river.walklog.benchmark

import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.TraceSectionMetric

/**
 * [StartupBenchmark]에서 사용하는 커스텀 메트릭 묶음.
 * StartupTimingMetric(TTID/TTFD), JIT 컴파일 이벤트, ClassInit 횟수를 [allMetrics]로 묶어 제공한다.
 */
object BenchmarkMetrics {
    private val jitCompilationMetric = TraceSectionMetric("JIT Compiling %")
    private val classInitMetric = TraceSectionMetric("L%/%;")
    val allMetrics = listOf(StartupTimingMetric(), jitCompilationMetric, classInitMetric)
}
