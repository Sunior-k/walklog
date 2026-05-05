# Baseline Profile

> 참고: [Android Developers — Baseline Profiles](https://developer.android.com/topic/performance/baselineprofiles) · [Google I/O 2022 — What's new in Android performance](https://youtu.be/9eNiM8M7lrc) · [Macrobenchmark](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview)

---

## 1. 문제: 콜드 스타트가 느린 이유

Android 앱을 처음 실행하면 ART(Android Runtime)가 DEX 바이트코드를 네이티브 머신 코드로 변환하는데, 이때 크게 두 가지 방식을 사용합니다.

| 방식 | 설명 | 특징 |
|---|---|---|
| **JIT** (Just-In-Time) | 실행 중에 필요한 메서드를 그때그때 컴파일 | 설치 빠름, 첫 실행 느림 |
| **AOT** (Ahead-Of-Time) | 실행 전에 미리 컴파일 | 실행 빠름, 컴파일 시간·용량 소모 |

처음 설치한 직후에는 JIT 상태로 시작하기 때문에 클래스 로딩·검증·인터프리팅이 실행 시점에 한꺼번에 몰리면서 **콜드 스타트가 눈에 띄게 느려집니다.** 이후 앱을 여러 번 실행하면 ART가 프로파일을 수집해 백그라운드에서 점진적으로 AOT 컴파일을 진행하지만, 최적화가 반영되기까지는 시간이 걸립니다.

```
사용자 관점:
설치 직후   → JIT → 느림 (콜드 스타트 600~800ms)
며칠 사용 후 → 점진적 AOT → 빨라짐 (200~300ms)
```

결국 사용자가 앱을 처음 만나는 순간이 오히려 앱이 가장 느린 시점이 됩니다.

---

## 2. 구글의 자동 최적화 — Cloud Profile과 그 한계

Android 7부터 ART는 JIT + AOT 혼합 방식을 채택했습니다. 앱 실행 중 JIT로 핫 패스를 기록하고, 기기 유휴 시간(충전 중 등)에 백그라운드에서 점진적으로 AOT 컴파일하는 방식입니다.

Google Play는 수백만 기기에서 수집한 이 데이터를 바탕으로 **Cloud Profile**을 구성하며, 신규 설치 시 주요 경로를 미리 AOT 컴파일해 콜드 스타트를 약 **15%** 개선합니다.

**Cloud Profile의 한계:**

| 문제 | 영향 |
|---|---|
| 앱 출시·업데이트마다 리셋 | 새 버전 초기 유저는 JIT only 상태 |
| 데이터 누적에 수일 소요 | 신규 버전 출시 직후 성능 저하 구간 발생 |
| 업데이트 주기가 잦을수록 효과 감소 | Cloud Profile이 성숙하기 전에 다시 리셋 |

---

## 3. 해결: Baseline Profile이란?

Baseline Profile은 **개발자가 직접 핫 패스를 정의해 앱과 함께 배포하는 힌트 파일**로, Cloud Profile 데이터가 전혀 없어도 앱 설치 즉시 해당 경로를 AOT 컴파일합니다. 덕분에 앱 업데이트 직후 새로 다운받는 유저도 처음부터 최적화된 시작 속도를 경험할 수 있습니다.

```
app/src/main/baseline-prof.txt

HSPLcom/river/walklog/MainActivity;->onCreate(Landroid/os/Bundle;)V
HSPLcom/river/walklog/feature/home/HomeRoute;...
PLcom/river/walklog/core/data/repository/StepRepositoryImpl;...
...
```

- `H` = Hot (자주 실행되는 메서드)
- `S` = Startup (앱 시작 경로)
- `P` = Post-startup (시작 직후 필요한 경로)
- `L` = class
- `;->` = 메서드 구분자

Google Play는 이 파일을 읽고 **앱 설치 시점에 해당 경로를 미리 AOT 컴파일**합니다.

---

## 4. 적용 전 vs 적용 후

```
적용 전 (JIT only)

[사용자가 앱 아이콘 탭]
    → Process 생성
    → DEX 로딩
    → 클래스 검증 (JIT 컴파일)   ← 여기서 지연
    → Compose 초기 컴포지션
    → 첫 프레임 렌더링

적용 후 (AOT + Baseline Profile)

[사용자가 앱 아이콘 탭]
    → Process 생성
    → 이미 컴파일된 네이티브 코드 실행  ← 지연 없음
    → Compose 초기 컴포지션
    → 첫 프레임 렌더링
```

**Google이 자사 앱에서 측정한 수치:**

| 앱 | 콜드 스타트 개선 |
|---|---|
| Google Maps | ~40% |
| Google Play Store | ~30% |
| Jetsnack (샘플 앱) | ~30% |

> 출처: Google I/O 2022 — "Performance: Baseline Profiles"

**WalkLog 실측 결과** (SM-A366N · Android 16 · `StartupBenchmark` 20 이터레이션):

| 조건 | timeToInitialDisplay (median) | JIT 이벤트 | ClassInit 횟수 |
|---|---|---|---|
| JIT only (기준선) | 718.2ms | 8건 · 62.9ms | 4,141건 · 177.9ms |
| Partial — Baseline Profile 비활성 | 695.3ms | 0 | 1,044건 · 73.8ms |
| **Baseline Profile 적용** | **700.5ms** | **0** | **1,054건 · 66.5ms** |
| Full AOT | 724.6ms | 0 | 4,407건 · 176.6ms |

`timeToInitialDisplayMs` 단독으로는 Baseline Profile 효과가 ~18ms(−2.5%)로 작아 보이지만, ClassInit 횟수가 **4,141 → 1,054건(−75%)** 으로 크게 감소했습니다. ART가 시작 경로의 클래스를 사전에 AOT 컴파일해 런타임 초기화 부담을 줄인 결과입니다.

JIT 컴파일이 0으로 완전히 사라진 것 역시 프로파일이 정상 적용됐다는 직접 증거입니다.

#### 왜 개선폭이 작게 보이나 — Android 버전과 기기 성능의 영향

구버전 Android(9~12)와 보급형 기기에서는 Baseline Profile 적용 시 콜드 스타트가 30~40% 단축되는 사례가 많습니다. WalkLog의 −2.5%가 작게 보이는 이유는 두 가지입니다.

**1. Android 16의 개선된 ART JIT**  
Android 16에서 ART JIT 컴파일러 자체가 크게 빨라졌습니다. 구 Android에서 JIT 오버헤드가 수백 ms에 달했던 반면, Android 16에서는 동일한 코드를 훨씬 빠르게 JIT 처리합니다. AOT(Baseline Profile)와의 절대 시간 격차가 좁아지는 것은 플랫폼 개선의 결과입니다.

**2. Without 기준선이 이미 빠름**  
WalkLog Without 기준이 718ms인 반면, Android 구버전 기기에서는 같은 앱도 1,500ms 이상 나오기도 합니다. 시작이 빠를수록 개선 여지 자체가 작습니다.

**결론**: `timeToInitialDisplayMs` 개선율이 작다고 해서 Baseline Profile이 효과 없는 것이 아닙니다. ClassInit −75%와 JIT 완전 제거가 실제 최적화 작동을 증명하며, 구버전 Android나 저사양 기기에서는 동일한 프로파일로 훨씬 큰 시간 단축 효과를 볼 수 있습니다.

Full AOT의 경우 모든 코드를 컴파일하지만 오히려 시작이 가장 느렸습니다. 대용량 AOT 코드를 캐시에 올리는 비용이 JIT 오버헤드를 상회한 결과로, Baseline Profile처럼 시작 경로를 선별적으로 AOT하는 방식이 더 효율적임을 보여줍니다.

---

## 5. 프로젝트 적용 구조

```
:benchmark 모듈
    ├── StartupBaselineProfile  (includeInStartupProfile = true)
    └── HomeBaselineProfile     (홈 스크롤 · 주간 리포트 · 미션 상세)
            ↓ 실행
    기기에서 앱 사용 흐름 재현
            ↓ 프로파일 수집
    app/src/release/generated/baselineProfiles/
        ├── baseline-prof.txt   (AOT 컴파일 힌트 → TTID + TTFD 개선)
        └── startup-prof.txt    (Dex Layout Optimization → TTID 개선)
            ↓ 빌드 시 포함
    APK 내 assets/dexopt/baseline.prof
            ↓ 설치 시
    ART AOT 컴파일 (profileinstaller trigger)
```

두 파일의 역할은 서로 다릅니다.

| 파일 | 최적화 방식 | 주요 효과 |
|---|---|---|
| `baseline-prof.txt` | dex → oat AOT 컴파일 | 시작 중 JIT 제거, 클래스 초기화 감소 → TTID + TTFD 단축 |
| `startup-prof.txt` | Dex Layout Optimization — 핫 패스 클래스를 dex 앞쪽에 배치 | 클래스 로딩 스캔 속도 향상 → 주로 TTID 단축 |

`includeInStartupProfile = true`를 설정한 `StartupBaselineProfile`이 두 파일을 모두 생성하며, `HomeBaselineProfile`은 `baseline-prof.txt`만 추가로 보완합니다.

### 모듈 설정

```kotlin
// benchmark/build.gradle.kts
plugins {
    id("com.android.test")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.baselineprofile)  // androidx.baselineprofile
}

android {
    targetProjectPath = ":app"  // 어느 앱의 프로파일을 생성할지 지정
}
```

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.baselineprofile)
}

