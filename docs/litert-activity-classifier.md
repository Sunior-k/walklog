# LiteRT — ActivityClassifier (온디바이스 HAR)

> 참고: [LiteRT Android 추론 가이드](https://ai.google.dev/edge/litert/inference) · [LiteRT 모델 변환](https://ai.google.dev/edge/litert/models/convert_tf) · [UCI HAR Dataset](https://archive.ics.uci.edu/dataset/240/human+activity+recognition+using+smartphones)

> **현재 구현 상태:** `activity_classifier.tflite` 모델 파일은 아직 assets에 포함하지 않았습니다.
> 그래서 `ActivityClassifier`의 `interpreter`는 `null`로 초기화되고, 모든 분류 결과는 `UNKNOWN`으로 반환이 됩니다.
> 센서 수집 파이프라인과 추론 코드는 구현되어 있어, 모델 파일을 `core/native/src/main/assets/`에 배치하면 바로 활성화할 수 있습니다.

---

## 1. 온디바이스 추론을 선택한 이유

`TYPE_STEP_COUNTER`로 얻을 수 있는 값은 누적 걸음 수에 가깝습니다.
사용자가 실제로 걷고 있는지, 차량에 탑승했는지, 혹은 기기를 흔든 것인지는 구분하기 어렵습니다.

서버 추론 대신 온디바이스 추론을 선택한 이유는 다음과 같습니다.

| 항목 | 서버 추론 | 온디바이스 (LiteRT) |
|---|---|---|
| 개인정보 | 원시 센서 데이터 전송 | 데이터 기기 밖 미전송 |
| 오프라인 동작 | 네트워크 필요 | 불필요 |
| 지연 시간 | 왕복 네트워크 | 수 ms 이내 |
| 배터리 | 추론 외 통신 비용 | 추론만 |
| 인프라 비용 | 서버 운영 필요 | 없음 |

---

## 2. 모델 스펙

| 항목 | 값 |
|---|---|
| 아키텍처 | CNN/LSTM 기반 HAR (UCI HAR Dataset 학습) |
| 입력 shape | `[1, 50, 6]` — batch × time steps × channels |
| 입력 채널 | accel_x, accel_y, accel_z, gyro_x, gyro_y, gyro_z |
| 샘플링 레이트 | 50 Hz (1초 윈도우 = 50 타임스텝) |
| 출력 shape | `[1, 3]` — softmax 확률 |
| 출력 클래스 | 0=WALKING, 1=STATIONARY, 2=UNKNOWN |
| 최적화 | int8 양자화 (DEFAULT 최적화) |

**채널 단위:**
- accel x/y/z: m/s² (Android `SensorEvent.values` 기본값)
- gyro x/y/z: rad/s (Android `SensorEvent.values` 기본값)

---

## 3. 모델 변환 (UCI HAR → LiteRT)

```python
import tensorflow as tf

# 저장된 SavedModel을 LiteRT로 변환
converter = tf.lite.TFLiteConverter.from_saved_model("har_model/")
converter.optimizations = [tf.lite.Optimize.DEFAULT]  # int8 동적 양자화
tflite_model = converter.convert()

with open("activity_classifier.tflite", "wb") as f:
    f.write(tflite_model)
```

변환된 파일은 해당 경로에 배치합니다.

```
core/native/src/main/assets/activity_classifier.tflite
```

`int8 양자화`를 적용한 이유는 다음과 같습니다.
- 모델 크기 약 75% 감소 (float32 대비)
- 추론 속도 2~4배 향상 (CPU int8 연산)
- 정확도 손실은 통상 1% 미만

---

## 4. 센서 수집 — ActivitySensorCollector

`TYPE_ACCELEROMETER`와 `TYPE_GYROSCOPE`를 동시에 수집하고, 50개 샘플 단위의 윈도우로 조립합니다.

```kotlin
@Singleton
class ActivitySensorCollector @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun observeWindows(): Flow<FloatArray> = callbackFlow {
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroSensor  = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val window      = FloatArray(WINDOW_SIZE * CHANNELS)  // 50 × 6 = 300?
        val lastGyro    = FloatArray(3)
        var gyroReady   = false
        var sampleCount = 0

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_GYROSCOPE -> {
                        // 최신 자이로 값 갱신
                        lastGyro[0] = event.values[0]
                        lastGyro[1] = event.values[1]
                        lastGyro[2] = event.values[2]
                        gyroReady = true
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        if (!gyroReady) return  // 자이로 초기화 전까지 대기
                        // 한 샘플 조립: [ax, ay, az, gx, gy, gz]
                        val offset = sampleCount * CHANNELS
                        window[offset..offset+2] = accel values
                        window[offset+3..offset+5] = lastGyro
                        sampleCount++
                        if (sampleCount == WINDOW_SIZE) {
                            trySend(window.copyOf())  // 50샘플 완성 → 방출
                            sampleCount = 0
                        }
                    }
                }
            }
        }

        sensorManager.registerListener(listener, accelSensor, SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(listener, gyroSensor,  SENSOR_DELAY_FASTEST)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
```

**설계 결정:**

| 항목 | 결정 | 이유 |
|---|---|---|
| 샘플 트리거 | 가속도계 이벤트 | 두 센서 중 더 안정적인 레이트 |
| 자이로 취급 | 마지막 수신값 유지 | 두 센서의 이벤트 빈도가 다를 수 있어 Hold-last 방식 |
| 윈도우 방식 | 비겹침(non-overlapping) | 중첩 윈도우 대비 CPU 절약 |
| gyroReady 가드 | 자이로 첫 이벤트 전 샘플 무시 | 0 초기화된 자이로 값이 모델 입력으로 들어가는 것 방지 |

---

## 5. 분류기 — ActivityClassifier

```kotlin
class ActivityClassifier(
    context: Context,
    private val sensorCollector: ActivitySensorCollector,
) : Closeable {

    // 모델 파일 없을 경우 null → classify()가 UNKNOWN 반환
    private val interpreter: Interpreter? = runCatching {
        val model = FileUtil.loadMappedFile(context, "activity_classifier.tflite")
        Interpreter(model, Interpreter.Options().apply { numThreads = 2 })
    }.getOrNull()

    // Flow 기반 연속 분류
    fun observeActivityState(): Flow<ActivityState> =
        sensorCollector.observeWindows()
            .map { window -> classify(window) }
            .flowOn(Dispatchers.Default)

    // 단일 윈도우 분류
    fun classify(sensorWindow: FloatArray): ActivityState {
        val interp = interpreter ?: return ActivityState.UNKNOWN

        // 평탄화된 FloatArray(300) → [1, 50, 6] 3차원 텐서
        val input = Array(1) { Array(WINDOW_SIZE) { FloatArray(CHANNELS) } }
        for (t in 0 until WINDOW_SIZE) {
            for (c in 0 until CHANNELS) {
                input[0][t][c] = sensorWindow[t * CHANNELS + c]
            }
        }

        val output = Array(1) { FloatArray(3) }  // [1, 3] softmax
        interp.run(input, output)

        val maxIdx = output[0].indices.maxByOrNull { output[0][it] }
            ?: ActivityState.UNKNOWN.ordinal
        return ActivityState.entries[maxIdx]
    }

    override fun close() = interpreter?.close() ?: Unit
}
```

### 텐서 레이아웃

```
sensorWindow (FloatArray, size = 300):
index 0  1  2  3  4  5  | 6  7  8  9  10 11 | ... | 294 295 296 297 298 299
      ax ay az gx gy gz  | ax ay az gx gy gz  | ... | (t=49)
      ←── t=0 ────────── → ←── t=1 ────────── → ... → ←── t=49 ──────────→

interpreter 입력 [1, 50, 6]:
  input[0][t][c] = sensorWindow[t * 6 + c]
```

---

## 6. ViewModel 연동

센서 권한을 확보한 뒤, `HomeViewModel`에서 분류 스트림을 구독합니다.

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val activityClassifier: ActivityClassifier,
    ...
) : ViewModel() {

    private fun startObservingActivity() {
        activityClassifier.observeActivityState()
            .catch { throwable ->
                crashReporter.log("Activity classifier error: ${throwable.message}")
            }
            .onEach { activityState ->
                _state.update { it.copy(activityState = activityState) }
            }
            .launchIn(viewModelScope)
    }
}
```

`observeActivityState()`는 `callbackFlow` 기반입니다.
`viewModelScope`가 취소되면 센서 리스너도 함께 해제되므로, 별도의 `close()` 호출은 필요하지 않습니다.

---

## 7. 분류 결과 — ActivityState

```kotlin
enum class ActivityState { WALKING, STATIONARY, UNKNOWN }
```

| 상태 | 조건 | 활용 |
|---|---|---|
| `WALKING` | 걷기 확률이 가장 높음 | `WalkProgressRing` 링 트랙 pulse 애니메이션
| `STATIONARY` | 정지 확률이 가장 높음 | 링 애니메이션 중단.  |
| `UNKNOWN` | 미분류 또는 모델 파일 없음 | 링 애니메이션 없음 |

`UNKNOWN`은 두 가지 경우에 반환됩니다:
1. softmax 확률이 UNKNOWN 클래스(index=2)에서 가장 높은 경우
2. `activity_classifier.tflite` 파일이 없어 `interpreter == null`인 경우

두 번째 경우는 `runCatching`으로 처리했습니다.
모델 파일이 없어도 앱이 크래시되지 않고 `UNKNOWN`으로 자연스럽게 fallback됩니다.

> **알림 아키텍처 결정:** `ActivityClassifier`는 홈 화면 링 애니메이션 제어까지만 담당합니다.
> 알림 예약은 `WalkingInsightsEngine`이 분석한 `peakHour`를 바탕으로 `AlarmManagerWalkingReminderScheduler`가 처리합니다.
> 반응형 방식(STATIONARY 1시간 지속 → 알림)을 사용하지 않은 이유는 업무 중이나 수면 중처럼 사용자가 의도적으로 정지한 상황에서도 알림이 발송될 수 있기 때문입니다. 대신 피크아워 사전 예약 방식을 사용해, 분류기의 책임을 UI 피드백으로 명확히 제한했습니다.

---

## 8. 배터리 최적화 고려사항

`SENSOR_DELAY_FASTEST`는 가능한 최고 레이트로 센서를 구동합니다.
50Hz 윈도우 수집에는 충분하지만, 장시간 연속 실행하면 배터리 부담이 생길 수 있습니다.

실제 서비스에 적용할 때는 화면이 활성화된 동안에만 구독하는 방식을 선택했습니다.

```kotlin
// 홈 화면 활성화 중에만 구독
override fun onResume() {
    super.onResume()
    handleIntent(HomeIntent.OnPermissionResult(granted = isPermissionGranted()))
}

