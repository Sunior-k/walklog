# NDK/JNI — Walking Insights Engine

> 참고: [Android NDK Guides](https://developer.android.com/ndk/guides) · [JNI Tips](https://developer.android.com/training/articles/perf-jni) · [CMake for Android](https://developer.android.com/ndk/guides/cmake)

---

## 1. 왜 C++ 엔진인가

걷기 데이터 분석은 7일 × 24시간 배열을 반복적으로 처리하는 부동소수점 연산입니다.
Kotlin으로도 구현할 수 있지만, 아래 이유로 C++ 엔진을 선택했습니다.

| 항목 | Kotlin/JVM | C++ (NDK) |
|---|---|---|
| GC 영향 | 배열 할당 → GC 대상 | 스택/힙 직접 제어 |
| SIMD 확장 | 불가 | ARM NEON 등 적용 가능 |
| 크로스플랫폼 재사용 | Android 전용 | iOS · 서버 공유 가능 |
| 빌드 복잡성 | 낮음 | CMake + NDK 설정 필요 |

엔진이 다루는 연산(최대 168개 float 값의 가중합, argmax, sigmoid 정규화)은 실시간이 아닌 화면 진입 시 1회 수행이므로, 현 단계에서 성능 차이보다 **구조적 분리**와 **재사용 가능성**이 선택 근거입니다.

---

## 2. 모듈 구조

```
core/native/
  src/main/
    cpp/
      CMakeLists.txt               ← NDK 빌드 설정
      walking_insights_engine.h    ← C++ API 선언
      walking_insights_engine.cpp  ← 분석 알고리즘 구현
      walking_insights_jni.cpp     ← JNI 진입점
    kotlin/.../engine/
      WalkingInsightsEngine.kt     ← JNI 브리지 (Kotlin 쪽 래퍼)
      WalkingInsightsResult.kt     ← 결과 데이터 클래스
```

Kotlin 계층은 C++ 구현 세부사항을 직접 알지 못합니다.
JNI 경계는 `WalkingInsightsEngine.kt`에 모아두고, feature 모듈에서는 순수 Kotlin API만 호출하도록 구성했습니다.

---

## 3. CMake 설정

```cmake
# core/native/src/main/cpp/CMakeLists.txt
cmake_minimum_required(VERSION 3.22.1)
project("walking_insights")

add_library(
    walking_insights
    SHARED
    walking_insights_engine.cpp
    walking_insights_jni.cpp
)

find_library(log-lib log)

target_link_libraries(walking_insights ${log-lib})
```

`add_library(SHARED)`를 사용해 `.so` 공유 라이브러리를 생성합니다.
ABI별(arm64-v8a, x86_64 등) 크로스 컴파일은 AGP가 담당합니다.

`build.gradle.kts`에서 CMake를 연결합니다:

```kotlin
// core/native/build.gradle.kts
android {
    defaultConfig {
        externalNativeBuild {
            cmake { cppFlags("-std=c++17", "-O2") }
        }
    }
    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}
```

`-O2`는 릴리스 수준의 최적화 옵션 이라고 보면 됩니다.
디버그 빌드에서도 동일 플래그를 적용해 성능 측정 환경의 일관성을 유지했습니다.

---

## 4. C++ API

### 4-1. 헤더

```cpp
// walking_insights_engine.h
#pragma once
#include <cstdint>

namespace walklog {

struct WalkingInsights {
    int32_t peak_hour;           // 0–23
    float   weekly_trend;        // 0.0–1.0
    float   recovery_difficulty; // 0.2–0.8
    float   streak_risk;         // 0.0–1.0
};

WalkingInsights analyze(
    const float* hourly_steps,  // float[days × 24]
    int32_t      days,          // 1–7
    int32_t      target_steps,  // 하루 목표 걸음 수
    int32_t      current_hour   // 0–23
) noexcept;

}  // namespace walklog
```

`noexcept`를 명시해 JNI 경계에서 C++ 예외가 JVM으로 전파되는 상황을 막았습니다. C++ 예외가 JNI 경계를 넘으면 정의되지 않은 동작이 될 수 있습니다.

### 4-2. 알고리즘

**peakHour** — 지수 가중 시간대 누적 → argmax

최근 날짜에 더 높은 가중치를 부여해, 오래된 패턴보다 최근 행동을 우선하도록 설계했습니다.

```cpp
// 날짜 가중치: 가장 오래된 날 0.5 → 오늘 1.0 (선형 보간)
for (int d = 0; d < days; d++) {
    float dayWeight = 0.5f + 0.5f * static_cast<float>(d) / (days - 1);
    for (int h = 0; h < 24; h++) {
        hourlyAccum[h] += hourly_steps[d * 24 + h] * dayWeight;
    }
}
peak_hour = argmax(hourlyAccum);  // 0–23
```

**weeklyTrend** — 전반부/후반부 평균 비율 → sigmoid 정규화

```cpp
// 전반부(earlier): 첫 절반 날 평균, 후반부(recent): 나머지 절반
float ratio = recentAvg / earlierAvg;  // 1.0 = 변화 없음
// sigmoid 대신 선형 클램프로 0–1 범위로 정규화
weekly_trend = clamp(0.5f + (ratio - 1.0f) * 0.5f, 0.0f, 1.0f);
```

`weeklyTrend > 0.5` → 증가 추세, `< 0.5` → 감소 추세.

**recoveryDifficulty** — 7일 목표 달성률 평균

```cpp
float totalAchievement = 0.0f;
for (int d = 0; d < days; d++) {
    float daySteps = sum(hourly_steps + d * 24, 24);
    totalAchievement += daySteps / target_steps;
}
float rate = totalAchievement / days;
recovery_difficulty = clamp(rate, 0.2f, 0.8f);
```

0.2 하한은 "목표 달성이 전혀 없어도 회복 미션이 불가능하게 느껴지지 않도록 한다"는 UX 의도를 반영한 값입니다.

**streakRisk** — 현재 페이스 기반 스트릭 중단 위험도

```cpp
// 오늘 현재 시각까지의 걸음 수 / (목표 × 경과 시간 비율)
float paceRisk        = 1.0f - min(currentStepsPace / expectedPace, 1.0f);
// 오늘 종료까지 목표 달성 가능성 부족분
float completionRisk  = max(0.0f, 1.0f - projectedSteps / target_steps);
streak_risk = clamp((paceRisk + completionRisk) * 0.5f, 0.0f, 1.0f);
```

---

## 5. JNI 진입점

```cpp
// walking_insights_jni.cpp
#include <jni.h>
#include "walking_insights_engine.h"

extern "C" {

JNIEXPORT jfloatArray JNICALL
Java_com_river_walklog_core_engine_WalkingInsightsEngine_analyzeNative(
    JNIEnv* env,
    jobject /* this */,
    jfloatArray hourlySteps,
    jint        targetStepsPerDay,
    jint        currentHour)
{
    jsize len = env->GetArrayLength(hourlySteps);

    // 입력 검증: 24의 배수, 1~7일
    if (len == 0 || len % 24 != 0 || len / 24 > 7) {
        return nullptr;
    }

    // JNI_ABORT: 읽기 전용 — 네이티브 버퍼가 수정돼도 원본 배열에 복사하지 않음
    const float* steps = env->GetFloatArrayElements(hourlySteps, nullptr);
    if (steps == nullptr) return nullptr;

    walklog::WalkingInsights result = walklog::analyze(
        steps,
        static_cast<int32_t>(len / 24),
        static_cast<int32_t>(targetStepsPerDay),
        static_cast<int32_t>(currentHour)
    );

    env->ReleaseFloatArrayElements(hourlySteps, const_cast<float*>(steps), JNI_ABORT);

    // 결과 4개 값을 FloatArray로 반환
    jfloatArray out = env->NewFloatArray(4);
    if (out == nullptr) return nullptr;

    jfloat buf[4] = {
        static_cast<jfloat>(result.peak_hour),
        result.weekly_trend,
        result.recovery_difficulty,
        result.streak_risk,
    };
    env->SetFloatArrayRegion(out, 0, 4, buf);
    return out;
}

}  // extern "C"
```

**JNI 함수 네이밍 규칙:**

```
Java_<패키지명 점→밑줄>_<클래스명>_<메서드명>
Java_com_river_walklog_core_engine_WalkingInsightsEngine_analyzeNative
```

**`JNI_ABORT`를 쓰는 이유:**

`GetFloatArrayElements`는 JVM이 내부적으로 복사본을 줄 수도, 원본 포인터를 줄 수도 있습니다.
- `0` 플래그: 네이티브에서 수정된 값을 JVM 배열에 반영
- `JNI_COMMIT`: 반영하되 해제는 나중에
- `JNI_ABORT`: 수정 내용 버리고 해제 (읽기 전용 접근)

입력 배열을 수정하지 않기 때문에 `JNI_ABORT`로 읽기 전용 의도를 명시했습니다.

---

## 6. Kotlin 브리지

```kotlin
// WalkingInsightsEngine.kt
class WalkingInsightsEngine {

    companion object {
        init { System.loadLibrary("walking_insights") }
    }

    fun analyze(
        hourlySteps: FloatArray,
        targetStepsPerDay: Int,
        currentHour: Int = LocalTime.now().hour,
    ): WalkingInsightsResult {
        require(hourlySteps.isNotEmpty() && hourlySteps.size % 24 == 0)
        require(hourlySteps.size / 24 in 1..7)
        require(currentHour in 0..23)

        val raw = analyzeNative(hourlySteps, targetStepsPerDay, currentHour)
            ?: error("Native engine returned null")

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
}
```

`System.loadLibrary("walking_insights")`는 `libwalking_insights.so`를 런타임에 로드합니다. `companion object { init { ... } }` 패턴을 사용해 클래스가 처음 로드될 때 한 번만 실행되도록 했습니다.

`private external fun`은 해당 함수의 실제 구현이 JNI 네이티브 코드에 있음을 컴파일러에 알려주는 Kotlin 키워드입니다.

---

## 7. 빌드 결과 확인

```bash
# ABI별 .so 파일 위치
app/build/intermediates/merged_native_libs/release/
  arm64-v8a/libwalking_insights.so
  x86_64/libwalking_insights.so
  armeabi-v7a/libwalking_insights.so

# APK 내 .so 크기 확인
# Android Studio → Build → Analyze APK → lib/ 폴더
```

```bash
# NDK 빌드만 별도 실행
./gradlew :core:native:externalNativeBuildDebug
```

---

## 8. 트러블슈팅

### `UnsatisfiedLinkError: No implementation found for analyzeNative`

- `System.loadLibrary` 호출 전에 `analyzeNative`를 호출한 경우
- JNI 함수명이 패키지/클래스명과 불일치 (`_`와 `_1` 구분 주의)
- ABI 필터 불일치: `abiFilters`에 빌드 대상 ABI가 없는 경우

### 네이티브 크래시 디버깅

```bash
# adb logcat에서 SIGSEGV / SIGABRT 스택 추출
adb logcat | ndk-stack -sym app/build/intermediates/merged_native_libs/debug/arm64-v8a
```

`ndk-stack` 도구를 사용하면 네이티브 스택 트레이스를 소스 파일과 라인 번호로 변환할 수 있습니다.
