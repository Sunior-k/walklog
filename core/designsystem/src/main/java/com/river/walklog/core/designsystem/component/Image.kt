package com.river.walklog.core.designsystem.component

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.river.walklog.core.designsystem.R

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun walklogImage(
    model: Any?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    sharedTransitionKey: String? = null,
    animatedContentScope: AnimatedContentScope? = null,
) {
    val imageModifier = if (
        sharedTransitionScope != null &&
        sharedTransitionKey != null &&
        animatedContentScope != null
    ) {
        with(sharedTransitionScope) {
            modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key = sharedTransitionKey),
                animatedVisibilityScope = animatedContentScope,
                boundsTransform = { _, _ ->
                    tween(durationMillis = 300)
                },
            )
        }
    } else {
        modifier
    }

    AsyncImage(
        model = model,
        contentScale = contentScale,
        error = if (LocalInspectionMode.current) painterResource(R.drawable.ic_default) else null,
        contentDescription = null,
        modifier = imageModifier,
    )
}
