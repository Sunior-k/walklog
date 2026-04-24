package com.river.walklog.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * 범용 커스텀 슬라이더 컴포넌트
 * @param value 현재 값
 * @param onValueChange 값 변경 시 호출되는 콜백
 * @param modifier 수정자
 * @param minValue 최소값
 * @param maxValue 최대값
 * @param step 단계 (0이면 연속값, 양수면 이산값)
 * @param enabled 활성화 여부
 * @param trackHeight 트랙 높이
 * @param thumbSize 썸 크기
 * @param thumbColor 썸 색상
 * @param inactiveBarColor 비활성 바 색상
 * @param activeBarColor 활성 바 색상
 */
@Composable
fun CustomSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    minValue: Float = 0f,
    maxValue: Float = 10f,
    step: Float = 0f,
    enabled: Boolean = true,
    trackHeight: Dp = 8.dp,
    thumbSize: Dp = 26.dp,
    thumbColor: Color = WalkLogTheme.colors.primary,
    inactiveBarColor: Color = WalkLogTheme.colors.outline,
    activeBarColor: Color = WalkLogTheme.colors.primary,
) {
    val thumbRadius = (thumbSize / 2).toPx()
    val trackHeightPx = trackHeight.toPx()
    var sliderWidth by remember { mutableFloatStateOf(0f) }
    val startOffset = thumbRadius + 1.dp.toPx()

    val touchAreaSize = maxOf(thumbSize, 48.dp)
    val touchRadius = (touchAreaSize / 2).toPx()

    val normalizedValue = if (maxValue > minValue) {
        (value - minValue) / (maxValue - minValue)
    } else {
        0f
    }

    var thumbX by remember(normalizedValue, startOffset, sliderWidth) {
        mutableFloatStateOf(
            if (sliderWidth > 0) {
                startOffset + normalizedValue * (sliderWidth - startOffset * 2)
            } else {
                startOffset
            },
        )
    }

    var isDragging by remember { mutableStateOf(false) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(touchAreaSize)
            .clipToBounds()
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = { offset ->
                            isDragging = isPointInThumb(
                                offset.x,
                                offset.y,
                                thumbX,
                                size.height / 2f,
                                touchRadius,
                            )
                        },
                        onTap = { offset ->
                            thumbX = offset.x.coerceIn(startOffset, sliderWidth - startOffset)
                            val newNormalizedValue =
                                if (sliderWidth > startOffset * 2) {
                                    ((thumbX - startOffset) / (sliderWidth - 2 * startOffset))
                                        .coerceIn(0f, 1f)
                                } else {
                                    0f
                                }

                            val newValue = minValue + newNormalizedValue * (maxValue - minValue)
                            val finalValue = if (step > 0) {
                                (newValue / step).roundToInt() * step
                            } else {
                                newValue
                            }.coerceIn(minValue, maxValue)

                            onValueChange(finalValue)
                        },
                    )
                }
            }
            .pointerInput(enabled) {
                if (enabled) {
                    detectHorizontalDragGestures(
                        onDragEnd = { isDragging = false },
                    ) { _, dragAmount ->
                        if (isDragging) {
                            thumbX += dragAmount
                            thumbX = thumbX.coerceIn(startOffset, sliderWidth - startOffset)
                            val newNormalizedValue =
                                if (sliderWidth > startOffset * 2) {
                                    ((thumbX - startOffset) / (sliderWidth - 2 * startOffset))
                                        .coerceIn(0f, 1f)
                                } else {
                                    0f
                                }

                            val newValue = minValue + newNormalizedValue * (maxValue - minValue)
                            val finalValue = if (step > 0) {
                                (newValue / step).roundToInt() * step
                            } else {
                                newValue
                            }.coerceIn(minValue, maxValue)

                            onValueChange(finalValue)
                        }
                    }
                }
            },
    ) {
        sliderWidth = size.width
        val canvasHeight = size.height

        val normalizedThumbX = thumbX.coerceIn(startOffset, sliderWidth - startOffset)
        val activeWidth = (normalizedThumbX - startOffset).coerceAtLeast(0f)

        // 활성 바 (선택된 부분)
        drawRoundRect(
            color = activeBarColor,
            size = Size(activeWidth + 3.dp.toPx(), trackHeightPx),
            topLeft = Offset(0f, (canvasHeight - trackHeightPx) / 2),
            cornerRadius = CornerRadius(100.dp.toPx(), 100.dp.toPx()),
        )

        // 비활성 바 (선택되지 않은 부분)
        val inactiveWidth = size.width - activeWidth - 5.dp.toPx()
        if (inactiveWidth > 0) {
            drawRoundRect(
                color = inactiveBarColor,
                size = Size(inactiveWidth, trackHeightPx),
                topLeft = Offset(activeWidth + 2.dp.toPx(), (canvasHeight - trackHeightPx) / 2),
                cornerRadius = CornerRadius(100.dp.toPx(), 100.dp.toPx()),
            )
        }

        // 썸 (드래그 핸들)
        drawCircle(
            color = thumbColor,
            radius = thumbRadius,
            center = Offset(normalizedThumbX, canvasHeight / 2),
        )
    }
}

private fun isPointInThumb(
    x: Float,
    y: Float,
    thumbCenterX: Float,
    thumbCenterY: Float,
    thumbRadius: Float,
): Boolean {
    val dx = x - thumbCenterX
    val dy = y - thumbCenterY
    return sqrt(dx.pow(2) + dy.pow(2)) <= thumbRadius
}

@Composable
fun Dp.toPx(): Float {
    return with(LocalDensity.current) { this@toPx.toPx() }
}
