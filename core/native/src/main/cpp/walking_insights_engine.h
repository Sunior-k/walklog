#pragma once
#include <cstdint>

namespace walklog {

/**
 * Aggregated walking insights derived from 7 days of hourly step data.
 */
struct WalkingInsights {
    int32_t peak_hour;            // Most active hour of day (0–23)
    float   weekly_trend;         // 0.0 (declining) … 1.0 (improving)
    float   recovery_difficulty;  // 0.0 (easy) … 1.0 (hard)
    float   streak_risk;          // 0.0 (safe) … 1.0 (at risk)
};

/**
 * Analyzes hourly step data to produce walking insights.
 *
 * @param hourly_steps  Float array of length days×24.
 *                      Layout: [day0_h0, day0_h1, …, day0_h23, day1_h0, …]
 *                      day 0 = oldest, day (days-1) = today.
 * @param days          Number of complete days in the array (1–7).
 * @param target_steps  User's daily step goal.
 * @param current_hour  Current hour of today (0–23).
 */
WalkingInsights analyze(
    const float* hourly_steps,
    int32_t      days,
    int32_t      target_steps,
    int32_t      current_hour
);

} // namespace walklog
