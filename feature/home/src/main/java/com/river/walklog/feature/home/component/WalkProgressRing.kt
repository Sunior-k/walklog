package com.river.walklog.feature.home.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.core.model.PremiumVisualMode
import com.river.walklog.core.ui.withComma
import com.river.walklog.feature.home.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

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

    val isPremium = WalkLogTheme.isPremium
    val showNightAura = isPremium && WalkLogTheme.premiumVisualMode == PremiumVisualMode.NIGHT
    val premiumPrimaryColor = WalkLogTheme.colors.primary

    // showNightAura가 false인 대다수 사용자에게 애니메이션 스케줄러가 등록되지 않도록
    // rememberInfiniteTransition/animateFloat 자체를 조건부로 생성하고, State<Float>을
    // 컴포지션 스코프에서 .value로 즉시 구독(by)하지 않는다. 실제 값 읽기는 Canvas의
    // 드로우 페이즈에서만 수행해 애니메이션 프레임마다 리컴포지션이 발생하지 않게 한다.
    val premiumGlowScale: State<Float>?
    val premiumRingTwinklePhase: State<Float>?
    if (showNightAura) {
        val premiumTransition = rememberInfiniteTransition(label = "PremiumRingAura")
        premiumGlowScale = premiumTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "GlowScale",
        )
        premiumRingTwinklePhase = premiumTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing),
            ),
            label = "RingTwinklePhase",
        )
    } else {
        premiumGlowScale = null
        premiumRingTwinklePhase = null
    }

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

            if (showNightAura) {
                val glowScale = premiumGlowScale?.value ?: 1f
                val ringTwinklePhase = premiumRingTwinklePhase?.value ?: 0f
                val glowRadius = radius * 1.35f * glowScale
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            WalkLogColor.Primary.copy(alpha = 0.28f * glowScale),
                            WalkLogColor.Primary.copy(alpha = 0.08f),
                            WalkLogColor.StaticWhite.copy(alpha = 0f),
                        ),
                        center = center,
                        radius = glowRadius,
                    ),
                    radius = glowRadius,
                    center = center,
                )

                // 도트를 회전시키지 않고 고리 둘레에 고정한 채 각자 다른 위상으로 반짝이게 해,
                // 기계적으로 도는 느낌 대신 별자리처럼 반짝이는 느낌을 준다.
                val orbitRadius = radius + strokeWidth
                val dotCount = 16
                repeat(dotCount) { i ->
                    val angle = Math.toRadians(360.0 / dotCount * i)
                    val dotX = center.x + (orbitRadius * cos(angle)).toFloat()
                    val dotY = center.y + (orbitRadius * sin(angle)).toFloat()
                    val twinkle = (sin(ringTwinklePhase + i * 0.7f) + 1f) / 2f
                    val baseAlpha = if (i % 4 == 0) 0.65f else 0.2f
                    drawCircle(
                        color = WalkLogColor.PrimaryLight.copy(alpha = baseAlpha * (0.35f + 0.65f * twinkle)),
                        radius = if (i % 4 == 0) 2.4.dp.toPx() else 1.2.dp.toPx(),
                        center = Offset(dotX, dotY),
                    )
                }
            }

            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth),
            )

            val progressBrush = when {
                progress >= 1f -> SolidColor(WalkLogColor.Success)
                showNightAura -> Brush.sweepGradient(
                    listOf(
                        WalkLogColor.PrimaryDark,
                        WalkLogColor.Primary,
                        WalkLogColor.PrimaryLight,
                        WalkLogColor.Primary,
                        WalkLogColor.PrimaryDark,
                    ),
                    center = center,
                )
                isPremium -> SolidColor(premiumPrimaryColor)
                else -> SolidColor(WalkLogColor.Primary)
            }

            drawArc(
                brush = progressBrush,
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
                text = stringResource(R.string.steps_format, currentSteps.coerceAtLeast(0).withComma()),
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
