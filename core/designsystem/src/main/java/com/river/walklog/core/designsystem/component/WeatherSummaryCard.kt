package com.river.walklog.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.R
import com.river.walklog.core.designsystem.foundation.WalkLogTheme

@Composable
fun WeatherSummaryCard(
    locationText: String,
    temperatureText: String,
    conditionText: String,
    adviceText: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    isLoading: Boolean = false,
    onRefreshClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(WalkLogTheme.colors.surface)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (isLoading) {
            WeatherCardSkeleton(
                modifier = Modifier.weight(1f),
                onRefreshClick = onRefreshClick,
            )
        } else {
            WeatherCardContent(
                modifier = Modifier.weight(1f),
                locationText = locationText,
                temperatureText = temperatureText,
                conditionText = conditionText,
                adviceText = adviceText,
                supportingText = supportingText,
                onRefreshClick = onRefreshClick,
            )
        }
    }
}

@Composable
private fun WeatherCardContent(
    modifier: Modifier,
    locationText: String,
    temperatureText: String,
    conditionText: String,
    adviceText: String,
    supportingText: String?,
    onRefreshClick: (() -> Unit)?,
) {
    WeatherConditionMark(conditionText = conditionText)
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = locationText,
                style = WalkLogTheme.typography.typography7M,
                color = WalkLogTheme.colors.onSurfaceVariant,
            )
            RefreshButton(onRefreshClick = onRefreshClick, enabled = true)
        }
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = temperatureText,
                    style = WalkLogTheme.typography.typography3B,
                    color = WalkLogTheme.colors.onSurface,
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = conditionText,
                    style = WalkLogTheme.typography.typography6SB,
                    color = WalkLogTheme.colors.onSurface,
                )
            }
            Text(
                text = adviceText,
                style = WalkLogTheme.typography.subTypography12SB,
                color = WalkLogTheme.colors.onSurfaceVariant,
            )
            Text(
                text = supportingText ?: " ",
                style = WalkLogTheme.typography.subTypography12M,
                color = WalkLogTheme.colors.onSurfaceVariant,
                modifier = if (supportingText != null) Modifier else Modifier.alpha(0f),
            )
        }
    }
}

@Composable
private fun WeatherCardSkeleton(
    modifier: Modifier,
    onRefreshClick: (() -> Unit)?,
) {
    val shimmerAlpha by rememberInfiniteTransition(label = "weather_shimmer").animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(700), repeatMode = RepeatMode.Reverse),
        label = "weather_shimmer_alpha",
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .background(WalkLogTheme.colors.primaryContainer.copy(alpha = shimmerAlpha), CircleShape),
    )
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerRow(
                modifier = Modifier.weight(1f),
                alpha = shimmerAlpha * 0.12f,
                widthFraction = 0.55f,
                cornerRadius = 4.dp,
            ) {
                Text("", style = WalkLogTheme.typography.typography7M)
            }
            RefreshButton(onRefreshClick = onRefreshClick, enabled = false)
        }
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            ShimmerRow(alpha = shimmerAlpha * 0.15f, widthFraction = 0.50f, cornerRadius = 6.dp) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(" ", style = WalkLogTheme.typography.typography3B)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(" ", style = WalkLogTheme.typography.typography6SB)
                }
            }
            ShimmerRow(alpha = shimmerAlpha * 0.10f, widthFraction = 0.80f, cornerRadius = 4.dp) {
                Text(" ", style = WalkLogTheme.typography.subTypography12SB)
            }
            ShimmerRow(alpha = shimmerAlpha * 0.10f, widthFraction = 0.65f, cornerRadius = 4.dp) {
                Text(" ", style = WalkLogTheme.typography.subTypography12M)
            }
        }
    }
}

@Composable
private fun RefreshButton(onRefreshClick: (() -> Unit)?, enabled: Boolean) {
    if (onRefreshClick == null) return
    IconButton(
        modifier = Modifier.size(36.dp),
        onClick = onRefreshClick,
        enabled = enabled,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_refresh),
            contentDescription = "날씨 새로고침",
            tint = WalkLogTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ShimmerRow(
    alpha: Float,
    widthFraction: Float,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.alpha(0f)) { content() }
        Box(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(widthFraction)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(WalkLogTheme.colors.onSurface.copy(alpha = alpha)),
            )
        }
    }
}

@Composable
private fun WeatherConditionMark(conditionText: String) {
    Column(
        modifier = Modifier
            .size(56.dp)
            .background(WalkLogTheme.colors.primaryContainer, CircleShape),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = conditionText.take(2),
            style = WalkLogTheme.typography.typography7SB,
            color = WalkLogTheme.colors.onPrimaryContainer,
        )
    }
}

@Preview
@Composable
private fun WeatherSummaryCardPreview() {
    WalkLogTheme {
        WeatherSummaryCard(
            locationText = "서울 기준",
            temperatureText = "18°",
            conditionText = "맑음",
            adviceText = "맑은 날씨예요. 가볍게 걷기 좋아요",
            supportingText = "습도 45% · 강수 0%",
            onRefreshClick = {},
        )
    }
}

@Preview
@Composable
private fun WeatherSummaryCardLoadingPreview() {
    WalkLogTheme {
        WeatherSummaryCard(
            locationText = "",
            temperatureText = "",
            conditionText = "",
            adviceText = "",
            isLoading = true,
            onRefreshClick = {},
        )
    }
}
