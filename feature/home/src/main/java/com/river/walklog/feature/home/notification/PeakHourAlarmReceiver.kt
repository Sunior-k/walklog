package com.river.walklog.feature.home.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class PeakHourAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(manager)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.river.walklog.core.designsystem.R.drawable.ic_default)
            .setContentTitle("걷기 좋은 시간이에요!")
            .setContentText("평소 이 시간대에 가장 많이 걸으셨어요. 지금 걸어보세요!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel(manager: NotificationManager) {
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "걷기 알림", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "평소 걷기 패턴 기반 걷기 권유 알림"
            },
        )
    }

    companion object {
        const val CHANNEL_ID = "walk_reminder"
        const val NOTIFICATION_ID = 1001
    }
}
