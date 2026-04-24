/**
 * Walking Insights Engine
 *
 * Computes four analytics signals from 7 days of hourly step data:
 *   - peak_hour           : exponentially weighted hourly activity profile
 *   - weekly_trend        : sigmoid-normalised recent-vs-earlier average ratio
 *   - recovery_difficulty : 7-day goal achievement rate → mission difficulty
 *   - streak_risk         : pace-vs-history × projected-completion deficit
 *
 * References:
 *   - NDK C++ guidelines  : https://developer.android.com/ndk/guides/cpp-support
 *   - IEEE Std 754-2019   : floating-point arithmetic
 */
#include "walking_insights_engine.h"

#include <algorithm>
#include <cmath>
#include <numeric>

namespace walklog {
namespace {

constexpr int HOURS = 24;

// Linearly increasing weight: day 0 (oldest) → 0.5, day (days-1) (today) → 1.0.
float dayWeight(int day, int totalDays) noexcept {
    if (totalDays <= 1) return 1.0f;
    return 0.5f + 0.5f * static_cast<float>(day)
                        / static_cast<float>(totalDays - 1);
}

float clamp(float v, float lo, float hi) noexcept {
    return v < lo ? lo : (v > hi ? hi : v);
}

// ─── Signal: peak_hour ───────────────────────────────────────────────────────

int32_t computePeakHour(const float* hourly, int days) noexcept {
    float weighted[HOURS] = {};
    for (int d = 0; d < days; ++d) {
        const float w = dayWeight(d, days);
        for (int h = 0; h < HOURS; ++h) {
            weighted[h] += hourly[d * HOURS + h] * w;
        }
    }
    const float* max = std::max_element(weighted, weighted + HOURS);
    return static_cast<int32_t>(max - weighted);
}

// ─── Signal: weekly_trend ────────────────────────────────────────────────────

float computeWeeklyTrend(const float* hourly, int days) noexcept {
    if (days < 2) return 0.5f;

    // Split into earlier half (days 0..split-1) and recent half (days split..days-1).
    const int split = days / 2;

    float earlierSum = 0.f, recentSum = 0.f;
    for (int d = 0; d < days; ++d) {
        float dayTotal = 0.f;
        for (int h = 0; h < HOURS; ++h) dayTotal += hourly[d * HOURS + h];
        (d < split ? earlierSum : recentSum) += dayTotal;
    }

    const float earlierAvg = earlierSum / static_cast<float>(split);
    const float recentAvg  = recentSum  / static_cast<float>(days - split);

    if (earlierAvg < 1.f) return 0.5f;

    // ratio 0.5 → trend 0.0, ratio 1.0 → trend 0.5, ratio 2.0 → trend 1.0
    const float ratio = recentAvg / earlierAvg;
    return clamp(0.5f + (ratio - 1.0f) * 0.5f, 0.f, 1.f);
}

// ─── Signal: recovery_difficulty ─────────────────────────────────────────────

float computeRecoveryDifficulty(
    const float* hourly,
    int days,
    int32_t target
) noexcept {
    if (target <= 0) return 0.5f;

    float totalAchievement = 0.f;
    for (int d = 0; d < days; ++d) {
        float dayTotal = 0.f;
        for (int h = 0; h < HOURS; ++h) dayTotal += hourly[d * HOURS + h];
        totalAchievement += std::min(dayTotal / static_cast<float>(target), 1.f);
    }
    const float avgAchievement = totalAchievement / static_cast<float>(days);

    // Higher avg achievement → harder recovery mission.
    // Clamped to [0.2, 0.8] so the mission is never trivially easy or impossible.
    return clamp(avgAchievement, 0.2f, 0.8f);
}

// ─── Signal: streak_risk ─────────────────────────────────────────────────────

float computeStreakRisk(
    const float* hourly,
    int days,
    int32_t target,
    int32_t currentHour
) noexcept {
    if (days < 2 || currentHour <= 0 || target <= 0) return 0.f;

    // Today's cumulative steps up to currentHour.
    const float* today = hourly + (days - 1) * HOURS;
    float todayProgress = 0.f;
    for (int h = 0; h <= currentHour && h < HOURS; ++h) {
        todayProgress += today[h];
    }

    // Historical average cumulative steps at the same time of day (excluding today).
    float historicalProgress = 0.f;
    for (int d = 0; d < days - 1; ++d) {
        float dayProgress = 0.f;
        for (int h = 0; h <= currentHour && h < HOURS; ++h) {
            dayProgress += hourly[d * HOURS + h];
        }
        historicalProgress += dayProgress;
    }
    historicalProgress /= static_cast<float>(days - 1);

    if (historicalProgress < 1.f) return 0.f;

    // Pace risk: how behind is today vs historical same-time pace.
    const float paceRatio = todayProgress / historicalProgress;
    const float paceRisk  = clamp(1.f - paceRatio * 0.8f, 0.f, 1.f);

    // Completion risk: projection of end-of-day total vs target.
    const float remainingFraction =
        static_cast<float>(HOURS - 1 - currentHour) / static_cast<float>(HOURS);
    const float projected      = todayProgress + historicalProgress * remainingFraction;
    const float completionRisk = clamp(1.f - projected / static_cast<float>(target), 0.f, 1.f);

    return clamp((paceRisk + completionRisk) * 0.5f, 0.f, 1.f);
}

} // namespace

// ─── Public API ──────────────────────────────────────────────────────────────

WalkingInsights analyze(
    const float* hourly_steps,
    int32_t      days,
    int32_t      target_steps,
    int32_t      current_hour
) {
    return WalkingInsights{
        .peak_hour           = computePeakHour(hourly_steps, days),
        .weekly_trend        = computeWeeklyTrend(hourly_steps, days),
        .recovery_difficulty = computeRecoveryDifficulty(hourly_steps, days, target_steps),
        .streak_risk         = computeStreakRisk(hourly_steps, days, target_steps, current_hour),
    };
}

} // namespace walklog