dependencies {
    implementation(libs.androidx.profileinstaller)  // 설치 시 AOT 컴파일 트리거
    baselineProfile(projects.benchmark)             // 프로파일 소스 모듈 연결
}
```

### benchmark 코드 구조

`:benchmark` 모듈은 Now in Android 패턴을 따라 Baseline Profile 생성과 실제 성능 측정을 화면 단위 서브 패키지로 분리합니다.

```
benchmark/src/main/kotlin/com/river/walklog/benchmark/
├── BenchmarkMetrics.kt
├── GeneralActions.kt
├── Utils.kt
├── baselineprofile/
│   ├── StartupBaselineProfile.kt
│   └── HomeBaselineProfile.kt
├── home/
│   ├── HomeActions.kt
│   └── ScrollHomeFeedBenchmark.kt
└── startup/
    └── StartupBenchmark.kt
```

`rule.collect {}` 블록이 실행되는 동안 ART는 호출된 클래스와 메서드를 추적하며, 수집이 끝나면 결과가 `baseline-prof.txt`로 저장됩니다.

`StartupBaselineProfile`에는 `includeInStartupProfile = true`를 적용해 Dex Layout Optimization 대상 시작 경로를 별도로 수집하고, `HomeBaselineProfile`은 홈 스크롤·주간 리포트·미션 상세 탐색 경로를 추가로 확보합니다.

---

## 6. 프로파일 생성과 측정

```bash
# API 28+ 실기기 또는 userdebug/rooted 에뮬레이터 연결 후:
./gradlew :app:generateBaselineProfile

