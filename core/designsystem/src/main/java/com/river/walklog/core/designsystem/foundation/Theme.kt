package com.river.walklog.core.designsystem.foundation

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import com.river.walklog.core.model.PremiumVisualMode

private val LightColorScheme = lightColorScheme(
    primary = WalkLogColor.Primary,
    onPrimary = WalkLogColor.StaticBlack,
    primaryContainer = WalkLogColor.PrimaryContainer,
    onPrimaryContainer = WalkLogColor.Gray900,
    inversePrimary = WalkLogColor.PrimaryDark,
    secondary = WalkLogColor.Secondary,
    onSecondary = WalkLogColor.StaticWhite,
    secondaryContainer = WalkLogColor.SecondaryContainer,
    onSecondaryContainer = WalkLogColor.StaticWhite,
    tertiary = WalkLogColor.Success,
    onTertiary = WalkLogColor.StaticWhite,
    tertiaryContainer = WalkLogColor.SuccessContainer,
    onTertiaryContainer = WalkLogColor.SuccessDark,
    error = WalkLogColor.Error,
    onError = WalkLogColor.StaticWhite,
    errorContainer = WalkLogColor.ErrorContainer,
    onErrorContainer = WalkLogColor.ErrorDark,
    background = WalkLogColor.Background,
    onBackground = WalkLogColor.TextPrimary,
    surface = WalkLogColor.Surface,
    onSurface = WalkLogColor.TextPrimary,
    surfaceVariant = WalkLogColor.SurfaceVariant,
    onSurfaceVariant = WalkLogColor.TextSecondary,
    outline = WalkLogColor.Gray300,
    outlineVariant = WalkLogColor.Gray200,
    scrim = WalkLogColor.StaticBlack,
    inverseSurface = WalkLogColor.Gray900,
    inverseOnSurface = WalkLogColor.StaticWhite,
)

private val DarkColorScheme = darkColorScheme(
    primary = WalkLogColor.Primary,
    onPrimary = WalkLogColor.StaticBlack,
    primaryContainer = WalkLogColor.PrimaryDark,
    onPrimaryContainer = WalkLogColor.PrimaryLight,
    secondary = WalkLogColor.Secondary,
    onSecondary = WalkLogColor.Gray900,
    secondaryContainer = WalkLogColor.SecondaryContainer,
    onSecondaryContainer = WalkLogColor.StaticWhite,
    tertiary = WalkLogColor.Success,
    onTertiary = WalkLogColor.StaticBlack,
    tertiaryContainer = WalkLogColor.SuccessDark,
    onTertiaryContainer = WalkLogColor.SuccessContainer,
    error = WalkLogColor.Error,
    onError = WalkLogColor.StaticBlack,
    errorContainer = WalkLogColor.ErrorDark,
    onErrorContainer = WalkLogColor.ErrorContainer,
    background = WalkLogColor.Gray900,
    onBackground = WalkLogColor.StaticWhite,
    surface = WalkLogColor.Gray700,
    onSurface = WalkLogColor.StaticWhite,
    surfaceVariant = WalkLogColor.Gray700,
    onSurfaceVariant = WalkLogColor.Gray300,
    outline = WalkLogColor.Gray500,
    outlineVariant = WalkLogColor.Gray700,
    scrim = WalkLogColor.StaticBlack,
    inverseSurface = WalkLogColor.Gray100,
    inverseOnSurface = WalkLogColor.Gray900,
)

/**
 * 리워드 스토어 "테마 팩" 교환 시 활성화되는 프리미엄 팔레트 3종.
 * 시스템 라이트/다크 모드와 무관하게, 현재 시간대·날씨([PremiumVisualMode])에 따라 선택된다.
 * NIGHT는 항상 다크(별/유성우와 어울리는 남색+골드), DAY_CLEAR/DAY_WET은 라이트 팔레트다.
 */
