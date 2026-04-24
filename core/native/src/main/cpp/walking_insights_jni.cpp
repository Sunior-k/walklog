/**
 * JNI bridge for WalkingInsightsEngine.
 *
 * Reference: android/ndk-samples — https://github.com/android/ndk-samples
 *
 * Naming convention: Java_<package_underscored>_<ClassName>_<methodName>
 * Package: com.river.walklog.core.engine
 */
#include "walking_insights_engine.h"

#include <jni.h>
#include <android/log.h>

#define LOG_TAG "WalkingInsightsJNI"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jfloatArray JNICALL
Java_com_river_walklog_core_engine_WalkingInsightsEngine_analyzeNative(
    JNIEnv*     env,
    jobject     /* thiz */,
    jfloatArray hourlySteps,
    jint        targetStepsPerDay,
    jint        currentHour
) {
    const jsize len  = env->GetArrayLength(hourlySteps);
    const int   days = len / 24;

    if (days < 1 || days > 7 || len % 24 != 0) {
        LOGE("Invalid hourlySteps length: %d (expected multiple of 24, max 168)", len);
        return nullptr;
    }

    jfloat* raw = env->GetFloatArrayElements(hourlySteps, nullptr);
    if (!raw) return nullptr;

    const walklog::WalkingInsights result = walklog::analyze(
        raw,
        static_cast<int32_t>(days),
        static_cast<int32_t>(targetStepsPerDay),
        static_cast<int32_t>(currentHour)
    );

    // JNI_ABORT: do not copy back — we only read the input array.
    env->ReleaseFloatArrayElements(hourlySteps, raw, JNI_ABORT);

    jfloatArray output = env->NewFloatArray(4);
    if (!output) return nullptr;

    const jfloat values[4] = {
        static_cast<jfloat>(result.peak_hour),
        result.weekly_trend,
        result.recovery_difficulty,
        result.streak_risk,
    };
    env->SetFloatArrayRegion(output, 0, 4, values);

    return output;
}

} // extern "C"
