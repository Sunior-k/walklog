package com.river.walklog.core.designsystem.foundation

import androidx.compose.ui.graphics.Color

object WalkLogColor {

    // Primary (브랜드 컬러 - 노랑)
    val Primary = Color(0xFFF5C400)
    val PrimaryDark = Color(0xFFE0B000)
    val PrimaryLight = Color(0xFFFFE066)
    val PrimaryContainer = Color(0xFFFFF3C4)

    // Secondary (보조 강조 - 블루)
    val Secondary = Color(0xFF4666A8)
    val SecondaryContainer = Color(0xFF1A237E)

    // Accent (카드 / 서브 포인트)
    val Accent = Color(0xFFC79B79)

    // Semantic - Error
    val Error = Color(0xFFFF3131)
    val ErrorDark = Color(0xFFCC0000)
    val ErrorContainer = Color(0xFFFFE5E5)

    // Semantic - Success (달성 / 보상 / 성공)
    val Success = Color(0xFF4CAF50)
    val SuccessDark = Color(0xFF388E3C)
    val SuccessContainer = Color(0xFFE8F5E9)

    // Semantic - Warning
    val Warning = Color(0xFFF5C400)

    // Gray Scale
    val Gray50 = Color(0xFFF8F8F8)
    val Gray100 = Color(0xFFEDEDED)
    val Gray200 = Color(0xFFDADADA)
    val Gray300 = Color(0xFFC4C4C4)
    val Gray400 = Color(0xFF9E9E9E)
    val Gray500 = Color(0xFF757575)
    val Gray700 = Color(0xFF4F4F4F)
    val Gray900 = Color(0xFF212121)

    // Background / Surface
    val Background = Color(0xFFF8F8F8)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Gray100

    // Text
    val TextPrimary = Gray900
    val TextSecondary = Gray500
    val TextDisabled = Gray300

    val PrimaryContainerDark = Color(0xFF3D3000)

    // Premium Theme — Night (프리미엄 + 밤 시간대: 별/유성우와 어울리는 남색+골드)
    val PremiumBackground = Color(0xFF0C1A47)
    val PremiumSurface = Color(0xFF13224F)
    val PremiumSurfaceVariant = Color(0xFF1B2C5C)
    val PremiumOutlineVariant = Color(0xFF2E4380)

    // Premium Theme — Day Clear (프리미엄 + 낮/맑음: 태양과 어울리는 크림/골드)
    val PremiumDayClearBackground = Color(0xFFFFF6E3)
    val PremiumDayClearSurface = Color(0xFFFFFFFF)
    val PremiumDayClearSurfaceVariant = Color(0xFFFFF0CC)
    val PremiumDayClearOutlineVariant = Color(0xFFF0D999)

    // Premium Theme — Day Wet (프리미엄 + 낮/비·눈·흐림: 빗줄기와 어울리는 페일 블루)
    val PremiumDayWetBackground = Color(0xFFEAF1F8)
    val PremiumDayWetSurface = Color(0xFFFFFFFF)
    val PremiumDayWetSurfaceVariant = Color(0xFFDCE7F2)
    val PremiumDayWetOutlineVariant = Color(0xFFB7CBE0)

    // Premium Theme — 배경 효과 전용 (PremiumWeatherEffects: 태양/번개)
    val PremiumSunColor = Color(0xFFFFC94D)
    val PremiumSunCoreStart = Color(0xFFFFDE7A)
    val PremiumSunCoreEnd = Color(0xFFFFB74D)
    val PremiumLightningBolt = Color(0xFFFFF6D8)

    // Reward Screen — 다크 네이비/브라운 배경 그라디언트 (프리미엄 테마와 무관한 고정 디자인)
    val RewardBackgroundTop = Color(0xFF0C1A47)
    val RewardBackgroundMid = Color(0xFF080F2A)
    val RewardBackgroundBottom = Color(0xFF040810)
    val RewardCircleDark = Color(0xFF2C1F00)
    val RewardCircleDarker = Color(0xFF0F0A00)

    // Static
    val StaticBlack = Color(0xFF000000)
    val StaticWhite = Color(0xFFFFFFFF)
}
