package com.river.walklog.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.model.PremiumVisualMode
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * 프리미엄 테마의 배경 효과 — 시간대/날씨([PremiumVisualMode])에 따라 밤(별+유성우),
 * 낮/맑음(태양), 낮/비·눈·흐림(구름+빗줄기+가끔 번개) 중 하나를 그린다. 홈·포인트 적립 내역
 * 화면에서 공용.
 */
@Composable
fun PremiumBackgroundEffect(mode: PremiumVisualMode, modifier: Modifier = Modifier) {
    when (mode) {
        PremiumVisualMode.NIGHT -> NightMeteorEffect(modifier)
        PremiumVisualMode.DAY_CLEAR -> SunGlowEffect(modifier)
        PremiumVisualMode.DAY_WET -> RainStreakEffect(modifier)
    }
}

private val MeteorPhaseOffsets = listOf(0f, 0.33f, 0.66f)
private val MeteorStartFractionsY = listOf(0.05f, 0.30f, 0.55f)

@Composable
private fun NightMeteorEffect(modifier: Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "nightMeteor")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "meteorProgress",
    )
    val twinklePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "starTwinklePhase",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawTwinklingStars(twinklePhase)

        // 유성 이동 방향(우상단→좌하단)의 반대쪽으로 꼬리를 그려야 "머리가 앞서고 꼬리가
        // 흐려지며 뒤따르는" 모양이 된다 — 꼬리를 이동 방향과 같은 쪽으로 그리면 유성이
        // 거꾸로 가는 것처럼 보인다.
        val travelAngle = atan2(0.9f * size.height, -1.4f * size.width)
        val tailAngle = travelAngle + PI.toFloat()
        val tailLength = 46.dp.toPx()

        MeteorPhaseOffsets.forEachIndexed { index, phaseOffset ->
            val t = (progress + phaseOffset) % 1f
            val alpha = sin(t * PI).toFloat().coerceIn(0f, 1f)
            if (alpha <= 0.02f) return@forEachIndexed

            val headX = size.width * (1.2f - 1.4f * t)
            val headY = size.height * (MeteorStartFractionsY[index] + 0.9f * t)
            val head = Offset(headX, headY)
            val tail = Offset(
                headX + (tailLength * cos(tailAngle)),
                headY + (tailLength * sin(tailAngle)),
            )
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(WalkLogColor.StaticWhite.copy(alpha = alpha), Color.Transparent),
                    start = head,
                    end = tail,
                ),
                start = head,
                end = tail,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

private val StarScatterPositions = listOf(
    0.08f to 0.12f, 0.85f to 0.08f, 0.20f to 0.85f,
    0.92f to 0.75f, 0.65f to 0.15f, 0.12f to 0.55f,
    0.75f to 0.45f, 0.45f to 0.90f, 0.35f to 0.30f,
)

private fun DrawScope.drawTwinklingStars(twinklePhase: Float) {
    StarScatterPositions.forEachIndexed { index, (fx, fy) ->
        val baseAlpha = if ((fx * 10).toInt() % 2 == 0) 0.5f else 0.22f
        val radius = if ((fy * 10).toInt() % 3 == 0) 1.8.dp.toPx() else 1.dp.toPx()
        val twinkle = (sin(twinklePhase + index * 0.9f) + 1f) / 2f
        val alpha = baseAlpha * (0.4f + 0.6f * twinkle)
        drawCircle(
            color = WalkLogColor.PrimaryLight.copy(alpha = alpha),
            radius = radius,
            center = Offset(size.width * fx, size.height * fy),
        )
    }
}

private val SunColor = WalkLogColor.PremiumSunColor
private val SunCoreGradient = listOf(WalkLogColor.PremiumSunCoreStart, WalkLogColor.PremiumSunCoreEnd)

