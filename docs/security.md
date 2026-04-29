# 보안 설계

> 참고: [OWASP Mobile Security Testing Guide](https://owasp.org/www-project-mobile-security-testing-guide/) · [Android Security Best Practices](https://developer.android.com/topic/security/best-practices) · [Network Security Configuration](https://developer.android.com/training/articles/security-config) · [Auto Backup](https://developer.android.com/guide/topics/data/autobackup)

---

## 적용 항목 요약

| 항목 | 대응 OWASP | 상태 |
|---|---|---|
| R8 난독화 | M7 — Insufficient Binary Protections | ✅ |
| Network Security Config (HTTP 차단) | M5 — Insecure Communication | ✅ |
| Network Security Config (사용자 CA 제외) | M5 — Insecure Communication | ✅ |
| Room DB / DataStore 백업 제외 | M6 — Inadequate Privacy Controls | ✅ |
| `android:exported` 명시 | M8 — Security Misconfiguration | ✅ |
| Room Parameterized Query | M4 — Insufficient Input/Output Validation | ✅ (Room 기본) |
| Crashlytics 디버그 비활성화 | M8 — Security Misconfiguration | ✅ |

---

## 1. Network Security Config

### 왜 필요한가?

별도 설정을 두지 않았을 때 Android의 기본 동작은 다음과 같습니다.

```
기본 동작 (설정 없음):
- HTTP 평문 트래픽 허용 (API 28 미만)
- 시스템 CA + 사용자 설치 CA 모두 신뢰
```

**사용자 설치 CA를 신뢰할 때 발생할 수 있는 문제:**

```
공격자 시나리오 (MitM — Man in the Middle):

1. 공격자가 자신의 CA 인증서를 기기에 설치
2. 공격자가 Wi-Fi 핫스팟 운영 (카페, 공공장소)
3. 사용자가 해당 Wi-Fi 접속
4. 공격자가 HTTPS 트래픽을 가로채고 자신의 인증서로 재서명
5. 기기가 "신뢰하는 CA"라며 연결 허용
6. 모든 API 통신 내용 노출
```

### WalkLog 설정

```xml
<!-- app/src/main/res/xml/network_security_config.xml -->
<network-security-config>

    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <!-- 시스템 CA만 신뢰 — 사용자 설치 CA 제외 -->
            <certificates src="system" />
        </trust-anchors>
    </base-config>

    <!-- Debug 빌드에서만 사용자 CA 허용 (Charles, mitmproxy 디버깅) -->
    <debug-overrides>
        <trust-anchors>
            <certificates src="system" />
            <certificates src="user" />
        </trust-anchors>
    </debug-overrides>

</network-security-config>
```

```xml
<!-- AndroidManifest.xml -->
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

### 설정 효과

| 시나리오 | 설정 전 | 설정 후 |
|---|---|---|
| HTTP 요청 | 허용 | 차단 (`IOException`) |
| 사용자 CA MitM (Release) | 허용 | 차단 (`SSLHandshakeException`) |
| 사용자 CA MitM (Debug) | 허용 | 허용 (개발 편의) |
| 시스템 CA HTTPS | 허용 | 허용 |

### debug-overrides가 안전한 이유

`<debug-overrides>`는 `android:debuggable="true"` 빌드에서만 적용됩니다.
Release APK에서는 AGP가 `android:debuggable="false"`를 자동 설정하므로, 프로덕션 환경에서 이 블록은 무시됩니다.

---

## 2. 백업 데이터 보호

### 백업이 보안 위협이 되는 경우

Android Auto Backup은 사용자의 Google 계정에 앱 데이터를 자동으로 업로드합니다. 제한 없이 허용할 경우 다음 문제가 생길 수 있습니다.

```
위협 시나리오:

1. 사용자 A가 앱 사용 (걸음 수 이력 Room DB에 저장)
2. Google 계정에 자동 백업
3. 사용자 A의 기기가 분실/도난
4. 공격자가 같은 Google 계정으로 새 기기 설정
5. 앱 설치 시 DB 자동 복원 → 걸음 수 이력 접근 가능
```

기기 이전도 주의가 필요합니다.
`baselineSensorSteps`처럼 센서 캘리브레이션에 가까운 기준값이 다른 기기에 복원되면 **걸음 수 오계산**이 발생할 수 있습니다.

### WalkLog 설정 (API 31 이상)

```xml
<!-- app/src/main/res/xml/data_extraction_rules.xml -->
<data-extraction-rules>

    <cloud-backup>
        <!-- Room DB: 걸음 수 이력 — 클라우드 백업에서 제외 -->
        <exclude domain="database" path="." />
        <!-- DataStore: 기기 고유 설정 상태 -->
        <exclude domain="file" path="datastore" />
    </cloud-backup>

    <device-transfer>
        <!-- 기기 이전 시 DB 제외 — 센서 기준값 재보정 필요 -->
        <exclude domain="database" path="." />
        <exclude domain="file" path="datastore" />
    </device-transfer>

</data-extraction-rules>
```

### WalkLog 설정 (API 30 이하 폴백)

```xml
<!-- app/src/main/res/xml/backup_rules.xml -->
<full-backup-content>
    <exclude domain="database" path="." />
    <exclude domain="file" path="datastore" />
</full-backup-content>
```

### 유효한 domain 값

```
file, database, sharedpref, external, root,
device_file, device_database, device_sharedpref, device_root
```

`cache`, `external-cache`는 유효한 domain 값이 아닙니다. 캐시 디렉터리는 시스템이 자동으로 백업 대상에서 제외합니다.

---

## 3. R8 난독화 (보안 관점)

자세한 내용은 [r8-obfuscation.md](r8-obfuscation.md)를 참고하시면 될 것 같습니다.

보안 관점에서의 핵심:

```
APK 배포 → 누구나 apktool, jadx로 디컴파일 가능

난독화 전:
com.river.walklog.feature.home.HomeViewModel
com.river.walklog.core.analytics.CrashlyticsReporter
→ 비즈니스 로직, API 엔드포인트, 인증 흐름 파악 용이

난독화 후:
a.b.c.d
a.b.e.f
→ 원래 구조 파악에 상당한 시간과 노력 필요
```

난독화가 공격을 **불가능하게** 만들지는 않습니다. 목표는 분석 비용을 높여 공격을 **실용적이지 않게** 만드는 것입니다.

---

## 4. Manifest 보안 설정

### exported 명시

Android 12(API 31)부터는 `intent-filter`가 있는 컴포넌트에 `android:exported`를 반드시 명시해야 합니다.

```xml
<!-- MainActivity: Launcher intent-filter가 있으므로 exported=true 필요 -->
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:label="@string/app_name">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<!-- FileProvider: 외부 앱이 직접 접근해서는 안 됨 -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:exported="false"  <!-- 직접 접근 차단 -->
    android:grantUriPermissions="true">  <!-- URI 권한만 선택적 부여 -->
</provider>
```

`grantUriPermissions="true"` + `exported="false"` 조합은 FileProvider에서 권장되는 보안 패턴입니다. 파일 URI를 직접 노출하지 않고, `Intent.FLAG_GRANT_READ_URI_PERMISSION`으로 필요한 순간에만 읽기 권한을 부여합니다.

### debuggable

Release 빌드에서는 AGP가 `android:debuggable="false"`를 자동으로 설정합니다.
`debuggable=true`인 앱은 `adb shell run-as`로 앱 데이터에 접근할 수 있어, 프로덕션 배포 전에 확인해야 합니다.

### Crashlytics 디버그 비활성화

```kotlin
// WalkLogApplication.kt
private fun configureCrashlytics() {
    val crashlytics = FirebaseCrashlytics.getInstance()
    // Debug 빌드에서 수집 비활성화 — 개발 중 오류가 프로덕션 대시보드를 오염시키지 않도록
    crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
}
```

---

## 5. SQL Injection 방지

Room은 내부적으로 `SQLiteStatement`의 `bindString()`, `bindLong()` 등을 사용해 파라미터를 바인딩합니다.
쿼리 문자열과 데이터가 분리되기 때문에 SQL Injection 위험을 구조적으로 줄일 수 있습니다.

```kotlin
// DailyStepDao.kt
@Query("SELECT * FROM daily_steps WHERE dateEpochDay = :dateEpochDay")
fun observeForDay(dateEpochDay: Long): Flow<DailyStepEntity?>
// ↑ :dateEpochDay는 파라미터 바인딩 → SQL 구문으로 해석되지 않음
```

WalkLog에서는 직접 문자열 쿼리(`@RawQuery`)를 사용하지 않습니다.

---

## 6. 권한 최소화 원칙

WalkLog가 선언한 권한:

| 권한 | 필요 이유 | 선언 조건 |
|---|---|---|
| `ACTIVITY_RECOGNITION` | 만보계 센서 사용 | API 29+ |
| `POST_NOTIFICATIONS` | 알림 전송 | API 33+ |

네트워크 권한(`INTERNET`)은 Firebase SDK가 `merge` 방식으로 자동 추가합니다.<br>
앱 코드에서는 직접 선언하지 않아, 서비스가 직접 요구하는 권한 범위를 분리해서 볼 수 있습니다. <br>
