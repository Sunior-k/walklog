# 아키텍처 결정 기록

> 각 설계 결정의 **이유**와 **트레이드오프**를 기록했습니다.
>
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
UI Event (함수 호출)
    ↓ viewModel.refresh() / viewModel.updatePermissionResult(granted)
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
// UI는 ViewModel의 공개 함수를 직접 호출
viewModel.refresh()
viewModel.refreshWeather()

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

WalkLog의 걸음 수 산정과 미션 달성 판단은 **Health Connect `StepsRecord.COUNT_TOTAL` 집계값**을 기준으로 합니다.

LiteRT 기반 `ActivityClassifier`는 걸음 수의 source of truth가 아니라, 사용자의 현재 활동 상태(WALKING / STATIONARY / UNKNOWN)를 분류하기 위한 **보조 파이프라인**입니다.

현재 저장소에는 다음 코드만 포함되어 있습니다.

- `ActivitySensorCollector`: 가속도계 · 자이로스코프 센서 윈도우 수집
- `ActivityClassifier`: LiteRT `Interpreter` 래핑 및 활동 상태 분류
- `ActivityStateProvider`: `isStationary` 상태를 외부에 노출하는 인터페이스

실제 모델 파일(`activity_classifier.tflite`)은 아직 배치하지 않았습니다. 모델 파일이 없으면 `ActivityClassifier`는 모든 입력에 대해 `UNKNOWN`을 반환합니다.

```
Health Connect StepsRecord.COUNT_TOTAL
        ↓ AggregateRequest / aggregateGroupByDuration
StepRepository
        ↓
Home / Mission / History / Report

SensorManager (accel + gyro)
        ↓ SENSOR_DELAY_FASTEST (accel 이벤트 주도, gyro 값을 hold·합성)
ActivitySensorCollector
        ↓ FloatArray(300) — 50샘플 × 6채널 non-overlapping 윈도우 emit
ActivityClassifier
        ↓ Interpreter.run([1, 50, 6] → [1, 3])
ActivityState (WALKING / STATIONARY / UNKNOWN)
        ├─ isStationary: StateFlow<Boolean>  ← ActivityStateProvider 구현체로 Hilt 바인딩
        └─ HomeState.activityState (보조 상태 표시 후보)
```

### 이유

Health Connect는 기기와 건강 앱이 기록한 걸음 데이터를 앱이 직접 센서를 소유하지 않고 읽을 수 있게 해줍니다. `AggregateRequest`는 중복 제거를 적용하므로 복수 앱이 같은 구간을 기록해도 WalkLog는 단일 집계값을 기준으로 화면과 미션을 계산할 수 있습니다.

LiteRT 파이프라인은 Health Connect 경로와 분리했습니다.

| 구성 요소 | 책임 |
|---|---|
| `ActivitySensorCollector` | `SENSOR_DELAY_FASTEST`로 가속도계 · 자이로스코프를 수집하고, `[ax, ay, az, gx, gy, gz]` 샘플 50개를 `FloatArray(300)`으로 emit |
| `ActivityClassifier` | 센서 윈도우를 `[1, 50, 6]` 텐서로 변환하고 LiteRT `Interpreter`로 활동 상태를 추론 |
| `ActivityStateProvider` | `isStationary: StateFlow<Boolean>`을 외부 레이어에 노출 |

위 구조는 센서 수집과 추론 책임을 분리합니다. `ActivityClassifier`는 `ActivityStateProvider`를 구현하고, Hilt `SingletonComponent`에서 `provideActivityStateProvider(classifier)`로 바인딩됩니다.

센서 퓨전 기반 HAR 모델을 별도 보조 신호로 두면:

- 홈 화면: 사용자가 현재 걷고 있는지 여부를 별도 상태로 노출할 수 있음
- 향후 실험: HC 걸음 수와 활동 상태를 비교해 기기 흔들림 같은 오탐 가능성을 분석할 수 있음

LiteRT를 선택한 이유:

