package com.river.walklog.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * 눌렀을 때 살짝 눌리는 느낌을 주는 프리미엄 전용 눌림 애니메이션.
 * [interactionSource]는 같은 컴포넌트의 `clickable(interactionSource = ...)`와 공유해야
 * 눌림 상태가 하나의 소스로 일관되게 반영된다.
 */
@Composable
fun Modifier.premiumPressScale(
    enabled: Boolean,
    interactionSource: InteractionSource,
): Modifier {
    if (!enabled) return this
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, label = "premiumPressScale")
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
