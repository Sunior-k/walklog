package com.river.walklog.feature.home.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmManagerWalkingReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : WalkingReminderScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(peakHour: Int) {
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, peakHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        alarmManager.setWindow(
            AlarmManager.RTC_WAKEUP,
            trigger.timeInMillis,
            WINDOW_MS,
            buildPendingIntent(),
        )
    }

    override fun cancel() {
        alarmManager.cancel(buildPendingIntent())
    }

    private fun buildPendingIntent(): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, PeakHourAlarmReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {
        private const val REQUEST_CODE = 2001
        private const val WINDOW_MS = 30 * 60 * 1000L
    }
}
