package com.river.walklog.core.designsystem.foundation

import androidx.compose.ui.graphics.Color

/**
 * 리캡(월간 돌아보기) 기능 전용 색상 토큰.
 *
 * 각 슬라이드는 주제에 맞는 그라데이션 배경을 가지며,
 * 텍스트/오버레이는 항상 [WalkLogColor.StaticWhite] 기반으로 구성된다.
 */
object RecapColors {
    val LoadingBackground = Color(0xFF0F0F1A)

    val CardGradientStart = Color(0xFF1E1E3F)
    val CardGradientEnd = Color(0xFF3A0CA3)
    val OpeningGradientStart = Color(0xFF0F0F1A)
    val OpeningGradientEnd = Color(0xFF1E1E3F)

    val TotalStepsGradientStart = Color(0xFFB5330A)
    val TotalStepsGradientEnd = Color(0xFFF4A261)

    val AverageStepsGradientStart = Color(0xFF1B4332)
    val AverageStepsGradientEnd = Color(0xFF52B788)

    val CaloriesGradientStart = Color(0xFF7D0909)
    val CaloriesGradientEnd = Color(0xFFE63946)

    val AchievementGradientStart = Color(0xFF023E8A)
    val AchievementGradientEnd = Color(0xFF0096C7)

    val BestDayGradientStart = Color(0xFF3A0CA3)
    val BestDayGradientEnd = Color(0xFF7209B7)

    val StreakGradientStart = Color(0xFF005F73)
    val StreakGradientEnd = Color(0xFF0A9396)

    val PersonaGradientStart = Color(0xFF14213D)
    val PersonaGradientEnd = Color(0xFF0D0D0D)
    val PersonaAccent = Color(0xFFFCA311)
}
