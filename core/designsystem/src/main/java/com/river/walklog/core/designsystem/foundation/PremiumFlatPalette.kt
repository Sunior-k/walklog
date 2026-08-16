package com.river.walklog.core.designsystem.foundation

import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.toArgb
import com.river.walklog.core.model.PremiumVisualMode

/**
 * History/Settings처럼 순수 Android View 기반이라 [WalkLogTheme]을 못 쓰는 화면에서, 배경/카드
 * 색만 런타임에 재칠하기 위한 팔레트. NIGHT는 [WalkLogTheme]의 프리미엄 Compose 화면(홈·포인트
 * 내역)과 같은 어두운 남색 카드 + 밝은 텍스트를 쓰고, DAY_CLEAR/DAY_WET은 밝은 카드 + 어두운
 * 텍스트를 쓴다 — 즉 카드는 배경과 항상 같은 명암 계열을 유지해 "밝은 카드가 어두운 배경 위에
 * 떠 있는" 반전 현상이 생기지 않는다.
 */
data class PremiumFlatColors(
    @ColorInt val background: Int,
    @ColorInt val cardBackground: Int,
    @ColorInt val onBackground: Int,
    @ColorInt val onBackgroundMuted: Int,
    @ColorInt val onCard: Int,
    @ColorInt val onCardMuted: Int,
    @ColorInt val cardOutline: Int,
)

object PremiumFlatPalette {
    fun forMode(mode: PremiumVisualMode): PremiumFlatColors = when (mode) {
        PremiumVisualMode.NIGHT -> PremiumFlatColors(
            background = WalkLogColor.PremiumBackground.toArgb(),
            cardBackground = WalkLogColor.PremiumSurfaceVariant.toArgb(),
            onBackground = WalkLogColor.StaticWhite.toArgb(),
            onBackgroundMuted = WalkLogColor.Gray300.toArgb(),
            onCard = WalkLogColor.StaticWhite.toArgb(),
            onCardMuted = WalkLogColor.Gray300.toArgb(),
            cardOutline = WalkLogColor.PremiumOutlineVariant.toArgb(),
        )
        PremiumVisualMode.DAY_CLEAR -> PremiumFlatColors(
            background = WalkLogColor.PremiumDayClearBackground.toArgb(),
            cardBackground = WalkLogColor.PremiumDayClearSurfaceVariant.toArgb(),
            onBackground = WalkLogColor.Gray900.toArgb(),
            onBackgroundMuted = WalkLogColor.Gray700.toArgb(),
            onCard = WalkLogColor.Gray900.toArgb(),
            onCardMuted = WalkLogColor.Gray700.toArgb(),
            cardOutline = WalkLogColor.PremiumDayClearOutlineVariant.toArgb(),
        )
        PremiumVisualMode.DAY_WET -> PremiumFlatColors(
            background = WalkLogColor.PremiumDayWetBackground.toArgb(),
            cardBackground = WalkLogColor.PremiumDayWetSurfaceVariant.toArgb(),
            onBackground = WalkLogColor.Gray900.toArgb(),
            onBackgroundMuted = WalkLogColor.Gray700.toArgb(),
            onCard = WalkLogColor.Gray900.toArgb(),
            onCardMuted = WalkLogColor.Gray700.toArgb(),
            cardOutline = WalkLogColor.PremiumDayWetOutlineVariant.toArgb(),
        )
    }
}

/** 배경이 [GradientDrawable]이면 모양은 유지한 채 채우기 색만 바꾸고, 아니면 단색으로 덮어쓴다. */
fun View.setPremiumCardColor(@ColorInt color: Int) {
    val current = background
    if (current is GradientDrawable) {
        (current.mutate() as GradientDrawable).setColor(color)
    } else {
        setBackgroundColor(color)
    }
}
