package com.river.walklog.core.designsystem.component.preview

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.river.walklog.core.designsystem.foundation.WalkLogTheme

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@PreviewScreenSizes
annotation class walklogPreview

@Composable
fun BasePreview(content: @Composable () -> Unit = {}) {
    WalkLogTheme {
        Surface(color = WalkLogTheme.colors.onPrimary) {
            content()
        }
    }
}
