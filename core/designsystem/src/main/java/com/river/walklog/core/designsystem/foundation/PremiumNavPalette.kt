package com.river.walklog.core.designsystem.foundation

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.toArgb
import com.river.walklog.core.model.PremiumVisualMode

/**
 * 바텀 내비게이션(순수 Android View)은 Compose 컴포지션 밖이라 [WalkLogTheme]을 못 쓴다.
 * 프리미엄 팔레트를 런타임에 직접 칠하기 위한 @ColorInt 값 3세트.
 */
data class PremiumNavColors(
    @ColorInt val surfaceColor: Int,
    @ColorInt val selectedColor: Int,
    @ColorInt val unselectedColor: Int,
)

object PremiumNavPalette {
    fun forMode(mode: PremiumVisualMode): PremiumNavColors = when (mode) {
        PremiumVisualMode.NIGHT -> PremiumNavColors(
            surfaceColor = WalkLogColor.PremiumSurface.toArgb(),
            selectedColor = WalkLogColor.Primary.toArgb(),
            unselectedColor = WalkLogColor.Gray300.toArgb(),
        )
        PremiumVisualMode.DAY_CLEAR -> PremiumNavColors(
            surfaceColor = WalkLogColor.PremiumDayClearBackground.toArgb(),
            selectedColor = WalkLogColor.PrimaryDark.toArgb(),
            unselectedColor = WalkLogColor.Gray500.toArgb(),
        )
        PremiumVisualMode.DAY_WET -> PremiumNavColors(
            surfaceColor = WalkLogColor.PremiumDayWetBackground.toArgb(),
            selectedColor = WalkLogColor.Secondary.toArgb(),
            unselectedColor = WalkLogColor.Gray500.toArgb(),
        )
    }
}
