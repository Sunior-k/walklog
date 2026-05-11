package com.river.walklog.feature.home.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.river.walklog.feature.home.R

class PeakHourAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(manager, context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.river.walklog.core.designsystem.R.drawable.ic_footprint)
            .setContentTitle(context.getString(R.string.notification_walking_title))
            .setContentText(context.getString(R.string.notification_walking_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel(manager: NotificationManager, context: Context) {
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
            },
        )
    }

    companion object {
        const val CHANNEL_ID = "walk_reminder"
        const val NOTIFICATION_ID = 1001
    }
}
