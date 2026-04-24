package com.river.walklog.feature.report.extension

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ReportShareManager(private val context: Context) {

    suspend fun saveBitmapToCache(
        bitmap: Bitmap,
        fileName: String = "weekly_report_${System.currentTimeMillis()}.png",
    ): Uri = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "shared_images").apply {
            if (!exists()) mkdirs()
        }
        val file = File(directory, fileName)
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            output.flush()
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun shareImage(
        imageUri: Uri,
        chooserTitle: String = "주간 리포트 공유",
        shareText: String = "이번 주 나의 걷기 리포트를 확인해보세요!",
    ) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