| 항목 | LiteRT | ONNX Runtime |
|---|---|---|
| Android 공식 지원 | Google 공식 | Microsoft 공식 |
| Kotlin API | Interpreter / Task API | OrtSession |
| int8 양자화 | 기본 지원 | 지원 |
| Firebase ML 연동 | 자연스러움 | 별도 설정 필요 |

### 트레이드오프

Health Connect를 기준으로 하면 앱이 자체 센서 수집으로 걸음 수를 재계산하지 않아도 됩니다. 데이터 출처가 일관되고 배터리 비용도 낮습니다.

대신 플랫폼 권한과 Health Connect 사용 가능 여부에 의존합니다. 실시간성도 Health Connect 갱신 주기와 권한 상태의 영향을 받습니다.

LiteRT 모델 파일(`.tflite`)은 별도 변환 및 배포 관리가 필요합니다. 모델을 앱에 포함하기 전까지는 코드 경로만 존재하고 실제 HAR 분류는 활성화되지 않습니다.

초기 버전은 UCI HAR Dataset 기반 공개 모델을 변환해 사용할 수 있습니다. 다만 스마트폰 위치와 사용자 보행 패턴에 따른 정확도 편차가 크므로, 모델 배치 후에는 실기기 센서 로그로 다음 항목을 검증해야 합니다.

- 입력 스케일
- 클래스 인덱스 매핑
- 배터리 사용량

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

## ADR-16. core:auth 분리 — Firebase Auth + Credential Manager

### 결정

Google 로그인 로직을 `feature:onboarding`·`feature:settings` 등에 직접 두지 않고, **`core:auth`** 독립 모듈로 분리했습니다.

```
feature:onboarding / feature:login / feature:settings
        ↓ inject
SignInWithGoogleUseCase / SignOutUseCase  (core:domain)
        ↓
AuthRepository  (interface, core:auth)
        ↓ binds
FirebaseAuthRepository  (impl, core:auth — callbackFlow<AuthUser?>)
```

### 이유

Firebase Auth를 feature 모듈이 직접 의존하면:

- 여러 feature에 Firebase SDK가 노출됨
- 로그인 제공자를 교체(Firebase → 자체 OAuth 등)하면 모든 feature 수정 필요
- 단위 테스트에서 Firebase 의존성을 끊기 어려움

`core:auth`가 `AuthRepository` 인터페이스를 제공하고 구현체(`FirebaseAuthRepository`)를 Hilt로 바인딩하면:

- feature 모듈은 `AuthRepository`만 알고 Firebase를 모름
- 로그인 제공자 교체 시 `FirebaseAuthRepository`만 교체
- 테스트에서 `FakeAuthRepository`로 대체 가능

**Credential Manager 선택 이유:**

기존 `GoogleSignInClient`(legacy) 대신 Android Credential Manager API를 사용합니다.

| 항목 | Credential Manager | GoogleSignInClient (legacy) |
|---|---|---|
| Android 공식 권장 | Android 14+ 권장 | Deprecated |
| Passkey 지원 | 기본 지원 | 미지원 |
| 비동기 모델 | suspend 함수 | Task/callback |
| One Tap 통합 | GetSignInWithGoogleOption | 별도 설정 |

`getGoogleIdToken()` 함수가 `suspend` 함수로 구현되어 있어 Kotlin coroutine과 자연스럽게 통합됩니다.

### AuthStateListener → callbackFlow

```kotlin
// FirebaseAuthRepository.kt
override val currentUser: Flow<AuthUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser?.toAuthUser())
    }
    firebaseAuth.addAuthStateListener(listener)
    awaitClose { firebaseAuth.removeAuthStateListener(listener) }
}
```

`MainActivityViewModel`이 이 Flow를 구독해 앱 실행 중 세션 만료·로그아웃을 감지하고, `signOutEvent`를 `LoginFragment`로 전달합니다.

---

## ADR-17. Firestore 동기화 + sync:work 모듈

### 결정

사용자 설정(닉네임·포인트·목표·알림 등)을 기기 간에 유지하기 위해 **Firebase Firestore** 원격 저장소와 **`sync:work`** 모듈을 추가했습니다.

