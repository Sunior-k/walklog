package com.river.walklog.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastBottomSheet(
    title: String,
    recommendedTimeText: String,
    description: String,
    averageStepsAtThisTime: String,
    activeDays: String,
    hourlyAverages: List<Float>,
    peakHour: Int,
    onDismiss: () -> Unit,
    onClickStartWalking: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isExpanded = currentWindowAdaptiveInfo().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        sheetMaxWidth = Dp.Unspecified,
        containerColor = WalkLogTheme.colors.background,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            ForecastSheetContent(
                title = title,
                recommendedTimeText = recommendedTimeText,
                description = description,
                averageStepsAtThisTime = averageStepsAtThisTime,
                activeDays = activeDays,
                hourlyAverages = hourlyAverages,
                peakHour = peakHour,
                onClickStartWalking = onClickStartWalking,
                modifier = if (isExpanded) {
                    Modifier.widthIn(max = 860.dp)
                } else {
                    Modifier
                },
            )
        }
    }
}

@Composable
private fun ForecastSheetContent(
    title: String,
    recommendedTimeText: String,
    description: String,
    averageStepsAtThisTime: String,
    activeDays: String,
    hourlyAverages: List<Float>,
    peakHour: Int,
    onClickStartWalking: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 4.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ForecastSheetHeader(
            title = title,
            recommendedTimeText = recommendedTimeText,
        )
        ForecastChartIfAvailable(hourlyAverages = hourlyAverages, peakHour = peakHour)
        ForecastDescriptionCard(description = description)
        ForecastPatternCard(
            averageStepsAtThisTime = averageStepsAtThisTime,
            activeDays = activeDays,
        )
        ForecastStartButton(onClickStartWalking = onClickStartWalking)
    }
}

@Composable
private fun ForecastChartIfAvailable(
    hourlyAverages: List<Float>,
    peakHour: Int,
) {
    if (hourlyAverages.isNotEmpty() && hourlyAverages.any { it > 0f }) {
        ForecastLineChartCard(
            hourlyAverages = hourlyAverages,
            peakHour = peakHour,
        )
    }
}

@Composable
private fun ForecastStartButton(onClickStartWalking: () -> Unit) {
    Button(
        onClick = onClickStartWalking,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = WalkLogColor.Primary,
            contentColor = WalkLogColor.StaticBlack,
        ),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        Text(text = "지금 걸으러 가기", style = WalkLogTheme.typography.typography6SB)
    }
}

@Composable
private fun ForecastLineChartCard(
    hourlyAverages: List<Float>,
    peakHour: Int,
) {
    val primaryColor = WalkLogColor.Primary
    val gridLineColor = WalkLogTheme.colors.onSurface.copy(alpha = 0.07f)
    val labelColor = WalkLogTheme.colors.onSurfaceVariant.copy(alpha = 0.55f)
    val density = LocalDensity.current
    val labelSizePx = with(density) { 9.5.sp.toPx() }
    val strokeWidthPx = with(density) { 2.dp.toPx() }
    val outerDotPx = with(density) { 6.dp.toPx() }
    val innerDotPx = with(density) { 3.5.dp.toPx() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(WalkLogTheme.colors.surface)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "주간 시간대별 활동",
                style = WalkLogTheme.typography.typography6SB,
                color = WalkLogTheme.colors.onSurface,
            )
            if (peakHour in 0..23) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(WalkLogColor.Primary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "최고 ${peakHour}시",
                        style = WalkLogTheme.typography.typography7SB,
                        color = WalkLogColor.Primary,
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
        ) {
            drawLineChart(
                hourlyAverages = hourlyAverages,
                peakHour = peakHour,
                primaryColor = primaryColor,
                gridLineColor = gridLineColor,
                labelColor = labelColor,
                labelSizePx = labelSizePx,
                strokeWidthPx = strokeWidthPx,
                outerDotPx = outerDotPx,
                innerDotPx = innerDotPx,
                density = density,
            )
        }
    }
}

