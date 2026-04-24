package com.river.walklog.feature.report.extension

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer

suspend fun GraphicsLayer.toAndroidBitmapSafely(): Bitmap {
    val imageBitmap: ImageBitmap = toImageBitmap()
    return imageBitmap.asAndroidBitmap()
}
