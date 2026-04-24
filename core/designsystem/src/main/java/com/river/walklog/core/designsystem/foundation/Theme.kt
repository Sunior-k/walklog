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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat

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

val LocalColors = staticCompositionLocalOf<ColorScheme> { LightColorScheme }
val LocalTypography = staticCompositionLocalOf { Typography }
val LocalDarkTheme = staticCompositionLocalOf { true }

@Composable
fun WalkLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

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
            insets.isAppearanceLightStatusBars = !darkTheme
            insets.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalColors provides colors,
        LocalTypography provides Typography,
        LocalDarkTheme provides darkTheme,
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
}