```
DataStoreUserSettingsRepository (core:data)
    implements Syncable
    ↓ sync()
FirestoreUserSettingsDataSource (core:data)
    ↓ Firestore: users/{uid}/data/settings

WorkManagerSyncManager (sync:work)
    implements SyncManager
    ↓ requestSync()
UserSettingsSyncWorker (sync:work, HiltWorker)
    ↓ UserSettingsRepository.sync()
```

### Syncable / SyncManager 인터페이스

```kotlin
// core:data
interface Syncable { suspend fun sync(): Boolean }
interface SyncManager { fun requestSync() }
```

Repository가 `Syncable`을 구현하고, `SyncManager` 구현체(`WorkManagerSyncManager`)가 WorkManager를 통해 네트워크 연결 조건부로 동기화를 예약합니다.

### sync() 로직 (merge 전략)

1. DataStore에서 로컬 설정 읽기
2. Firestore에서 원격 설정 fetch
3. 원격 값이 있으면 로컬에 병합 (재설치 후 데이터 복원)
4. 병합된 최신 로컬 값을 Firestore에 업로드 (최신 상태 반영)

```kotlin
// merge 규칙 요약
nickname       = remote.nickname 우선 (비어있지 않으면)
totalPoints    = max(local, remote)   // 포인트는 손실 방지
dailyStepGoal  = remote.dailyStepGoal 우선 (0이 아니면)
isOnboardingCompleted = local OR remote
```

포인트는 `max()` 를 취해 어느 기기에서 더 많이 쌓았든 손실이 없습니다.

### Firestore 경로

```
users/{uid}/data/settings  (FirestoreUserSettings 문서)
```

Firestore Security Rules에서 `request.auth.uid == userId` 조건으로 본인 문서만 읽기·쓰기 허용합니다.

### 이유

DataStore는 기기 로컬 저장소이므로 재설치 시 데이터가 소실됩니다. Firestore 동기화로 기기 교체·재설치 후에도 닉네임·포인트·목표가 복원됩니다. WorkManager의 `KEEP` 정책(`ExistingWorkPolicy.KEEP`)을 사용해 동일한 sync 작업이 중복 예약되지 않습니다.

### 트레이드오프

- 로그인하지 않은 사용자(`userId == ""`)는 sync를 건너뜀 — 로컬 전용으로 동작
- 오프라인이면 WorkManager가 네트워크 연결 시점에 자동 재시도
- Firestore 쓰기 비용이 발생하지만, 설정 문서는 1개이므로 MAU 규모에서 무시할 수준

---

## ADR-18. React Native Brownfield — 리워드 스토어

### 결정

