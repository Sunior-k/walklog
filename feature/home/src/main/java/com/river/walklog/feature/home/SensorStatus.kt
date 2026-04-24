package com.river.walklog.feature.home

import androidx.compose.runtime.Stable

/**
 * Health Connect 가용성 및 권한 상태.
 *
 * @Stable: 모듈 경계를 넘는 타입은 Compose 컴파일러가 안정성을 자동 추론하지 못할 수 있음.
 * 모든 서브타입이 data object이므로 명시적으로 선언.
 */
@Stable
sealed interface SensorStatus {
    /** 초기 로딩 중 – Health Connect 가용성을 아직 확인하지 않았다. */
    data object Loading : SensorStatus

    /** Health Connect 사용 가능하고 권한 허용됨 – 정상 측정 중. */
    data object Available : SensorStatus

    /** 이 기기에서 Health Connect SDK 를 사용할 수 없음 (Android 9 미만 등). */
    data object Unavailable : SensorStatus

    /** Health Connect SDK 는 사용 가능하나 READ_STEPS 권한이 없음. */
    data object PermissionRequired : SensorStatus
}