# 생성된 파일 위치
app/src/main/baseline-prof.txt
```

실제 측정값은 Macrobenchmark 테스트 결과를 통해 확인할 수 있습니다.

```bash
# 시작 시간 측정 (4가지 CompilationMode)
./gradlew :benchmark:connectedBenchmarkAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.river.walklog.benchmark.startup.StartupBenchmark

# 홈 스크롤 프레임 성능 측정 (3가지 CompilationMode)
./gradlew :benchmark:connectedBenchmarkAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.river.walklog.benchmark.home.ScrollHomeFeedBenchmark
```

시작 시간은 `StartupBenchmark`의 `timeToInitialDisplayMs` 중앙값 기준입니다 (SM-A366N · Android 16 · 20 이터레이션).

| 조건 | 테스트 메서드 | timeToInitialDisplay | ClassInit 횟수 |
|---|---|---|---|
| JIT only (기준선) | `startupWithoutPreCompilation` | 718.2ms | 4,141건 |
| Partial — 프로파일 비활성 | `startupWithPartialCompilationAndDisabledBaselineProfile` | 695.3ms | 1,044건 |
| **Baseline Profile 적용** | `startupPrecompiledWithBaselineProfile` | **700.5ms** | **1,054건** |
| Full AOT | `startupFullyPrecompiled` | 724.6ms | 4,407건 |

Android 15/16 실기기에서 `Unable to confirm activity launch completion` 오류가 발생한다면 `androidx.benchmark` 버전을 먼저 확인해 주세요. `startActivityAndWait()`가 런치 프레임을 확인하는 과정에서 기기별 `framestats` 결과를 읽지 못할 수 있으므로, 본 프로젝트는 `androidx.benchmark:benchmark-macro-junit4`와 Baseline Profile Gradle Plugin을 1.4.1 이상으로 사용합니다.

> **왜 실기기가 필요한가?**
> ART가 실제 DEX 실행 경로를 추적해야 하기 때문입니다.
> 일반 `debug` 에뮬레이터는 ART 프로파일 생성 API가 제한되므로, `userdebug` 빌드 에뮬레이터나 실제 기기가 필요합니다.

> **Charles Proxy / Proxyman 주의**
> 프록시 도구가 활성화된 상태에서 Macrobenchmark를 실행하면 테스트가 실패할 수 있습니다.
> Benchmark가 내부적으로 실제 네트워크 환경을 가정하기 때문에, 프록시로 트래픽이 우회되면 상태 검증 단계에서 오류가 발생합니다.
> 벤치마크 실행 전 프록시를 반드시 종료하세요.

---

## 7. benchmark 빌드타입이 별도로 필요한 이유

Baseline Profile은 **release와 동일한 환경**에서 생성해야 실제 프로덕션 경로를 수집할 수 있는데, R8 minification이 켜져 있으면 UIAutomator의 `By.text()` 탐색이 난독화된 문자열과 맞지 않는 문제가 생길 수 있습니다.

```kotlin
// app/build.gradle.kts
create("benchmark") {
    initWith(buildTypes.getByName("release"))  // release 기반
    isMinifyEnabled = false                    // UIAutomator 탐색 안정성
    isShrinkResources = false
    isDebuggable = false                       // 실제 앱 동작 재현
}
```

---

## 8. `profileinstaller`의 역할

Google Play 배포 환경에서는 APK에 포함된 Baseline Profile을 읽어 클라우드 컴파일을 수행합니다. 그러나 **직접 APK 설치나 사이드로딩**처럼 Google Play를 거치지 않는 경로에서는 이 과정이 생략됩니다.

`profileinstaller` 라이브러리는 이 공백을 보완하기 위해 추가했습니다.

```
앱 최초 실행 시
    → profileinstaller가 APK 내 baseline.prof 감지
    → BackgroundDexOptService에 AOT 컴파일 요청
    → 다음 실행부터 사전 컴파일된 코드 사용
```

덕분에 테스트/QA 환경에서 직접 설치한 APK도 프로파일 효과를 받을 수 있습니다.
