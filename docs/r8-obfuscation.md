# R8 난독화 및 코드 최적화

> 참고: [Android Developers — Shrink, obfuscate, and optimize](https://developer.android.com/build/shrink-code) · [R8 Compatibility Guide](https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md) · [ProGuard Manual](https://www.guardsquare.com/manual/configuration/usage)

---

## 1. R8이란?

R8은 Google이 ProGuard를 대체하기 위해 제공하는 **Android 전용 코드 최적화 도구**입니다.
AGP에 내장되어 있어, `isMinifyEnabled = true`를 설정하면 release 빌드에서 자동으로 활성화됩니다.

ProGuard와의 차이:

| 항목 | ProGuard | R8 |
|---|---|---|
| 통합 | 외부 도구 | AGP 내장 |
| 처리 단계 | 순차적 (shrink → obfuscate → optimize) | 단일 패스 처리 |
| DEX 변환 | D8가 별도 수행 | R8이 직접 DEX 생성 |
| 최적화 수준 | 기본 | 더 공격적 (Full mode) |

---

## 2. R8의 세 가지 작업

### 2-1. Shrinking (코드·리소스 제거)

Shrinking은 **진입점** 부터 그래프를 탐색하면서 사용되지 않는 클래스, 메서드, 필드를 제거하는 과정입니다. 일반적으로 **Tree Shaking**이라고 부릅니다.

```
:app (entry point)
    ↓
feature:home → HomeViewModel → StepRepository → StepRepositoryImpl
                                                           ↓
                                           사용되지 않는 클래스는 제거됨
```

`isShrinkResources = true`를 함께 적용하면 코드에서 참조되지 않는 drawable, string, layout도 APK에서 제외됩니다.

**WalkLog에서는 `nonMinifiedRelease`와 `release`를 비교해 R8 효과를 측정합니다.**

`debug` 빌드는 디버깅 메타데이터와 debug 전용 설정이 포함되므로 R8 효과만 비교하기에는 기준이 섞입니다. 따라서 release 기반이지만 R8을 끈 `nonMinifiedRelease`를 기준선으로 두고, R8과 resource shrink가 켜진 `release`와 비교합니다.

### 2-2. Obfuscation (난독화)

Obfuscation은 클래스명, 메서드명, 필드명을 짧고 의미 없는 이름으로 바꾸는 단계입니다.

```
난독화 전:
com.river.walklog.feature.home.HomeViewModel
com.river.walklog.core.data.repository.StepRepositoryImpl

난독화 후:
a.b.c.d
a.b.e.f
```

**목적:**
- 리버스 엔지니어링 비용 증가 (APK를 디컴파일해도 원래 구조 파악이 어려움)
- APK 크기 추가 감소 (짧은 이름 → DEX 파일 크기 감소)

**스택 트레이스 문제와 해결:**

난독화가 적용되면 원본 크래시 스택 트레이스는 사람이 읽기 어려운 형태가 됩니다.

```
난독화된 스택 트레이스 (Crashlytics 원본):
a.b.c.d.a(Unknown Source:4)
a.b.e.f.b(Unknown Source:11)

Crashlytics 매핑 후 (자동 deobfuscation):
HomeViewModel.collectSteps(HomeViewModel.kt:52)
StepRepositoryImpl.observeCurrentSteps(StepRepositoryImpl.kt:34)
```

R8 빌드에서 생성되는 `mapping.txt`를 Crashlytics에 업로드하면, 대시보드에서 원래 클래스명과 라인 번호로 자동 복원됩니다.

```kotlin
// app/build.gradle.kts
getByName("release") {
    configure<CrashlyticsExtension> {
        mappingFileUploadEnabled = true  // mapping.txt 자동 업로드
    }
}
```

```proguard
# proguard-rules.pro
# 스택 트레이스 라인 번호 유지 (Crashlytics 대시보드에서 정확한 위치 확인)
-keepattributes SourceFile,LineNumberTable
# 소스 파일명은 "SourceFile"로 통일 (실제 파일명 노출 방지)
-renamesourcefileattribute SourceFile
```

### 2-3. Optimization (코드 최적화)

R8 Full Mode에서 수행하는 최적화:

| 최적화 | 설명 |
|---|---|
| 메서드 인라이닝 | 단순 메서드 호출을 호출부에 직접 삽입 |
| Dead code 제거 | 도달 불가능한 코드 블록 제거 |
| 상수 폴딩 | 컴파일 시점에 계산 가능한 값 미리 계산 |
| 클래스 병합 | 단일 구현을 가진 인터페이스 인라이닝 |
| 파라미터 제거 | 항상 같은 값으로 호출되는 파라미터 제거 |

---

## 3. WalkLog 설정

```kotlin
// app/build.gradle.kts
getByName("release") {
    isMinifyEnabled = true      // Shrinking + Obfuscation + Optimization
    isShrinkResources = true    // 미사용 리소스 제거
}
```

### 빌드타입별 동작 비교

| BuildType | isMinifyEnabled | isShrinkResources | 용도 |
|---|---|---|---|
| `debug` | false | false | 개발 (빠른 빌드, 가독성) |
| `release` | **true** | **true** | 배포 (최적화, 보안) |
| `benchmark` | false | false | Baseline Profile 생성 |

`benchmark` 빌드타입에서는 minify를 끕니다.
UIAutomator가 `By.text("지난주 리포트")`처럼 UI 요소를 텍스트로 탐색할 때 난독화가 끼어들면 테스트 안정성이 떨어질 수 있고, Baseline Profile 생성 자체에도 minification은 필요하지 않습니다.

---

## 4. ProGuard 규칙이 필요한 이유

R8은 정적 분석으로 도달하지 않는 코드를 제거합니다.
다만 **리플렉션이나 클래스명 문자열로 참조되는 코드**는 정적 분석만으로 추적하기 어렵습니다. 이런 영역에는 명시적인 keep 규칙이 필요합니다.

### WalkLog에서 필요한 규칙과 이유

**Room Entity 보존:**
```proguard
-keep @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
```
Room은 `@Entity` 클래스의 필드를 **리플렉션**으로 읽어 SQL 컬럼과 매핑합니다.
만약 R8이 `totalSteps` 같은 필드명을 `a`로 바꾸면 DB 스키마와 맞지 않아 release 빌드에서만 크래시가 발생할 수 있습니다.

**WorkManager Worker 보존:**
```proguard
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
```
WorkManager는 Worker 클래스를 **클래스명 문자열**로 인스턴스화합니다. 클래스명이 난독화되면 런타임에 Worker를 찾지 못할 수 있습니다.

**Glance Widget 보존:**
```proguard
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
```
`AppWidgetReceiver`는 `AndroidManifest.xml`에 클래스명으로 등록됩니다. 이 이름이 바뀌면 시스템이 위젯 Receiver를 찾지 못해 위젯이 로드되지 않습니다.

**예외 클래스 보존:**
```proguard
-keep public class * extends java.lang.Exception
```
Crashlytics는 예외를 Issues 탭에 분류할 때 예외 클래스명을 사용합니다. 예외 클래스명까지 난독화되면 서로 다른 예외가 비슷한 이름으로 묶여 원인 파악이 어려워집니다.

### 라이브러리 Consumer Rules

대부분의 라이브러리는 자체 ProGuard 규칙을 `consumer-proguard-rules.pro`로 제공합니다.
AGP가 이걸 자동으로 적용하므로, 아래 라이브러리는 별도 규칙을 직접 작성하지 않아도 되는 경우가 많습니다.

| 라이브러리 | 자체 규칙 포함 |
|---|---|
| Hilt | ✅ |
| Retrofit | ✅ |
| Firebase Crashlytics | ✅ |
~~| Room | ✅ (단, 추가 규칙 권장) |~~
| Coroutines | ✅ |
| DataStore | ✅ |

---

## 5. 빌드 결과 확인

```bash
# R8 적용 전/후를 같은 release 계열에서 비교
./gradlew :app:assembleNonMinifiedRelease :app:assembleRelease
```

생성 파일:

```bash
app/build/outputs/
  apk/nonMinifiedRelease/app-nonMinifiedRelease.apk    # release 기반, R8 off
  apk/release/app-release.apk                         # release 기반, R8 on
  mapping/release/mapping.txt                         # 난독화 매핑 테이블

# mapping.txt 예시
com.river.walklog.feature.home.HomeViewModel -> a.b.c.d:
    int currentSteps -> a
    void collectSteps() -> b
```

### APK 크기 측정

```bash
stat -f "%z %N" app/build/outputs/apk/nonMinifiedRelease/app-nonMinifiedRelease.apk
stat -f "%z %N" app/build/outputs/apk/release/app-release.apk
```

### DEX 크기 측정

```bash
unzip -l app/build/outputs/apk/nonMinifiedRelease/app-nonMinifiedRelease.apk | rg "classes.*\\.dex"
unzip -l app/build/outputs/apk/release/app-release.apk | rg "classes.*\\.dex"
```

DEX 합산 크기는 출력된 `classes*.dex`의 byte 값을 모두 더해 계산합니다.

### 리소스/네이티브 라이브러리 구성 확인

```bash
unzip -l app/build/outputs/apk/nonMinifiedRelease/app-nonMinifiedRelease.apk > /tmp/walklog-non-minified-release-apk.txt
unzip -l app/build/outputs/apk/release/app-release.apk > /tmp/walklog-release-apk.txt
```

Android Studio → **Build → Analyze APK** 에서도 APK 내부 구성과 파일별 크기를 확인할 수 있습니다.

### WalkLog 실측 결과

측정 환경:

| 항목 | 값 |
|---|---|
| 비교 기준 | `nonMinifiedRelease` vs `release` |
| Build command | `./gradlew :app:assembleNonMinifiedRelease :app:assembleRelease` |
| AGP | 8.13.1 |
| R8 적용 | `release`만 `isMinifyEnabled=true`, `isShrinkResources=true` |

측정값:

| 항목 | nonMinifiedRelease | release | 변화 |
|---|---:|---:|---:|
| APK 크기 | 69,469,747 bytes (66 MB) | 32,686,358 bytes (31 MB) | -53.0% |
| DEX 합산 크기 | 44,247,932 bytes | 8,438,072 bytes | -80.9% |
| `classes*.dex` 개수 | 5개 | 2개 | 3개 감소 |
| `mapping.txt` | 없음 | 71 MB 생성 | 난독화 적용 |

감소율 계산:

```
(nonMinifiedRelease - release) / nonMinifiedRelease * 100
```

`release` 빌드에서 `mapping.txt`가 생성되면 난독화가 적용된 것입니다. Crashlytics Gradle Plugin은 이 파일을 업로드해 난독화된 스택 트레이스를 원본 클래스명과 라인 번호로 복원합니다.

이번 측정에서는 `mapping.txt` 상단에 `# compiler: R8`, `# compiler_version: 8.13.17`, `pg_map_hash`가 기록됐고, 실제 클래스가 `a.a`, `R8$$REMOVED$$CLASS$$...` 형태로 매핑되어 난독화와 최적화가 적용됐음을 확인했습니다.

---

## 6. R8 트러블슈팅

### 크래시가 release에서만 발생하는 경우

R8이 필요한 코드를 제거했거나 이름을 변경한 경우입니다.

```bash
# 1. mapping.txt로 스택 트레이스 복원
# Android Studio → Run → "Analyze Stack Trace" 에 난독화된 스택 붙여넣기

# 2. R8 trace 활성화
# gradle.properties에 추가:
# r8.printUnusedRules=path/to/output.txt
# → 실제로 적용된 규칙과 사용되지 않은 규칙 확인 가능
```

### 특정 클래스만 난독화 제외

```proguard
# 특정 클래스 전체 유지
-keep class com.river.walklog.core.domain.model.** { *; }

# 또는 소스 코드에서 @Keep 어노테이션 사용
@Keep
data class DailyStepCount(...)
```