override fun onPause() {
    super.onPause()
    activityJob?.cancel()
}
```

or WorkManager 기반의 15분 간격 배치 분류로 전환해 배터리 소모를 줄일 수 있습니다.

---

## 9. 모델 정확도

UCI HAR Dataset 기반 모델의 공개 벤치마크:

| 클래스 | Precision | Recall |
|---|---|---|
| WALKING | ~97% | ~96% |
| STATIONARY | ~99% | ~99% |
| 복합(계단, 앉기 등) | ~90% | ~88% |

본 프로젝트에서는 WALKING / STATIONARY / UNKNOWN 3개 클래스로 단순화했습니다. 따라서 원본 6개 클래스(앉기·서기·눕기·걷기·계단 오름·계단 내림) 중 이동 관련 클래스를 WALKING으로 매핑하거나, 프로젝트 목적에 맞게 재훈련하는 과정이 필요합니다.

---

## 10. 모델 도입 후 기능 확장

모델 파일(`activity_classifier.tflite`)을 배치한 뒤 `ActivityClassifier`가 실제 WALKING/STATIONARY 상태를 반환하면, 다음 기능을 확장할 수 있습니다.

### 10-1. 걷기 세션 자동 감지

`WALKING` 상태의 시작과 종료를 감지해 걷기 세션을 자동으로 기록합니다.

```
ActivityClassifier.WALKING 연속 감지
    → WalkingSessionTracker: 세션 시작 타임스탬프 기록
    → STATIONARY/UNKNOWN N초 이상 지속 → 세션 종료
    → Room WalkingSessionEntity 저장
    → "오늘 15분 걸으셨어요" 인앱 배지 또는 알림