private fun DrawScope.drawLineChart(
    hourlyAverages: List<Float>,
    peakHour: Int,
    primaryColor: Color,
    gridLineColor: Color,
    labelColor: Color,
    labelSizePx: Float,
    strokeWidthPx: Float,
    outerDotPx: Float,
    innerDotPx: Float,
    density: Density,
) {
    val maxValue = hourlyAverages.maxOrNull()?.takeIf { it > 0f } ?: return
    val labelAreaHeight = labelSizePx + with(density) { 10.dp.toPx() }
    val chartHeight = size.height - labelAreaHeight
    val slotWidth = size.width / (hourlyAverages.size - 1).toFloat()

    // 데이터 포인트 계산
    val points = hourlyAverages.mapIndexed { hour, value ->
        val x = hour * slotWidth
        val y = chartHeight * (1f - (value / maxValue).coerceIn(0f, 1f))
        Offset(x, y)
    }

    // 50% 기준 점선
    drawLine(
        color = gridLineColor,
        start = Offset(0f, chartHeight * 0.5f),
        end = Offset(size.width, chartHeight * 0.5f),
        strokeWidth = with(density) { 1.dp.toPx() },
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f)),
    )

    // 채우기 그라디언트 (선 아래 면적)
    val fillPath = buildSmoothPath(points).apply {
        lineTo(points.last().x, chartHeight)
        lineTo(points.first().x, chartHeight)
        close()
    }
    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.25f),
                primaryColor.copy(alpha = 0.00f),
            ),
            startY = 0f,
            endY = chartHeight,
        ),
    )

    // 라인
    drawPath(
        path = buildSmoothPath(points),
        color = primaryColor,
        style = Stroke(
            width = strokeWidthPx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )

    // 피크 포인트 마커
    if (peakHour in points.indices) {
        val peak = points[peakHour]
        drawCircle(color = primaryColor.copy(alpha = 0.22f), radius = outerDotPx, center = peak)
        drawCircle(color = primaryColor, radius = innerDotPx, center = peak)
    }

    // X축 레이블
    val labelPaint = android.graphics.Paint().apply {
        textSize = labelSizePx
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        color = labelColor.toArgb()
    }
    listOf(0, 6, 12, 18).forEach { hour ->
        drawContext.canvas.nativeCanvas.drawText(
            "${hour}시",
            hour * slotWidth,
            size.height - with(density) { 1.dp.toPx() },
            labelPaint,
        )
    }
}

// Catmull-Rom 방식으로 부드러운 곡선 Path 생성
private fun buildSmoothPath(points: List<Offset>): Path = Path().apply {
    if (points.isEmpty()) return@apply
    moveTo(points[0].x, points[0].y)
    for (i in 0 until points.size - 1) {
        val p0 = if (i > 0) points[i - 1] else points[i]
        val p1 = points[i]
        val p2 = points[i + 1]
        val p3 = if (i < points.size - 2) points[i + 2] else points[i + 1]
        val cp1x = p1.x + (p2.x - p0.x) / 6f
        val cp1y = p1.y + (p2.y - p0.y) / 6f
        val cp2x = p2.x - (p3.x - p1.x) / 6f
        val cp2y = p2.y - (p3.y - p1.y) / 6f
        cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
    }
}

// ─── Other sections ───────────────────────────────────────────────────────────

@Composable
private fun ForecastSheetHeader(title: String, recommendedTimeText: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = WalkLogTheme.typography.typography5SB,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
        Text(
            text = recommendedTimeText,
            style = WalkLogTheme.typography.typography3B,
            color = WalkLogTheme.colors.onSurface,
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(WalkLogTheme.colors.secondaryContainer)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = "오늘의 추천 시간",
                style = WalkLogTheme.typography.typography7SB,
                color = WalkLogTheme.colors.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun ForecastDescriptionCard(description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(WalkLogTheme.colors.surfaceVariant)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "왜 이 시간대를 추천하나요?",
            style = WalkLogTheme.typography.typography6SB,
            color = WalkLogTheme.colors.onSurface,
        )
        Text(
            text = description,
            style = WalkLogTheme.typography.typography6M,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ForecastPatternCard(
    averageStepsAtThisTime: String,
    activeDays: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(WalkLogTheme.colors.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "최근 7일 패턴",
            style = WalkLogTheme.typography.typography6SB,
            color = WalkLogTheme.colors.onSurface,
        )
        ForecastInfoRow("이 시간 평균 걸음 수", averageStepsAtThisTime)
        ForecastInfoRow("활동이 있었던 날", activeDays)
    }
}

@Composable
private fun ForecastInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = WalkLogTheme.typography.typography6M,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
        Text(
            text = value,
            style = WalkLogTheme.typography.typography6SB,
            color = WalkLogTheme.colors.onSurface,
        )
    }
}
