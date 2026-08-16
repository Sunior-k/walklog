package com.river.walklog.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.component.preview.BasePreview
import com.river.walklog.core.designsystem.component.preview.walklogPreview
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme

/**
 * 리워드 스토어 "테마 팩" 교환으로 활성화되는 프리미엄 테마 상태를 나타내는 배지.
 */
@Composable
fun PremiumChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "premiumChipShimmer")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerProgress",
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(WalkLogColor.PrimaryDark, WalkLogColor.Primary, WalkLogColor.PrimaryLight),
                ),
            )
            .drawWithContent {
                drawContent()
                val bandWidth = size.width * 0.4f
                val sweepX = size.width * shimmerProgress
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            WalkLogColor.StaticWhite.copy(alpha = 0.5f),
                            Color.Transparent,
                        ),
                        start = Offset(sweepX - bandWidth, 0f),
                        end = Offset(sweepX + bandWidth, size.height),
                    ),
                )
            }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SparkleIcon(modifier = Modifier.size(10.dp))
        Text(
            text = text,
            style = WalkLogTheme.typography.subTypography13B,
            color = WalkLogColor.StaticBlack,
        )
    }
}

@Composable
private fun SparkleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.graphicsLayer()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerRadius = size.minDimension / 2f
        val innerRadius = outerRadius * 0.4f
        val path = Path().apply {
            for (i in 0 until 8) {
                val angle = (Math.PI / 4) * i
                val radius = if (i % 2 == 0) outerRadius else innerRadius
                val x = cx + (radius * kotlin.math.cos(angle)).toFloat()
                val y = cy + (radius * kotlin.math.sin(angle)).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        drawPath(path, color = WalkLogColor.StaticBlack)
    }
}

@walklogPreview
@Composable
private fun PremiumChipPreview() {
    BasePreview {
        PremiumChip(text = "PREMIUM")
    }
}