```

### 10-2. 실시간 칼로리/페이스

사용자가 걷는 중일 때만 홈 화면에 실시간 칼로리와 페이스를 표시합니다.

```
ActivityState.WALKING 감지
    → 현재 세션 걸음 수 / 경과 시간 → 걸음/분 (페이스)
    → 페이스 × 체중(kg) × 칼로리 계수 → 소모 칼로리 추정
    → HomeViewModel.caloriesLive 업데이트
```

### 10-3. 목표 달성 예측

현재 걷는 속도를 기반으로 "이 속도면 X시에 목표 달성" 메시지를 홈 화면에 표시합니다.

```
WalkingInsightsEngine C++ 내부에서 projectedSteps 산출 (이미 구현됨)
    → WALKING 상태일 때 HomeViewModel에 노출
    → "현재 페이스 유지 시 오후 5시 30분에 목표 달성 예상"
```

C++ 엔진에서 이미 `projectedSteps`를 계산하고 있으므로, 이후에는 UI 연결과 WALKING 상태 조건만 추가하면 됩니다.

---

## 11. 기술 개선 방향

| 개선 항목 | 방법 |
|---|---|
| 모델 배치 | `activity_classifier.tflite`를 `core/native/src/main/assets/`에 배치 |
| 모델 업데이트 | Firebase ML Model Delivery로 앱 업데이트 없이 모델 교체 |
| 정확도 개선 | 온디바이스 피드백 루프 → 사용자 레이블 기반 파인튜닝 |
| 배터리 최적화 | `SENSOR_DELAY_GAME`으로 전환 후 정확도 재측정 |
| 진동 피드백 | `ActivityState.WALKING` 감지 시 진동으로 목표 달성 독려 |
