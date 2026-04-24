package com.river.walklog.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.component.preview.BasePreview
import com.river.walklog.core.designsystem.component.preview.walklogPreview
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme

/**
 * Badge 크기
 */
enum class BadgeSize {
    XSmall,
    Small,
    Medium,
    Large,
}

/**
 * Badge 스타일 (채도)
 */
enum class BadgeVariant {
    Fill,
    Weak,
}

enum class BadgeColor {
    Blue,
    Teal,
    Green,
    Red,
    Yellow,
    Elephant,
}

@Composable
fun BaseBadge(
    text: String,
    size: BadgeSize,
    variant: BadgeVariant,
    color: BadgeColor,
    modifier: Modifier = Modifier,
) {
    val (backgroundColor, textColor) = getBadgeColors(variant, color)
    val (horizontalPadding, verticalPadding) = getBadgePadding(size)
    val cornerRadius = getBadgeCornerRadius(size)
    val textStyle = getBadgeTextStyle(size)

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(cornerRadius),
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = textStyle,
            color = textColor,
        )
    }
}

@Composable
private fun getBadgeColors(
    variant: BadgeVariant,
    color: BadgeColor,
): Pair<Color, Color> {
    return when (variant) {
        BadgeVariant.Fill -> when (color) {
            BadgeColor.Blue -> WalkLogColor.Secondary to WalkLogColor.StaticWhite
            BadgeColor.Teal -> WalkLogColor.Success to WalkLogColor.StaticWhite
            BadgeColor.Green -> WalkLogColor.Success to WalkLogColor.StaticWhite
            BadgeColor.Red -> WalkLogColor.Error to WalkLogColor.StaticWhite
            BadgeColor.Yellow -> WalkLogColor.Primary to WalkLogColor.StaticBlack
            BadgeColor.Elephant -> WalkLogColor.Gray700 to WalkLogColor.StaticWhite
        }

        BadgeVariant.Weak -> when (color) {
            BadgeColor.Blue -> WalkLogColor.SecondaryContainer to WalkLogColor.Secondary
            BadgeColor.Teal -> WalkLogColor.SuccessContainer to WalkLogColor.SuccessDark
            BadgeColor.Green -> WalkLogColor.SuccessContainer to WalkLogColor.SuccessDark
            BadgeColor.Red -> WalkLogColor.ErrorContainer to WalkLogColor.ErrorDark
            BadgeColor.Yellow -> WalkLogColor.PrimaryContainer to WalkLogColor.PrimaryDark
            BadgeColor.Elephant -> WalkLogColor.Gray100 to WalkLogColor.Gray700
        }
    }
}

@Composable
private fun getBadgePadding(size: BadgeSize): Pair<Dp, Dp> {
    return when (size) {
        BadgeSize.XSmall -> 4.dp to 2.dp
        BadgeSize.Small -> 6.dp to 3.dp
        BadgeSize.Medium -> 8.dp to 4.dp
        BadgeSize.Large -> 10.dp to 5.dp
    }
}

@Composable
private fun getBadgeCornerRadius(size: BadgeSize): Dp {
    return when (size) {
        BadgeSize.XSmall -> 4.dp
        BadgeSize.Small -> 4.dp
        BadgeSize.Medium -> 6.dp
        BadgeSize.Large -> 6.dp
    }
}

@Composable
private fun getBadgeTextStyle(size: BadgeSize): TextStyle {
    return when (size) {
        BadgeSize.XSmall -> WalkLogTheme.typography.subTypography13M
        BadgeSize.Small -> WalkLogTheme.typography.subTypography12M
        BadgeSize.Medium -> WalkLogTheme.typography.subTypography11M
        BadgeSize.Large -> WalkLogTheme.typography.subTypography10M
    }
}

@walklogPreview
@Composable
private fun PreviewBadge() {
    BasePreview {
        BaseBadge(
            text = "Badge",
            size = BadgeSize.Medium,
            variant = BadgeVariant.Fill,
            color = BadgeColor.Blue,
        )
    }
}
