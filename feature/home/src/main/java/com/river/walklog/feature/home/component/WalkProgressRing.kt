package com.river.walklog.feature.home.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import kotlin.math.min

@Composable
fun WalkProgressRing(
    currentSteps: Int,
    targetSteps: Int,
    isWalking: Boolean = false,
    modifier: Modifier = Modifier,
    showStepText: Boolean = true,
) {
    val progress = if (targetSteps <= 0) {
        0f
    } else {
        (currentSteps.toFloat() / targetSteps.toFloat()).coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "WalkProgressRingAnimation",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "WalkingPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = RingTrackAlpha,
        targetValue = RingTrackAlpha * 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "PulseAlpha",
    )
    val trackAlpha = if (isWalking) pulseAlpha else RingTrackAlpha
    val trackColor = WalkLogTheme.colors.onSurface.copy(alpha = trackAlpha)

    Box(
        modifier = modifier
            .size(220.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 20.dp.toPx()
            val diameter = min(size.width, size.height)
            val radius = (diameter - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth),
            )

            drawArc(
                color = if (progress >= 1f) WalkLogColor.Success else WalkLogColor.Primary,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(x = center.x - radius, y = center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }

        if (showStepText) {
            Text(
                text = "${currentSteps.coerceAtLeast(0)}보",
                style = WalkLogTheme.typography.typography3B,
                color = WalkLogTheme.colors.onSurface,
            )
        }
    }
}

private const val RingTrackAlpha = 0.18f

@Preview @Composable
private fun WalkProgressRingPreview() {
    WalkLogTheme { WalkProgressRing(currentSteps = 7500, targetSteps = 10000) }
}

@Preview @Composable
private fun WalkProgressRingWalkingPreview() {
    WalkLogTheme { WalkProgressRing(currentSteps = 7500, targetSteps = 10000, isWalking = true) }
}
