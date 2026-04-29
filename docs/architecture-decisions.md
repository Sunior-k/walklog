# 아키텍처 결정 기록

> 각 설계 결정의 **이유**와 **트레이드오프**를 기록했습니다.
> 참고: [Now in Android](https://github.com/android/nowinandroid) · [Guide to app architecture](https://developer.android.com/topic/architecture) · [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

---

## ADR-1. Multi-Module 구조

### 결정

단일 모듈 대신 feature + core 기반 **멀티 모듈** 구조를 선택했습니다.

### 이유

| 이점 | 설명 |
|---|---|
| **점진적 빌드** | Gradle이 변경된 모듈만 재빌드. `feature:home`만 수정하면 `feature:recap`은 재빌드하지 않음 |
| **의존성 가시화** | 모듈 간 경계가 컴파일 타임에 강제됨. `feature:home`이 `core:database`를 직접 참조하려 하면 빌드 실패 |
| **팀 확장성** | 여러 개발자가 다른 feature 모듈을 병렬로 작업할 때 충돌 최소화 |
| **테스트 격리** | 모듈 단위로 테스트를 독립 실행 가능 |

---

## ADR-2. core:model — 순수 Kotlin 데이터 클래스 전용 모듈

### 결정

도메인 모델(DailyStepCount, MonthlyRecap, UserSettings 등)을 `core:domain`에서 분리해 **`core:model`** 독립 모듈로 이동했습니다.

### 이유

NiA의 `core:model` 패턴을 따릅니다.

```
이전: core:domain 안에 model/ + usecase/ + repository/ 혼재
NiA:  core:model (데이터 클래스만) + core:domain (use case만) + core:data (repository)
```

`core:model`이 Android 의존성 없는 순수 Kotlin 모듈이면:
- 단위 테스트가 JVM에서 즉시 실행됨 (Android 런타임 불필요)
- `core:database`, `core:datastore`, `core:data` 모두 `core:model`만 의존하면 되므로 순환 의존 없음
- 데이터 클래스 변경이 use case · repository와 독립적으로 이루어짐

---

## ADR-2-1. core:database · core:datastore가 core:model에만 의존하는 이유

### 결정

```
잘못된 구조: core:database → core:domain  (도메인 레이어를 아래에서 참조)
올바른 구조: core:database → core:model   (순수 데이터 클래스만 참조)
          core:data → core:database (api — model transitive 노출)
          core:domain → core:data
```

### 이유

Clean Architecture에서 DB 계층은 domain을 **알아서는 안 됩니다.**
DB 계층의 역할은 데이터를 SQL로 저장하고 읽는 것 이기 때문입니다.
Entity → Domain Model 변환은 Repository 구현체(`core:data`)의 책임입니다.

```kotlin
// core:data — StepRepositoryImpl.kt
// Entity → Domain Model 변환이 여기서만 일어남
private fun DailyStepEntity.toDomain() = DailyStepCount(
    dateEpochDay = dateEpochDay,
    steps = totalSteps,
    targetSteps = targetSteps,
)
```

`core:database`가 `api(projects.core.model)`로 모델을 노출하면, `core:data`는 `api(projects.core.database)` 하나로 Room 타입과 도메인 모델 타입을 모두 얻습니다. `core:domain`을 중간에 끼울 필요가 없으므로 레이어 간 순환 의존이 생기지 않습니다.

---

## ADR-3. CrashReporter 추상화

### 결정

Firebase Crashlytics를 feature 모듈이 직접 의존하지 않고 `core:analytics`의 `CrashReporter` 인터페이스로 추상화했습니다.

### 이유

```
직접 의존:
feature:home → firebase-crashlytics

문제:
- feature 모듈에 Firebase SDK가 노출됨
- 향후 Crashlytics를 Sentry 등으로 교체하려면 모든 feature 수정 필요
```

```
추상화:
feature:home → CrashReporter (core:analytics)
                      ↑
           CrashlyticsReporter (impl, app 레벨 Hilt 바인딩)

이점:
- feature 모듈은 Firebase를 모름
- Crashlytics → Sentry 교체 시 CrashlyticsReporter만 수정
```

```kotlin
// 테스트에서 사용 가능한 NoOpCrashReporter 구현
class NoOpCrashReporter : CrashReporter {
    override fun recordException(throwable: Throwable) = Unit
    override fun log(message: String) = Unit
    override fun setKey(key: String, value: String) = Unit
}
```

---

## ADR-3-1. Repository 인터페이스를 core:data에 두는 이유

### 결정

`StepRepository`, `UserSettingsRepository` 인터페이스를 `core:domain`이 아닌 **`core:data`** 에 뒀습니다.

### 이유

전통적 Clean Architecture는 "도메인이 Repository 계약을 소유한다"고 합니다. 하지만 NiA는 달랐습니다.

```
전통 CA:  core:domain (interface) ← core:data (impl)
NiA:      core:data (interface + impl)
          core:domain (use case) → core:data (interface 사용)
```

NiA 선택의 이유?:
- Repository는 데이터 접근 계약 — 본질적으로 "data layer의 공개 API"
- use case가 repository를 사용하므로, `core:domain → core:data` 방향이 더 자연스러움
- `core:domain`이 `core:data`를 의존하면 계층은 `feature → domain → data → database/datastore`로 일방향 유지

```kotlin
// core:domain use case가 core:data의 인터페이스를 import
class GetWeeklyStepSummaryUseCase @Inject constructor(
    private val stepRepository: StepRepository,
) {
    operator fun invoke(weekStartEpochDay: Long) =
        stepRepository.getWeeklyStepSummary(weekStartEpochDay)
}
```

단순 위임에 불과한 use case(ObserveLiveSteps, GetTodaySteps, CheckSensorAvailability)는 제거하고 해당 feature가 `StepRepository`를 직접 주입합니다.
비즈니스 로직이 있는 use case(GetWeeklyStepSummary, GetMonthlyRecap)만 `core:domain`에 유지합니다.

---

## ADR-4. XML Navigation + Fragment shell 구조

### 결정

순수 Compose Navigation 대신 **XML NavGraph + Fragment + ComposeView** 하이브리드 구조를 선택했습니다.

### 이유

실무에서 Compose를 도입할 때 기존 Fragment 기반 Navigation을 즉시 교체하기 어렵습니다. 그 공존 패턴을 직접 구현했습니다.

```kotlin
// HomeFragment.kt — Fragment가 ComposeView를 감싸는 패턴
class HomeFragment : Fragment() {
    override fun onCreateView(...): View = ComposeView(requireActivity()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            WalkLogTheme {
                HomeRoute(
                    onNavigateToWeeklyReport = {
                        findNavController().navigate(R.id.action_home_to_weeklyReport)
                    },
                    ...
                )
            }
        }
    }
}
```

`DisposeOnViewTreeLifecycleDestroyed` 전략을 사용해 Fragment의 View Lifecycle과 Compose의 컴포지션 생명주기를 올바르게 연동했습니다.

### 마이그레이션 경로

향후 Compose Navigation으로 전환 시:

```
현재:  MainActivity → XML NavGraph → Fragment → ComposeView → Route
이후:  MainActivity → ComposeView → NavHost → Route (직접)
```

각 Route 컴포저블은 이미 독립적으로 설계되어 있어 Fragment 래퍼만 제거하면 됩니다.

---

## ADR-5. UDF

### 결정

모든 ViewModel은 단방향 데이터 흐름을 따릅니다.

```
UI Event (UserIntent)
    ↓ handleIntent()
ViewModel
    ↓ StateFlow<UiState>
UI (Compose)
    ↓ collectAsStateWithLifecycle()
Screen
```

### 이유

```kotlin
// 잘못된 패턴 (양방향):
viewModel.setUserName("익명")          // UI가 ViewModel 상태를 직접 수정
val name = viewModel.userName          // UI가 ViewModel에서 직접 값을 읽음

// 올바른 패턴 (UDF):
// UI는 Intent만 전달
viewModel.handleIntent(HomeIntent.OnRefresh)

// ViewModel은 State만 노출
val state: StateFlow<HomeState> = _state.asStateFlow()

// UI는 State만 구독
val state by viewModel.state.collectAsStateWithLifecycle()
```

**이점:**
- UI 상태가 항상 `StateFlow`에서 단일 출처로 관리됨
- 상태 변경 흐름을 추적하기 쉬움
- `UiState`를 그대로 단위 테스트에 사용 가능

---

## ADR-6. Flow + catch 패턴

### 결정

`viewModelScope.launchIn()`으로 수집하는 모든 Flow에 `.catch { }` 핸들러를 붙였습니다.

### 이유

```kotlin
// 위험한 패턴:
getMonthlyRecap(year, month)
    .onEach { recap -> _state.update { ... } }
    .launchIn(viewModelScope)
// Flow가 예외를 던지면 코루틴이 취소됨 → UI 업데이트 중단 → 사용자는 빈 화면

// 안전한 패턴:
getMonthlyRecap(year, month)
    .onEach { recap -> _state.update { ... } }
    .catch { e -> crashReporter.recordException(e) }  // 예외 포착 + 기록
    .launchIn(viewModelScope)
// 예외가 발생해도 코루틴은 계속 실행됨
```

Kotlin Coroutine에서 `launchIn`은 `launch { flow.collect() }`와 동일합니다.
처리되지 않은 예외는 `CoroutineExceptionHandler`로 전파되거나 코루틴을 종료 시키게 됩니다.
`.catch`를 Flow 체인 안에 두면 예외를 Flow 레벨에서 처리하므로 코루틴 자체는 살아있습니다.

---

## ADR-7. Convention Plugin

### 결정

`build-logic` included build를 사용해 Convention Plugin을 관리합니다.

### 이유

```kotlin
// Convention Plugin 없이 (각 모듈이 직접 설정):
android {
    compileSdk = 35
    defaultConfig { minSdk = 28 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

// Convention Plugin 사용 후:
plugins {
    id("river.android.feature")
}
// compileSdk, minSdk, Java 버전, Hilt, Compose, 테스트 설정 모두 자동 적용
```

`build-logic` included build 방식은 buildSrc 대비 캐싱이 더 잘 됩니다.
buildSrc 변경 시 전체 재빌드가 발생하지만, included build는 Plugin 코드가 변경된 경우만 영향받습니다.

---

## ADR-8. Dispatcher 추상화

### 결정

`Dispatchers.IO`를 직접 사용하지 않고 `WalkLogDispatchers` 인터페이스로 주입했습니다.

### 이유

```kotlin
// core:common — WalkLogDispatchers
data class WalkLogDispatchers(
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher,
    val main: CoroutineDispatcher,
)

// 테스트에서 교체 가능:
val testDispatchers = WalkLogDispatchers(
    io = UnconfinedTestDispatcher(),
    default = UnconfinedTestDispatcher(),
    main = UnconfinedTestDispatcher(),
)
```

`Dispatchers.IO`를 직접 참조하면 단위 테스트에서 실제 스레드 풀이 생성됩니다.
`UnconfinedTestDispatcher`로 교체하면 코루틴이 즉시 실행되어 테스트가 빠르고 예측 가능해집니다.

---

## ADR-9. Bottom Navigation

### 결정

단일 홈 화면 대신 **홈 · 기록 · 리워드 · 설정**  BottomNavigationView를 도입했습니다.

### 이유

기능이 늘어나면서 모든 진입점을 홈에 두면 홈 화면이 비대해지고 탐색 경로가 불명확해집니다.
사용자 행동 흐름을 "오늘 걸음(홈) → 지난 기록(기록) → 리워드(보상) → 개인 설정(설정)"으로 분리하면 각 탭이 단일 책임을 갖습니다.

```kotlin
// MainActivity.kt — 상위 탭에서만 BottomNav 표시
private val bottomNavDestinations = setOf(
    R.id.homeFragment,
    R.id.historyFragment,
    ...
)

navController.addOnDestinationChangedListener { _, destination, _ ->
    bottomNav.isVisible = destination.id in bottomNavDestinations
}
```

상세 화면(WeeklyReport, MissionDetail, Recap 등)은 탭 위에 쌓이는 구조이므로 해당 화면에서는 BottomNav를 숨깁니다. `NavigationUI.setupWithNavController`로 탭 선택 상태를 NavController와 자동 동기화합니다.

### 트레이드오프

탭 구조를 XML NavGraph의 Fragment id와 1:1 매핑해야 합니다.
`menu/bottom_nav_menu.xml`의 item id와 `nav_graph.xml`의 Fragment destination id가 동일해야 `setupWithNavController`가 올바르게 동작합니다.

---

## ADR-10. feature:settings · feature:history

### 결정

Settings와 History 화면을 Compose가 아닌 **XML Fragment + ViewBinding**으로 구현했습니다.

### 이유

포트폴리오 목적도 있습니다. Compose만 쓰면 실무에서 흔한 XML 레이아웃 작업을 보여줄 수 없어 History · Settings를 XML로 구현했습니다.

```
Compose 화면  → Fragment + ComposeView 래퍼 → XML NavGraph
XML 화면      → Fragment + ViewBinding     → XML NavGraph
```

두 방식 모두 동일한 NavGraph, 동일한 BottomNav, 동일한 ViewModel 패턴을 공유합니다. UI 레이어만 다를 뿐 아키텍처는 일관됩니다.

### 트레이드오프

XML Fragment는 `fragment-ktx`(`by viewModels()`)를 명시적으로 의존성에 추가해야 합니다. `river.android.feature` Convention Plugin이 자동 포함하지 않으므로 각 XML Fragment 모듈의 `build.gradle.kts`에 직접 선언합니다.

---

## ADR-11. DataStore 계층 분리

### 결정

사용자 설정 저장소를 **NiA(Now in Android) 패턴**에 따라 두 레이어로 분리했습니다.

```
feature:settings  ──→  core:data (UserSettingsRepository 인터페이스)
feature:onboarding ──→  core:data
MainActivity       ──→  core:data
                              ↑ implements
                   DefaultUserSettingsRepository (core:data)
                              ↓ delegates
                   UserPreferencesDataSource (core:datastore)
```

### 이유

NiA의 `OfflineFirstUserDataRepository` 패턴을 따릅니다.

| 역할 | 위치 | 책임 |
|---|---|---|
| Repository 인터페이스 | `core:data` | 비즈니스 계약 정의 |
| Repository 구현체 | `core:data` | DataSource 위임, 향후 캐싱/분석 로직 추가 가능 |
| DataSource | `core:datastore` | DataStore 읽기/쓰기만 담당 |

`core:datastore`를 Repository 구현체로 쓰지 않는 이유:

```
이전 구조: UserPreferencesDataStore implements UserSettingsRepository
   → core:datastore가 domain 계약을 직접 구현 — 역할 과적재
   → 향후 캐싱, 원격 설정 동기화, Analytics 로깅 추가 시 DataSource에 비즈니스 로직이 섞임

NiA 구조: DataSource는 저장만, Repository가 조합 책임
   → DefaultUserSettingsRepository에서 분석 이벤트, 마이그레이션 로직 추가 가능
   → DataSource 교체(Room, 서버 동기화 등)가 Repository에 영향 없음
```

`core:database`와 `core:datastore`가 각각 `api(projects.core.model)`로 모델 타입을 노출하고, `core:data`는 `api(projects.core.database)` · `api(projects.core.datastore)`로 전이적으로 전달을합니다.
feature 모듈이 `core:data` 하나만 의존해도 `UserSettings`, `ThemeMode` 등 도메인 타입을 별도 선언 없이 사용할 수 있습니다.

---

## ADR-12. ThemeMode — AppCompatDelegate DarkMode 전환

### 결정

다크모드를 Compose `isSystemInDarkTheme()`만으로 처리하지 않고, **`AppCompatDelegate.setDefaultNightMode()`** 로 전체 앱 테마를 전환했습니다.

### 이유

본 프로젝트는 Compose 화면과 XML 화면이 혼용됩니다. Compose만의 다크모드(`isSystemInDarkTheme()`)는 Compose 영역에만 적용됩니다.
XML 리소스(`@color/walklog_surface`, BottomNavigationView 등)는 `night` 리소스 한정자 기반 시스템 테마를 따릅니다.

`AppCompatDelegate.setDefaultNightMode()`는 앱 전체의 `Configuration.uiMode`를 변경하므로 Compose와 XML 두 영역을 동시에 전환합니다.

```kotlin
// MainActivity.kt
private fun observeThemeMode() {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            userPreferencesDataStore.preferences
                .map { it.themeMode }
                .distinctUntilChanged()
                .collect { themeMode ->
                    val nightMode = themeMode.toNightMode()
                    if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
                        AppCompatDelegate.setDefaultNightMode(nightMode)
                    }
                }
        }
    }
}

private fun ThemeMode.toNightMode(): Int = when (this) {
    ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    ThemeMode.LIGHT  -> AppCompatDelegate.MODE_NIGHT_NO
    ThemeMode.DARK   -> AppCompatDelegate.MODE_NIGHT_YES
}
```

`distinctUntilChanged()`로 동일 테마로의 중복 적용을 방지하고, 현재 모드와 다를 때만 `setDefaultNightMode`를 호출해 불필요한 Activity Recreation을 억제합니다.

---

## ADR-13. core:native — JNI/NDK 분석 엔진 분리

### 결정

걷기 데이터 분석 로직을 `core:native` 모듈의 C++ 엔진으로 분리하고, Android 앱은 JNI를 통해 결과를 소비하도록 설계했습니다.

```
feature:forecast / feature:home
        ↓ inject
WalkingInsightsEngine.kt   (JNI 브리지)
        ↓ System.loadLibrary("walking_insights")
libwalking_insights.so     (C++ — walking_insights_engine.cpp)
```

### 이유

시간대 패턴 분석, 주간 추세 계산, 스트릭 리스크 산출은 7일 × 24시간의 배치 수치 연산입니다. C++에서 처리하면:

- 부동소수점 배치 연산을 JVM GC 영향 없이 실행
- 향후 SIMD 최적화 또는 다른 플랫폼(iOS) 재사용 경로 확보
- JNI 인터페이스가 `FloatArray` ↔ `const float*` 단순 매핑이므로 오버헤드 최소

JNI 데이터 흐름:

```
Kotlin FloatArray(days × 24)
    ↓ GetFloatArrayElements / JNI_ABORT (read-only)
C++ const float* → analyze() → WalkingInsights struct
    ↓ SetFloatArrayRegion
Kotlin FloatArray(4) → WalkingInsightsResult
```


---

## ADR-14. LiteRT 온디바이스 추론

### 결정

스마트폰 가속도계 · 자이로스코프 데이터를 기반으로 사용자의 활동 상태(WALKING / STATIONARY / UNKNOWN)를 분류하는 LiteRT 모델을 `core:native`에 통합했습니다.

```
SensorManager (accel + gyro)
        ↓ 50Hz 슬라이딩 윈도우 수집
ActivityClassifier.classify(sensorWindow: FloatArray)
        ↓ Interpreter.run([1, 50, 6] → [1, 3])
ActivityState (WALKING / STATIONARY / UNKNOWN)
        ↓
feature:forecast (예보 보정 신호)
feature:home     (걷기 상태 확인)
```

### 이유

`TYPE_STEP_COUNTER`는 누적 걸음 수만 반환합니다. 사용자가 실제로 걷고 있는지, 기기를 흔들거나 차량에 탑승한 건지는 구분하지 않습니다.
센서 퓨전 기반 HAR 모델을 보조 신호로 사용하면:

- 예보 화면: 실제 걷기 중인 시간대만 집계해 피크 시간대 정확도 향상
- 미션 화면: 목표 달성 여부를 걸음 수 + 활동 상태로 교차 확인

LiteRT를 선택한 이유:

| 항목 | LiteRT | ONNX Runtime |
|---|---|---|
| Android 공식 지원 | Google 공식 | Microsoft 공식 |
| Kotlin API | Interpreter / Task API | OrtSession |
| int8 양자화 | 기본 지원 | 지원 |
| Firebase ML 연동 | 자연스러움 | 별도 설정 필요 |

### 트레이드오프

모델 파일(`.tflite`)은 별도 변환 및 배포 관리가 필요합니다. 초기 버전은 UCI HAR Dataset 기반 공개 모델을 변환해 사용하며, 정확도 개선은 온디바이스 피드백 루프로 확장 가능합니다.

---

## ADR-15. Health Connect Android 14+ 매니페스트 요구사항

### 결정

HC 권한 다이얼로그가 표시되려면 `AndroidManifest.xml`에 **두 가지** `activity-alias`를 모두 선언해야 합니다.

### 이유

Android 14(API 34)부터 HC는 앱이 권한 사용 현황 화면을 제공할 수 있는지 확인합니다. `VIEW_PERMISSION_USAGE` + `HEALTH_PERMISSIONS` alias가 없으면 HC가 해당 앱에 대해 권한 다이얼로그를 표시하지 않습니다.

```xml
<!-- 1. 개인정보 처리방침 진입점 (모든 HC 버전 필수) -->
<activity-alias
    android:name=".HealthConnectPrivacyRationaleActivity"
    android:exported="true"
    android:targetActivity=".MainActivity">
    <intent-filter>
        <action android:name="androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE" />
    </intent-filter>
</activity-alias>

<!-- 2. 권한 사용 현황 진입점 (Android 14+ HC 권한 다이얼로그 표시에 필수) -->
<activity-alias
    android:name=".HealthConnectPermissionUsageActivity"
    android:exported="true"
    android:permission="android.permission.START_VIEW_PERMISSION_USAGE"
    android:targetActivity=".MainActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW_PERMISSION_USAGE" />
        <category android:name="android.intent.category.HEALTH_PERMISSIONS" />
    </intent-filter>
</activity-alias>
```

`android:permission="android.permission.START_VIEW_PERMISSION_USAGE"` 속성은 이 인텐트를 시스템만 발송할 수 있도록 제한합니다 — 임의 앱이 이 화면을 직접 시작하는 것을 방지합니다.

### `<queries>` 섹션

HC 패키지 가시성(Android 11+ 패키지 쿼리 제한)을 위해 `<queries>`에도 두 인텐트를 선언해야 합니다.

```xml
<queries>
    <package android:name="com.google.android.apps.healthdata" />
    <intent>
        <action android:name="androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE" />
    </intent>
    <intent>
        <action android:name="android.intent.action.VIEW_PERMISSION_USAGE" />
        <category android:name="android.intent.category.HEALTH_PERMISSIONS" />
    </intent>
</queries>
```

---
