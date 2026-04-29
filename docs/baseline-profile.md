# Baseline Profile

> 참고: [Android Developers — Baseline Profiles](https://developer.android.com/topic/performance/baselineprofiles) · [Google I/O 2022 — What's new in Android performance](https://youtu.be/9eNiM8M7lrc) · [Macrobenchmark](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview)

---

## 1. 문제: 콜드 스타트가 느린 이유

Android 앱을 처음 실행하면 ART(Android Runtime)가 DEX 바이트코드를 네이티브 머신 코드로 변환합니다. 이때 사용되는 변환 방식은 크게 두 가지로 나뉩니다.

| 방식 | 설명 | 특징 |
|---|---|---|
| **JIT** (Just-In-Time) | 실행 중에 필요한 메서드를 그때그때 컴파일 | 설치 빠름, 첫 실행 느림 |
| **AOT** (Ahead-Of-Time) | 실행 전에 미리 컴파일 | 실행 빠름, 컴파일 시간·용량 소모 |

앱을 처음 설치한 직후에는 JIT 상태로 시작합니다.
클래스 로딩, 검증, 인터프리팅이 실행 시점에 몰리기 때문에 **콜드 스타트가 눈에 띄게 느려질 수 있습니다.**
사용자가 앱을 여러 번 실행하면 ART가 프로파일을 수집하고 백그라운드에서 점진적으로 AOT 컴파일을 진행하지만, 최적화가 반영되기까지는 시간이 걸립니다.

```
사용자 관점:
설치 직후   → JIT → 느림 (콜드 스타트 600~800ms)
며칠 사용 후 → 점진적 AOT → 빨라짐 (200~300ms)
```

즉, 사용자가 앱을 처음 만나는 순간이 오히려 앱이 가장 느린 시점이 될 수 있습니다.

---

## 2. 해결: Baseline Profile이란?

Baseline Profile은 **앱 시작에 필요한 클래스와 메서드를 ART에 미리 알려주는 힌트 파일**입니다.

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

## 3. 적용 전 vs 적용 후

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

---

## 4. 프로젝트 적용 구조

```
:benchmark 모듈
    └── BaselineProfileGenerator.kt
            ↓ 실행
    기기에서 앱 사용 흐름 재현
            ↓ 프로파일 수집
    app/src/main/baseline-prof.txt (자동 생성)
            ↓ 빌드 시 포함
    APK 내 assets/dexopt/baseline.prof
            ↓ 설치 시
    ART AOT 컴파일 (profileinstaller trigger)
```

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

### BaselineProfileGenerator 코드 설명

```kotlin
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = "com.river.walklog") {
        // 1. 콜드 스타트 → 홈 화면 첫 프레임까지 대기
        startActivityAndWait()

        // 2. 홈 화면 스크롤 — Column/LazyColumn 렌더링 경로 수집
        homeScreenInteraction()

        // 3. 주간 리포트 진입 후 뒤로가기 — Navigation 경로 수집
        weeklyReportInteraction()

        // 4. 미션 상세 진입 — BottomBar, ProgressIndicator 렌더링 수집
        missionDetailInteraction()
    }
}
```

`rule.collect {}` 블록이 실행되는 동안 ART는 호출된 클래스와 메서드를 추적합니다. 수집이 끝나면 결과가 `baseline-prof.txt`로 저장됩니다.

---

## 5. 프로파일 생성 방법

```bash
# API 28+ 실기기 또는 userdebug/rooted 에뮬레이터 연결 후:
./gradlew :benchmark:connectedBenchmarkAndroidTest

# 생성된 파일 위치
app/src/main/baseline-prof.txt
```

> **왜 실기기가 필요한가?**
> ART가 실제 DEX 실행 경로를 추적해야 하기 때문입니다.
> 일반 `debug` 에뮬레이터는 ART 프로파일 생성 API가 제한되므로, `userdebug` 빌드 에뮬레이터나 실제 기기가 필요합니다.

---

## 6. benchmark 빌드타입이 별도로 필요한 이유

Baseline Profile은 **release와 동일한 환경**에서 생성해야 실제 프로덕션 경로를 수집할 수 있습니다. 다만 R8 minification이 켜져 있으면 UIAutomator의 `By.text()` 탐색이 난독화된 문자열과 맞지 않을 수 있습니다.

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

## 7. `profileinstaller`의 역할

Google Play 배포에서는 APK에 포함된 Baseline Profile을 읽어 클라우드 컴파일을 수행합니다.
반면 **직접 APK 설치나 사이드로딩**처럼 Google Play를 거치지 않는 경로에서는 이 과정이 생략됩니다.

`profileinstaller` 라이브러리는 이 공백을 보완하기 위해 사용했습니다.

```
앱 최초 실행 시
    → profileinstaller가 APK 내 baseline.prof 감지
    → BackgroundDexOptService에 AOT 컴파일 요청
    → 다음 실행부터 사전 컴파일된 코드 사용
```

덕분에 테스트/QA 환경에서 직접 설치한 APK도 프로파일 효과를 받을 수 있습니다.