private val PremiumNightColorScheme = darkColorScheme(
    primary = WalkLogColor.Primary,
    onPrimary = WalkLogColor.StaticBlack,
    primaryContainer = WalkLogColor.PrimaryDark,
    onPrimaryContainer = WalkLogColor.PrimaryLight,
    secondary = WalkLogColor.PrimaryLight,
    onSecondary = WalkLogColor.StaticBlack,
    secondaryContainer = WalkLogColor.PrimaryContainerDark,
    onSecondaryContainer = WalkLogColor.PrimaryLight,
    tertiary = WalkLogColor.Success,
    onTertiary = WalkLogColor.StaticBlack,
    tertiaryContainer = WalkLogColor.SuccessDark,
    onTertiaryContainer = WalkLogColor.SuccessContainer,
    error = WalkLogColor.Error,
    onError = WalkLogColor.StaticWhite,
    errorContainer = WalkLogColor.ErrorDark,
    onErrorContainer = WalkLogColor.ErrorContainer,
    background = WalkLogColor.PremiumBackground,
    onBackground = WalkLogColor.StaticWhite,
    surface = WalkLogColor.PremiumSurface,
    onSurface = WalkLogColor.StaticWhite,
    surfaceVariant = WalkLogColor.PremiumSurfaceVariant,
    onSurfaceVariant = WalkLogColor.Gray300,
    outline = WalkLogColor.PrimaryDark,
    outlineVariant = WalkLogColor.PremiumOutlineVariant,
    scrim = WalkLogColor.StaticBlack,
    inverseSurface = WalkLogColor.Gray100,
    inverseOnSurface = WalkLogColor.Gray900,
)

private val PremiumDayClearColorScheme = lightColorScheme(
    primary = WalkLogColor.PrimaryDark,
    onPrimary = WalkLogColor.StaticWhite,
    primaryContainer = WalkLogColor.PrimaryContainer,
    onPrimaryContainer = WalkLogColor.PrimaryContainerDark,
    secondary = WalkLogColor.Accent,
    onSecondary = WalkLogColor.StaticWhite,
    secondaryContainer = WalkLogColor.PremiumDayClearSurfaceVariant,
    onSecondaryContainer = WalkLogColor.Gray900,
    tertiary = WalkLogColor.Success,
    onTertiary = WalkLogColor.StaticWhite,
    tertiaryContainer = WalkLogColor.SuccessContainer,
    onTertiaryContainer = WalkLogColor.SuccessDark,
    error = WalkLogColor.Error,
    onError = WalkLogColor.StaticWhite,
    errorContainer = WalkLogColor.ErrorContainer,
    onErrorContainer = WalkLogColor.ErrorDark,
    background = WalkLogColor.PremiumDayClearBackground,
    onBackground = WalkLogColor.Gray900,
    surface = WalkLogColor.PremiumDayClearSurface,
    onSurface = WalkLogColor.Gray900,
    surfaceVariant = WalkLogColor.PremiumDayClearSurfaceVariant,
    onSurfaceVariant = WalkLogColor.Gray700,
    outline = WalkLogColor.PremiumDayClearOutlineVariant,
    outlineVariant = WalkLogColor.PremiumDayClearSurfaceVariant,
    scrim = WalkLogColor.StaticBlack,
    inverseSurface = WalkLogColor.Gray900,
    inverseOnSurface = WalkLogColor.StaticWhite,
)

