package com.river.walklog.feature.reward

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.river.walklog.core.designsystem.R
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun RewardScreen(state: RewardState) {
    val infiniteTransition = rememberInfiniteTransition(label = "reward")

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.80f,
        targetValue = 1.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_scale",
    )

    val particleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
        ),
        label = "particle_angle",
    )

    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
        ),
        label = "ring_rotation",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0C1A47),
                        Color(0xFF080F2A),
                        Color(0xFF040810),
                    ),
                ),
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawStarField()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(20.dp))

            Text(
                text = "W A L K L O G",
                style = WalkLogTheme.typography.typography7M,
                color = WalkLogColor.StaticWhite.copy(alpha = 0.25f),
                letterSpacing = 5.sp,
            )

            Spacer(Modifier.height(44.dp))

            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // 외부 글로우 효과
                    val glowRadius = size.minDimension / 2f * glowScale
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFF5C400).copy(alpha = 0.22f * glowScale),
                                Color(0xFFF5C400).copy(alpha = 0.06f),
                                Color.Transparent,
                            ),
                            radius = glowRadius,
                        ),
                        radius = glowRadius,
                    )

                    // 궤도 링 — 원을 따라 점으로 표현한 점선 링
                    val orbitRadius = size.minDimension / 2.6f
                    val dotCount = 24
                    repeat(dotCount) { i ->
                        val angle = Math.toRadians(
                            (360.0 / dotCount * i) + ringRotation.toDouble(),
                        )
                        val dotX = center.x + (orbitRadius * cos(angle)).toFloat()
                        val dotY = center.y + (orbitRadius * sin(angle)).toFloat()
                        drawCircle(
                            color = Color(0xFFF5C400).copy(alpha = if (i % 3 == 0) 0.55f else 0.15f),
                            radius = if (i % 3 == 0) 2.2.dp.toPx() else 1.2.dp.toPx(),
                            center = Offset(dotX, dotY),
                        )
                    }

                    // 120도 간격으로 회전 입자 3개
                    listOf(0f, 120f, 240f).forEach { offset ->
                        val angle = Math.toRadians((particleAngle + offset).toDouble())
                        val px = center.x + (orbitRadius * cos(angle)).toFloat()
                        val py = center.y + (orbitRadius * sin(angle)).toFloat()
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFF5C400),
                                    Color(0xFFF5C400).copy(alpha = 0f),
                                ),
                                center = Offset(px, py),
                                radius = 6.dp.toPx(),
                            ),
                            radius = 6.dp.toPx(),
                            center = Offset(px, py),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF2C1F00), Color(0xFF0F0A00)),
                            ),
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color(0xFFF5C400).copy(alpha = 0.8f),
                                    Color(0xFFFFE066).copy(alpha = 0.3f),
                                    Color(0xFFF5C400).copy(alpha = 0.8f),
                                ),
                            ),
                            shape = CircleShape,
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(com.river.walklog.feature.reward.R.drawable.ic_crown),
                        contentDescription = "Crown Icon",
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // 제목
            Text(
                text = "REWARD",
                fontWeight = FontWeight.Black,
                fontSize = 38.sp,
                letterSpacing = 10.sp,
                color = WalkLogColor.Primary,
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "걷는 만큼, 쌓이는 보상",
                style = WalkLogTheme.typography.typography5M,
                color = WalkLogColor.StaticWhite.copy(alpha = 0.6f),
            )

            Spacer(Modifier.height(28.dp))

            // 업데이트 예정 안내
            Box(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                WalkLogColor.Primary.copy(alpha = 0.2f),
                                WalkLogColor.Primary.copy(alpha = 0.85f),
                                WalkLogColor.Primary.copy(alpha = 0.2f),
                            ),
                        ),
                        shape = RoundedCornerShape(50),
                    )
                    .background(
                        WalkLogColor.Primary.copy(alpha = 0.08f),
                        RoundedCornerShape(50),
                    )
                    .padding(horizontal = 22.dp, vertical = 9.dp),
            ) {
                Text(
                    text = "✦  추후 업데이트 예정  ✦",
                    style = WalkLogTheme.typography.typography7SB,
                    color = WalkLogColor.Primary,
                    letterSpacing = 1.sp,
                )
            }

            Spacer(Modifier.height(44.dp))

            // 섹션 구분
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    WalkLogColor.StaticWhite.copy(alpha = 0.12f),
                                ),
                            ),
                        ),
                )
                Text(
                    text = "  미리보기  ",
                    style = WalkLogTheme.typography.typography7M,
                    color = WalkLogColor.StaticWhite.copy(alpha = 0.3f),
                    letterSpacing = 2.sp,
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    WalkLogColor.StaticWhite.copy(alpha = 0.12f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )
            }

            Spacer(Modifier.height(20.dp))

            // 기능 미리보기 카드
            val features = listOf(
                RewardFeatureItem(emoji = "🏅", title = "포인트 적립", subtitle = "걸음마다 포인트"),
                RewardFeatureItem(emoji = "🎖", title = "뱃지 컬렉션", subtitle = "성취 배지 모음"),
                RewardFeatureItem(emoji = "🏪", title = "리워드 스토어", subtitle = "포인트로 교환"),
                RewardFeatureItem(emoji = "🎁", title = "레벨 보상", subtitle = "레벨업 선물"),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RewardFeatureCard(feature = features[0], modifier = Modifier.weight(1f))
                    RewardFeatureCard(feature = features[1], modifier = Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RewardFeatureCard(feature = features[2], modifier = Modifier.weight(1f))
                    RewardFeatureCard(feature = features[3], modifier = Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(44.dp))

            // Footer
            Text(
                text = "추후 업데이트를 통해 제공됩니다",
                style = WalkLogTheme.typography.typography7R,
                color = WalkLogColor.StaticWhite.copy(alpha = 0.28f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "꾸준한 걸음이 빛나는 보상으로 돌아와요",
                style = WalkLogTheme.typography.subTypography12R,
                color = WalkLogColor.StaticWhite.copy(alpha = 0.16f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(36.dp))
        }
    }
}

private data class RewardFeatureItem(
    val emoji: String,
    val title: String,
    val subtitle: String,
)

@Composable
private fun RewardFeatureCard(
    feature: RewardFeatureItem,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFFFFFFF).copy(alpha = 0.07f),
                        Color(0xFFFFFFFF).copy(alpha = 0.03f),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        WalkLogColor.StaticWhite.copy(alpha = 0.14f),
                        WalkLogColor.StaticWhite.copy(alpha = 0.04f),
                    ),
                ),
                shape = RoundedCornerShape(18.dp),
            )
            .padding(18.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = feature.emoji,
                    fontSize = 26.sp,
                )
                Spacer(Modifier.weight(1f))
                // 잠금 배지
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(WalkLogColor.StaticWhite.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "🔒",
                        fontSize = 9.sp,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = feature.title,
                style = WalkLogTheme.typography.typography6SB,
                color = WalkLogColor.StaticWhite.copy(alpha = 0.82f),
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = feature.subtitle,
                style = WalkLogTheme.typography.subTypography12R,
                color = WalkLogColor.StaticWhite.copy(alpha = 0.38f),
            )
        }

        // 카드 상단 골드 shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            WalkLogColor.Primary.copy(alpha = 0.35f),
                            Color.Transparent,
                        ),
                    ),
                )
                .align(Alignment.TopCenter),
        )
    }
}

private fun DrawScope.drawStarField() {
    val positions = listOf(
        Pair(0.12f, 0.06f), Pair(0.74f, 0.10f), Pair(0.43f, 0.04f),
        Pair(0.89f, 0.19f), Pair(0.26f, 0.32f), Pair(0.94f, 0.41f),
        Pair(0.05f, 0.55f), Pair(0.63f, 0.25f), Pair(0.37f, 0.72f),
        Pair(0.80f, 0.60f), Pair(0.10f, 0.80f), Pair(0.52f, 0.90f),
        Pair(0.68f, 0.50f), Pair(0.20f, 0.48f), Pair(0.58f, 0.15f),
        Pair(0.84f, 0.78f), Pair(0.33f, 0.95f), Pair(0.77f, 0.35f),
    )
    positions.forEach { (fx, fy) ->
        val alpha = if ((fx * 10).toInt() % 3 == 0) 0.4f else 0.18f
        val radius = if ((fy * 10).toInt() % 4 == 0) 1.8.dp.toPx() else 1.dp.toPx()
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = radius,
            center = Offset(size.width * fx, size.height * fy),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RewardScreenPreview() {
    WalkLogTheme {
        RewardScreen(state = RewardState())
    }
}
