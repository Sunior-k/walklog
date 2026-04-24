package com.river.walklog.core.engine

import java.time.LocalTime

/**
 * JNI bridge to the native Walking Insights engine (libwalking_insights.so).
 *
 * The C++ engine computes four signals from up to 7 days of hourly step data:
 * - **peakHour**           — exponentially weighted hourly activity profile
 * - **weeklyTrend**        — sigmoid-normalised recent-vs-earlier daily average ratio
 * - **recoveryDifficulty** — 7-day goal achievement rate mapped to mission difficulty
 * - **streakRisk**         — current-day pace vs historical pace × projected deficit
 *
 * Reference: android/ndk-samples (https://github.com/android/ndk-samples)
 */
class WalkingInsightsEngine {

    /**
     * Analyzes hourly step data for the past [days] days.
     *
     * @param hourlySteps       Float array of length days×24. Layout:
     *                          [day0_h0, day0_h1, …, day0_h23, day1_h0, …]
     *                          where day 0 is the oldest and the last day is today.
     * @param targetStepsPerDay User's daily step goal.
     * @param currentHour       Current hour of today (0–23). Defaults to system time.
     */
    fun analyze(
        hourlySteps: FloatArray,
        targetStepsPerDay: Int,
        currentHour: Int = LocalTime.now().hour,
    ): WalkingInsightsResult {
        require(hourlySteps.isNotEmpty() && hourlySteps.size % 24 == 0) {
            "hourlySteps.size must be a multiple of 24, got ${hourlySteps.size}"
        }
        require(hourlySteps.size / 24 in 1..7) {
            "hourlySteps must cover 1–7 days, got ${hourlySteps.size / 24}"
        }
        require(currentHour in 0..23) {
            "currentHour must be 0–23, got $currentHour"
        }

        val raw = analyzeNative(hourlySteps, targetStepsPerDay, currentHour)
            ?: error("Native engine returned null — check logcat for JNI errors")

        return WalkingInsightsResult(
            peakHour            = raw[0].toInt(),
            weeklyTrend         = raw[1],
            recoveryDifficulty  = raw[2],
            streakRisk          = raw[3],
        )
    }

    private external fun analyzeNative(
        hourlySteps: FloatArray,
        targetStepsPerDay: Int,
        currentHour: Int,
    ): FloatArray?

    companion object {
        init {
            System.loadLibrary("walking_insights")
        }
    }
}
