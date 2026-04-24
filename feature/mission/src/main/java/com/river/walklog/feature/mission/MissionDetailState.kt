package com.river.walklog.feature.mission

import androidx.compose.runtime.Immutable
import com.river.walklog.core.model.MissionType

@Immutable
data class MissionDetailState(
    val title: String = "오늘 목표 달성까지 조금만 더 걸어보세요",
    val description: String = "매일 꾸준히 걷는 습관을 만들 수 있도록 오늘의 미션을 준비했어요.",
    val currentSteps: Int = 0,
    val targetSteps: Int = 6_000,
    val rewardText: String = "+20 캐시",
    val missionType: MissionType = MissionType.DAILY,
    val isCompleted: Boolean = false,
    val recommendedTimeText: String = "오후 3시",
    val weatherLocationText: String = "서울 기준",
    val weatherTemperatureText: String = "-",
    val weatherConditionText: String = "정보 없음",
    val weatherAdviceText: String = "날씨 정보를 불러오는 중이에요",
    val weatherSupportingText: String = "",
)

sealed interface MissionDetailIntent {
    data object OnClickBack : MissionDetailIntent
    data object OnClickStartWalking : MissionDetailIntent
    data object OnRefreshWeather : MissionDetailIntent
}
