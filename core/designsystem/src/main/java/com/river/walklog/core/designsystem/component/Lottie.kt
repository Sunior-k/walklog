package com.river.walklog.core.designsystem.component

import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.river.walklog.core.designsystem.component.preview.BasePreview
import com.river.walklog.core.designsystem.component.preview.walklogPreview
import com.river.walklog.core.designsystem.foundation.WalkLogColor

/**
 *
 * @param resId
 * @param iterations 반복 횟수. [LottieConstants.IterateForever] 로 무한 반복 가능
 * @param speed      재생 속도 (1f = 기본, 2f = 2배속, 음수 = 역재생)
 * @param isPlaying  true 면 재생, false 면 일시정지
 */
@Composable
fun WalkLogLottie(
    @RawRes resId: Int,
    modifier: Modifier = Modifier,
    iterations: Int = LottieConstants.IterateForever,
    speed: Float = 1f,
    isPlaying: Boolean = true,
) {
    if (LocalInspectionMode.current) {
        Box(modifier = modifier.background(WalkLogColor.Gray100))
        return
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        speed = speed,
        isPlaying = isPlaying,
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier,
    )
}

@walklogPreview
@Preview
@Composable
private fun WalkLogLottiePreview() {
    BasePreview {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(WalkLogColor.Gray100),
        )
    }
}