private val PremiumDayWetColorScheme = lightColorScheme(
    primary = WalkLogColor.Secondary,
    onPrimary = WalkLogColor.StaticWhite,
    primaryContainer = WalkLogColor.SecondaryContainer,
    onPrimaryContainer = WalkLogColor.StaticWhite,
    secondary = WalkLogColor.PrimaryDark,
    onSecondary = WalkLogColor.StaticWhite,
    secondaryContainer = WalkLogColor.PremiumDayWetSurfaceVariant,
    onSecondaryContainer = WalkLogColor.Gray900,
    tertiary = WalkLogColor.Success,
    onTertiary = WalkLogColor.StaticWhite,
    tertiaryContainer = WalkLogColor.SuccessContainer,
    onTertiaryContainer = WalkLogColor.SuccessDark,
    error = WalkLogColor.Error,
    onError = WalkLogColor.StaticWhite,
    errorContainer = WalkLogColor.ErrorContainer,
    onErrorContainer = WalkLogColor.ErrorDark,
    background = WalkLogColor.PremiumDayWetBackground,
    onBackground = WalkLogColor.Gray900,
    surface = WalkLogColor.PremiumDayWetSurface,
    onSurface = WalkLogColor.Gray900,
    surfaceVariant = WalkLogColor.PremiumDayWetSurfaceVariant,
    onSurfaceVariant = WalkLogColor.Gray700,
    outline = WalkLogColor.PremiumDayWetOutlineVariant,
    outlineVariant = WalkLogColor.PremiumDayWetSurfaceVariant,
    scrim = WalkLogColor.StaticBlack,
    inverseSurface = WalkLogColor.Gray900,
    inverseOnSurface = WalkLogColor.StaticWhite,
)

val LocalColors = staticCompositionLocalOf<ColorScheme> { LightColorScheme }
val LocalTypography = staticCompositionLocalOf { Typography }
val LocalDarkTheme = staticCompositionLocalOf { true }
val LocalIsPremiumTheme = staticCompositionLocalOf { false }
val LocalPremiumVisualMode = staticCompositionLocalOf { PremiumVisualMode.NIGHT }

@Composable
fun WalkLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isPremiumTheme: Boolean = false,
    premiumVisualMode: PremiumVisualMode = PremiumVisualMode.NIGHT,
    content: @Composable () -> Unit,
) {
    val colors = when {
        isPremiumTheme -> when (premiumVisualMode) {
            PremiumVisualMode.NIGHT -> PremiumNightColorScheme
            PremiumVisualMode.DAY_CLEAR -> PremiumDayClearColorScheme
            PremiumVisualMode.DAY_WET -> PremiumDayWetColorScheme
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val locale = LocalConfiguration.current.locales[0]
    val fontScale = when (locale.language) {
        "en", "ja" -> 0.9f
        else -> 1.0f
    }
    val scaledTypography = remember(fontScale) { Typography.scale(fontScale) }
    val useDarkSystemBars = if (isPremiumTheme) premiumVisualMode == PremiumVisualMode.NIGHT else darkTheme

    if (!LocalInspectionMode.current) {
        val view = LocalView.current
        SideEffect {
            val window = (view.context as Activity).window

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                @Suppress("DEPRECATION")
                window.statusBarColor = colors.background.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = colors.background.toArgb()
            }
            val insets = WindowCompat.getInsetsController(window, view)
            insets.isAppearanceLightStatusBars = !useDarkSystemBars
            insets.isAppearanceLightNavigationBars = !useDarkSystemBars
        }
    }

    CompositionLocalProvider(
        LocalColors provides colors,
        LocalTypography provides scaledTypography,
        LocalDarkTheme provides useDarkSystemBars,
        LocalIsPremiumTheme provides isPremiumTheme,
        LocalPremiumVisualMode provides premiumVisualMode,
        LocalDensity provides Density(LocalDensity.current.density, 1f),
    ) {
        content()
    }
}

object WalkLogTheme {
    val colors: ColorScheme
        @Composable get() = LocalColors.current
    val typography: WalkLogTypography
        @Composable get() = LocalTypography.current
    val isDark: Boolean
        @Composable get() = LocalDarkTheme.current
    val isPremium: Boolean
        @Composable get() = LocalIsPremiumTheme.current
    val premiumVisualMode: PremiumVisualMode
        @Composable get() = LocalPremiumVisualMode.current
}