리워드 스토어 화면 하나를 위해 앱 전체를 React Native로 전환하지 않고, [Toss SLASH23 발표](https://toss.tech)의 브라운필드 패턴을 참고해 **기존 네이티브 앱에 React Native 화면 한 개를 임베드**했습니다. RN 프로젝트는 별도 Gradle/Node 루트(`reward-store-rn/`)에서 독립적으로 개발하고, `@callstack/react-native-brownfield`로 패키징한 Fat AAR을 `app` 모듈이 소비합니다.

```
reward-store-rn/  (독립 Node/Gradle 루트, RN 0.86.2)
    ↓ brownfield package:android + publish:android
com.river.walklog:reactnativeapp:0.0.1-local  (mavenLocal AAR)
    ↓ consumed by
app/build.gradle.kts
    ↓
ReactNativeBrownfield.initialize(application, listOf(RewardBridgePackage()))
    ↓
RewardStoreFragment → ReactNativeFragment("RewardStoreApp")
```

### 이유

전체 네이티브 재작성 없이 RN 생태계(빠른 반복, 크로스플랫폼 컴포넌트)를 실험적으로 도입할 수 있는 최소 단위가 화면 하나였습니다. 앱 전체를 RN으로 옮기면 기존 Compose 디자인시스템·Health Connect·NDK 엔진 통합을 모두 다시 만들어야 하지만, 브라운필드 방식은 기존 아키텍처를 건드리지 않고 한 화면만 교체할 수 있습니다.

### 네이티브 ↔ RN 통신 — RewardBridgeModule

RN 쪽은 네이티브 도메인 로직에 직접 접근할 수 없으므로, Hilt `@EntryPoint`로 UseCase들을 RN의 `NativeModule`(`RewardBridgeModule`)에 노출합니다.

```kotlin
@EntryPoint
@InstallIn(SingletonComponent::class)
interface RewardBridgeEntryPoint {
    fun getPointsBalanceUseCase(): GetPointsBalanceUseCase
    fun redeemRewardUseCase(): RedeemRewardUseCase
    fun getIssuedCouponsUseCase(): GetIssuedCouponsUseCase
    fun markCouponUsedUseCase(): MarkCouponUsedUseCase
    fun getActiveThemeUseCase(): GetActiveThemeUseCase
    fun getRewardCatalogUseCase(): GetRewardCatalogUseCase
    fun redeemPromoCodeUseCase(): RedeemPromoCodeUseCase
    fun authRepository(): AuthRepository
}
```

`RewardBridgeModule`은 이 EntryPoint를 `EntryPointAccessors.fromApplication()`으로 얻어 각 `@ReactMethod`에서 UseCase를 호출하고, 결과를 `Promise`로 RN에 반환합니다. RN이 로그인 화면으로 이동해야 할 때는 `reactApplicationContext.currentActivity as? MainActivity`로 캐스팅해 `MainActivity.navigateToLogin()`(내부적으로 `findNavController().navigate(R.id.loginFragment)`)을 직접 호출합니다.

### RewardStoreViewHost — RN 루트 View 재사용

`ReactNativeBrownfield.createView()`는 화면에 진입할 때마다 시스템 back 콜백(`OnBackPressedCallback`)을 새로 등록하고 절대 remove하지 않는 라이브러리 버그가 있습니다. 화면을 나갔다가 다시 들어올 때마다 죽은 콜백이 디스패처에 쌓이고, 두 번째 진입부터 시스템 back이 먹히지 않게 됩니다. `RewardStoreViewHost`가 RN 루트 View를 앱 생애주기 동안 단 하나만 만들어 캐싱하고, `RewardStoreFragment`는 이 View를 컨테이너에 붙였다 뗐다만 하는 방식으로 이 누수를 원천 차단합니다. RN 쪽 컴포넌트는 View 재사용으로 다시 마운트되지 않으므로, 화면 재진입 시 잔액/쿠폰/로그인 상태를 다시 조회하도록 네이티브 → JS `postMessage({"type": "SCREEN_FOCUSED"})` 신호를 별도로 보냅니다.

### RN ↔ 네이티브 디자인시스템 정합

RN은 Kotlin 모듈(`core:designsystem`)을 직접 참조할 수 없으므로, `reward-store-rn/src/theme/walklogColors.ts`에 실제 hex 값을 수동으로 미러링합니다. `rewardCatalog.ts`의 `RewardCatalogIds`가 `core/model/RewardCatalogIds.kt`와 동일한 패턴으로 수동 동기화되는 것과 같은 방식입니다. RN은 `getThemeState()` 브릿지로 현재 라이트/다크/프리미엄 상태를 읽어 세 팔레트 중 하나를 선택해 렌더링합니다.

### RN 프로젝트 구조

```
reward-store-rn/
  App.tsx                        조립 루트 — useRewardStore() + 하위 컴포넌트 배치만
  src/
    hooks/useRewardStore.ts      상태·이펙트·브릿지 호출 전부
    components/                  SignInBanner, RewardCard, CouponSection,
                                  CouponDetailModal, PromoCodeModal
    styles/createStyles.ts       WalkLogPalette → StyleSheet
    nativeModules/RewardBridge.ts  네이티브 브릿지 타입/시그니처
    theme/walklogColors.ts       디자인 토큰 미러링
    data/rewardCatalog.ts        오프라인 폴백 카탈로그
```

초기에는 `App.tsx` 한 파일에 상태·이펙트·5개 하위 컴포넌트·스타일이 모두 들어 있었습니다. 기능이 늘어날수록(코드 등록, 동적 카탈로그 등) 한 파일에서 관련 없는 변경들이 서로 충돌하기 쉬워져, 상태/이펙트를 `useRewardStore` 훅으로, 각 UI 조각을 개별 컴포넌트 파일로, 스타일을 별도 모듈로 분리했습니다.

### 트레이드오프

- **AAR이 `mavenLocal()`에만 게시됨** — CI나 새로 클론한 환경에서는 `reward-store-rn/`에서 `npx brownfield package:android && publish:android`를 먼저 실행해야 `app` 모듈이 빌드됨. 저장소에 AAR을 커밋하거나 사내 Maven 레포로 옮기는 작업이 남아있음
- **AAR을 항상 Release variant로 패키징** — JS 번들이 AAR 안에 내장되므로 `app` 모듈의 debug/release 빌드 타입과 무관하게 Metro 없이 동작함. `app/src/debug/res/xml/network_security_config.xml`의 Metro cleartext 허용 설정은 Debug variant로 다시 패키징해 핫 리로드로 개발할 때만 쓰는 예비용이며, 기본 워크플로우에서는 사용하지 않음
- **디자인 토큰 수동 동기화** — Kotlin 쪽 색상이 바뀌면 `walklogColors.ts`도 사람이 직접 맞춰야 함(공유 스키마 수단 없음)
- Hermes 버전은 하드코딩하지 않고 `com.facebook.react:hermes-android` 좌표를 RNGP가 자동 치환하도록 두고, `app` 모듈(비-RNGP 프로젝트)에서는 `resolutionStrategy.dependencySubstitution`으로 명시적 버전을 매핑

---

## ADR-19. Firestore 기반 쿠폰 발급/사용 — 서버 없는 환경의 위조 방지

### 결정

아메리카노 쿠폰 교환을 로컬 Room이 아니라 **Firestore**(`couponRedemptions/{code}`)에 저장하도록 결정했습니다. Cloud Functions는 두지 않고, 클라이언트가 직접 문서를 생성/수정하되 **Security Rules**로 위조·중복 발급·소유권 침해를 막습니다.

```
IssueCouponUseCase / GetIssuedCouponsUseCase / MarkCouponUsedUseCase  (core:domain)
    ↓
CouponRepository  (interface, core:data)
    ↓ binds
FirestoreCouponRepository  (impl)
    ↓
FirestoreCouponDataSource
    ↓ 문서 ID = 쿠폰 코드
couponRedemptions/{code}  { userId, rewardId, pointsCost, status, createdAt, usedAt }
```

### 이유

로컬 Room에만 코드를 저장하면 앱을 지우거나 기기를 바꾸면 쿠폰이 사라지고, 같은 코드를 여러 기기에 복제해 중복 사용하는 것도 막을 방법이 없습니다. Firestore로 옮기면 기기 독립적으로 코드가 유지되고, 문서 ID를 코드 자체로 쓰면 **Firestore가 동일 ID의 중복 `create`를 원천적으로 거부**하는 성질을 그대로 활용할 수 있습니다.

코드 생성은 클라이언트에서 하지만(`"WALK-" + 6자리 랜덤`), 발급 시 충돌(같은 코드가 이미 존재)하면 최대 5회까지 새 코드로 재시도합니다.

```kotlin
// FirestoreCouponDataSource.kt
suspend fun issueCoupon(userId: String, rewardId: String, pointsCost: Int): Coupon {
    repeat(MAX_CODE_ATTEMPTS) {
        val code = "WALK-${randomCode()}"
        runCatching { firestore.runTransaction { ... }.await() }
            .onSuccess { return it }
        // 충돌(이미 존재하는 코드)이면 다음 시도로
    }
    error("코드 발급 실패")
}
```

보안 규칙 상세는 [보안 설계 문서 7장](security.md#7-firestore-security-rules--쿠폰-위조중복-등록-방지)에 정리했습니다.

### 트레이드오프

- Cloud Functions가 없어 **서버 서명 발급이 아님** — Security Rules는 "인증된 소유자만, 정해진 필드로, ISSUED→USED 단방향으로만" 강제할 뿐, 포인트 차감과 쿠폰 발급이 하나의 원자적 트랜잭션으로 묶여있지 않음
- 로그인하지 않은 사용자는 `RedeemRewardUseCase`가 Firestore 호출 전에 `RedeemResult.SignInRequired`로 걸러냄 — 게스트는 쿠폰 상품을 볼 수는 있지만 교환은 로그인 후에만 가능
- `firestore.rules`는 저장소에 파일로만 존재하고, 실제 배포는 `firebase deploy --only firestore:rules`를 별도로 실행해야 함(CI에 자동화되어 있지 않음)

---

## ADR-20. RedeemRewardUseCase 결과 타입 — sealed interface로 표현한 3가지 분기

### 결정

리워드 교환 결과를 `Boolean`(성공/실패) 대신 **`sealed interface RedeemResult`** 로 표현했습니다.

```kotlin
sealed interface RedeemResult {
    data object Success : RedeemResult
    data object InsufficientBalance : RedeemResult
    data object SignInRequired : RedeemResult
}
```

### 이유

교환이 실패하는 이유는 "포인트 부족"과 "로그인 필요" 두 가지로 서로 다른 UI 반응(전자는 알럿, 후자는 로그인 화면 유도)이 필요합니다. `Boolean`이나 `Result<Unit>`으로는 이 둘을 구분할 수 없어 호출부(RN 브릿지, `RewardBridgeModule.redeemReward`)가 실패 사유를 알 수 없었습니다. `sealed interface`로 표현하면 `when` 분기에서 컴파일러가 누락을 잡아주고, RN 쪽에는 문자열 상태(`"SUCCESS" | "INSUFFICIENT_BALANCE" | "SIGN_IN_REQUIRED"`)로 직렬화해 전달합니다.

이 UseCase는 상품 종류별 부수 효과(쿠폰 발급/뱃지 기록/기부 합산/테마 활성화)도 함께 실행합니다 — `RewardCatalogIds`로 분기하는 단일 UseCase에 몰아둔 이유는 "포인트 차감 → 부수 효과 → 기록"이 하나의 흐름으로 실패 없이 순서대로 일어나야 하기 때문입니다(Cloud Functions 트랜잭션이 없는 대신 클라이언트에서 순서를 보장).

### 트레이드오프

- 신규 상품 타입이 늘어날수록 `RedeemRewardUseCase` 내부의 `if (rewardId == ...)` 분기가 함께 늘어남 — 상품이 크게 늘어나면 전략 패턴(`RewardEffectHandler` 같은 인터페이스)으로 리팩터링이 필요할 수 있음
- `RedeemResult`는 `core:domain`에만 존재하므로 RN(TypeScript) 쪽은 문자열 리터럴 유니온(`RedeemResult` in `RewardBridge.ts`)으로 별도 정의 — Kotlin `sealed interface`가 바뀌면 TS 쪽도 수동으로 맞춰야 함

---

## ADR-21. 리워드 적립/보유 내역의 Firestore 백업 — 재설치 시 데이터 유실 방지

### 결정

걸음 목표 달성 포인트 적립(`PointsLedgerEntry`)과 리워드 교환 기록(`RewardRedemption`, 뱃지·테마 팩 소유 여부의 근거)은 기존엔 Room에만 저장되어 있었습니다. 앱 재설치나 기기 변경 후 재로그인하면 이 데이터가 영구히 사라지는 문제가 있어, 쿠폰(ADR-19)과 동일하게 **Firestore를 백업 대상으로 추가**했습니다. 단, 오프라인 우선 동작은 유지해야 하므로 Firestore를 진실의 원천으로 바꾸지는 않고, Room을 그대로 두고 **양방향 push/pull 동기화**를 추가하는 방식을 택했습니다.

```
RoomRewardRedemptionRepository : RewardRedemptionRepository, Syncable
    ↓ recordRedemption() 시
Room INSERT (항상) → 로그인 상태면 Firestore에도 업로드 → 발급된 문서 ID를 remoteId 컬럼에 기록
    ↓ sync() 시 (AppDataSyncWorker가 호출)
1. remoteId가 null인 로컬 행을 Firestore로 push
2. Firestore에만 있고 로컬에 없는 문서를 pull (remoteId 집합으로 중복 제거)
```

`PointsLedgerEntity`/`RewardRedemptionEntity`에 nullable `remoteId: String?` 컬럼을 추가해(Room 스키마 v3→v4 마이그레이션) "아직 Firestore에 올리지 않음(null)"과 "이미 백업됨"을 구분합니다.

### 이유

- Room을 진실의 원천으로 유지해야 오프라인 상태에서도 즉시 적립/기록이 가능함 — Firestore를 유일한 저장소로 바꾸면 매 걸음 목표 달성마다 네트워크 왕복이 필요해짐
- 게스트(로그인하지 않은 사용자)는 Firestore에 쓸 수 없으므로(Security Rules가 `request.auth != null` 요구) 업로드를 조건부로 건너뛰고, 로컬 기록만 남김 — 이후 로그인하면 다음 `sync()`에서 push됨
- `Syncable`을 레포지토리 인터페이스 자체(`RewardRedemptionRepository`)에 넣지 않고 구현체에만 붙인 이유는 `DataStoreUserSettingsRepository`가 이미 쓰던 것과 같은 패턴(`as? Syncable` 캐스팅)을 그대로 따른 것 — 인터페이스를 core:domain의 Fake로도 구현해야 하는데, 도메인 계층은 "동기화된다"는 사실 자체를 알 필요가 없음

### 트레이드오프

- push/pull이 원자적 트랜잭션이 아니라, 동시에 여러 기기에서 같은 계정으로 오프라인 기록을 쌓다가 동기화하면 중복 없이 합쳐지긴 하지만 순서는 보장되지 않음(정렬은 UI 레이어에서 `createdAtEpochMillis` 기준으로 처리)
- `AppDataSyncWorker`는 앱 시작 시 1회만 실행(`WorkManager` `ExistingWorkPolicy.KEEP`) — 앱을 켜둔 채로 오래 사용하는 동안 발생한 기록은 다음 재시작 전까지 백업되지 않음

---

## ADR-22. Firestore 기반 동적 리워드 카탈로그 — 앱 업데이트 없는 가격 관리

### 결정

리워드 스토어 상품(뱃지/쿠폰/기부/테마 팩)의 가격과 판매 여부를 RN 코드에 하드코딩하지 않고 **Firestore `rewardCatalog` 컬렉션**에서 읽어오도록 했습니다. 클라이언트 쓰기는 Security Rules로 전부 막고(`allow write: if false`), 관리자가 Firebase 콘솔에서 문서를 직접 수정합니다.

```
Firebase 콘솔에서 rewardCatalog/{itemId} 문서 수정
    ↓
GetRewardCatalogUseCase (core:domain) — isActive 필터링
    ↓
RewardBridgeModule.getRewardCatalog() — RN이 화면 진입마다 호출
    ↓
reward-store-rn: fallbackRewardCatalog(오프라인 폴백) 대신 서버 값으로 교체
```

### 이유

가격 변경이나 상품 판매 중단을 앱 업데이트 없이 즉시 반영하고 싶다는 요구가 있었습니다. Cloud Functions/Admin SDK가 없는 환경이라 "읽기는 누구나, 쓰기는 전부 차단 + 콘솔에서만 수정"이 realistic한 범위였습니다.

`FirestoreRewardCatalogDataSource.observeCatalog()`는 처음엔 `addSnapshotListener`로 구현했으나, 호출부(`RewardBridgeModule.getRewardCatalog()`)가 매번 `.first()`로 한 번만 소비하는 구조라 문제가 있었습니다. Firestore 로컬 캐시가 있으면 리스너의 첫 콜백이 콘솔 수정 이전의 **캐시된 값**으로 먼저 도착하고, `.first()`가 그 값으로 바로 완료되며 리스너를 해지해버려 — 서버의 최신 값은 영영 도착하지 못하는 레이스가 있었습니다. 매 호출마다 한 번씩 `get()`으로 새로 받아오는 방식(기본 Source: 온라인이면 서버, 오프라인이면 캐시로 자동 폴백)으로 바꿔 해결했습니다.

### 트레이드오프

- 스토어 화면에 진입할 때마다 네트워크 호출이 발생함 — 상품 개수가 적어(4개) 비용은 무시할 수준이지만, 카탈로그가 커지면 캐싱 전략이 필요할 수 있음
- 콘솔에서 필드 타입을 잘못 바꾸면(예: 숫자를 문자열로) 매핑이 조용히 기본값으로 fallback됨(`getLong(...) ?: 0L`) — 관리자 실수를 앱이 감지하지 못함

---

## ADR-23. 이벤트/프로모션 코드 등록 — 외부 코드 입력과 리워드 스토어 교환의 분리

### 결정

리워드 스토어에서 포인트로 상품을 교환하는 기존 흐름(`RedeemRewardUseCase`)과는 별개로, 마케팅/이벤트에서 외부로 배포한 코드를 사용자가 직접 입력해 포인트를 받는 기능을 추가했습니다. `promoCodes/{code}` 컬렉션에 문서를 관리자가 콘솔에서 직접 생성하고, 클라이언트는 Firestore 트랜잭션으로 `redemptionCount`만 증가시킵니다.

```kotlin
sealed interface PromoCodeRedeemResult {
    data class Success(val pointsAwarded: Int) : PromoCodeRedeemResult
    data object AlreadyRedeemed : PromoCodeRedeemResult
    data object InvalidCode : PromoCodeRedeemResult
    data object SignInRequired : PromoCodeRedeemResult
    data object UnknownError : PromoCodeRedeemResult
}
```

중복 등록 여부는 `users/{uid}/data/promoCodeRedemptions/entries/{code}` 문서의 존재 여부로 판단하며(이미 로그인 사용자 소유 경로라 기존 규칙으로 보호됨), Firestore 트랜잭션 안에서 "이미 등록했는지 확인 → 코드 유효성(활성/한도/만료) 확인 → 두 문서를 함께 갱신"을 원자적으로 처리합니다.

### 이유

기존 쿠폰(ADR-19)은 스토어에서 포인트를 써서 발급받는 것이라 발급 시점에 이미 `userId`를 알고 있지만, 프로모션 코드는 외부에서 받은 코드를 사용자가 스스로 입력하는 것이라 "이 코드가 유효한가"와 "이 사용자가 이미 썼는가"를 둘 다 서버 쪽(Security Rules + 트랜잭션)에서 검증해야 위조·중복 등록을 막을 수 있습니다. `RedeemRewardUseCase`에 분기를 추가하는 대신 별도 UseCase(`RedeemPromoCodeUseCase`)로 둔 이유는 두 흐름이 "무엇을 검증하는가"부터 다르기 때문입니다(포인트 잔액 vs. 코드 자체의 유효성).

Security Rules에서 `redemptionCount`가 정확히 1만큼 증가했는지, 다른 필드(`pointsReward`, `isActive`, `maxRedemptions`, `expiresAtEpochMillis`)는 그대로인지를 모두 강제해 클라이언트가 임의로 포인트를 부풀리거나 만료된 코드를 재사용하는 것을 막습니다.

### 트레이드오프

- 코드 발급(문서 생성)은 전부 관리자가 Firebase 콘솔에서 수동으로 해야 함 — 대량 발급이나 코드별 발급 자동화가 필요해지면 별도 어드민 도구가 필요함
- 만료 시각 비교는 `request.time.toMillis() < resource.data.expiresAtEpochMillis`처럼 Security Rules 문법(`timestamp.fromMillis`가 아니라 `request.time`을 밀리초로 변환하는 방향)에 맞춰야 함 — 처음 작성 시 이 부분에서 배포 경고가 났었음