@Composable
private fun SunGlowEffect(modifier: Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "sunGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(2600), repeatMode = RepeatMode.Reverse),
        label = "sunGlowScale",
    )
    val rayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20_000, easing = LinearEasing)),
        label = "sunRayRotation",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val sunCenter = Offset(size.width * 0.82f, size.height * 0.18f)
        val sunRadius = size.minDimension * 0.16f
        val glowRadius = sunRadius * 2.2f * glowScale

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    SunColor.copy(alpha = 0.35f * glowScale),
                    SunColor.copy(alpha = 0.08f),
                    Color.Transparent,
                ),
                center = sunCenter,
                radius = glowRadius,
            ),
            radius = glowRadius,
            center = sunCenter,
        )

        rotate(degrees = rayRotation, pivot = sunCenter) {
            repeat(8) { i ->
                val angle = Math.toRadians(360.0 / 8 * i)
                val innerRadius = sunRadius * 1.3f
                val outerRadius = sunRadius * 1.9f
                drawLine(
                    color = SunColor.copy(alpha = 0.4f),
                    start = sunCenter + Offset((innerRadius * cos(angle)).toFloat(), (innerRadius * sin(angle)).toFloat()),
                    end = sunCenter + Offset((outerRadius * cos(angle)).toFloat(), (outerRadius * sin(angle)).toFloat()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }

        drawCircle(
            brush = Brush.radialGradient(SunCoreGradient, center = sunCenter, radius = sunRadius),
            radius = sunRadius,
            center = sunCenter,
        )
    }
}

private const val RainStreakCount = 24
private val CloudClusters = listOf(
    Triple(0.15f, 0.12f, 0.20f), Triple(0.32f, 0.08f, 0.15f),
    Triple(0.68f, 0.17f, 0.22f), Triple(0.86f, 0.10f, 0.14f),
)

@Composable
private fun RainStreakEffect(modifier: Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "rainProgress",
    )
    // 대부분은 어둡게 유지하다가 7초 주기의 끝에서만 두 번 깜빡여, 실제 번개처럼 불규칙하게 친다.
    val lightningAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7000
                0f at 0
                0f at 6550
                0.9f at 6600
                0.1f at 6650
                0.7f at 6690
                0f at 6820
                0f at 7000
            },
        ),
        label = "lightningFlash",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawCloudBlobs()

        val length = 18.dp.toPx()
        val drift = 4.dp.toPx()
        repeat(RainStreakCount) { i ->
            val phase = i / RainStreakCount.toFloat()
            val t = (progress + phase) % 1f
            val xFraction = (i * 0.618f) % 1f
            val startX = size.width * xFraction
            val startY = size.height * t - size.height * 0.1f
            drawLine(
                color = WalkLogColor.Secondary.copy(alpha = 0.35f),
                start = Offset(startX, startY),
                end = Offset(startX + drift, startY + length),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        if (lightningAlpha > 0.01f) {
            drawLightningFlash(lightningAlpha)
        }
    }
}

private fun DrawScope.drawCloudBlobs() {
    val cloudColor = WalkLogColor.PremiumDayWetSurfaceVariant
    CloudClusters.forEach { (fx, fy, fr) ->
        val cx = size.width * fx
        val r = size.minDimension * fr
        // 카드 미리보기처럼 캔버스가 낮을 때 구름 위쪽이 잘리지 않도록, 중심을 반지름만큼 아래로 고정한다.
        val cy = (size.height * fy).coerceAtLeast(r * 1.15f)
        listOf(
            Offset(cx - r * 0.5f, cy) to r,
            Offset(cx + r * 0.4f, cy - r * 0.15f) to r * 0.8f,
            Offset(cx + r * 0.1f, cy + r * 0.25f) to r * 0.65f,
        ).forEach { (center, radius) ->
            // 원 여러 개를 이음매가 보이는 단색 대신, 부드럽게 퍼지는 방사형 그라디언트로 겹쳐
            // 뭉게구름처럼 자연스럽게 번지는 형태를 만든다.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(cloudColor.copy(alpha = 0.8f), cloudColor.copy(alpha = 0f)),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )
        }
    }
}

private fun DrawScope.drawLightningFlash(alpha: Float) {
    drawRect(color = WalkLogColor.StaticWhite.copy(alpha = alpha * 0.4f))

    val originX = size.width * 0.62f
    val bolt = Path().apply {
        moveTo(originX, 0f)
        lineTo(originX - size.width * 0.05f, size.height * 0.22f)
        lineTo(originX + size.width * 0.03f, size.height * 0.24f)
        lineTo(originX - size.width * 0.08f, size.height * 0.48f)
        lineTo(originX + size.width * 0.02f, size.height * 0.50f)
        lineTo(originX - size.width * 0.10f, size.height * 0.75f)
    }
    drawPath(
        path = bolt,
        color = WalkLogColor.PremiumLightningBolt.copy(alpha = alpha * 0.9f),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}
