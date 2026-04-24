# activity_classifier.tflite

Place the converted LiteRT model file here:

```
core/native/src/main/assets/activity_classifier.tflite
```

## 모델 스펙

| 항목 | 값 |
|---|---|
| 입력 shape | `[1, 50, 6]` — batch × time steps × channels |
| 입력 채널 | accel_x, accel_y, accel_z, gyro_x, gyro_y, gyro_z |
| 출력 shape | `[1, 3]` — softmax 확률 |
| 출력 클래스 | 0=WALKING, 1=STATIONARY, 2=UNKNOWN |
| 샘플링 레이트 | 50 Hz (1초 윈도우) |

## 변환 방법

UCI HAR Dataset 기반 학습 모델을 LiteRT 포맷으로 변환:

```bash
# TensorFlow → LiteRT 변환 (Python)
import tensorflow as tf

converter = tf.lite.TFLiteConverter.from_saved_model("har_model/")
converter.optimizations = [tf.lite.Optimize.DEFAULT]  # int8 quantization
tflite_model = converter.convert()

with open("activity_classifier.tflite", "wb") as f:
    f.write(tflite_model)
```

## 참고

- UCI HAR Dataset: https://archive.ics.uci.edu/dataset/240
- LiteRT 변환 가이드: https://ai.google.dev/edge/litert/models/convert_tf
