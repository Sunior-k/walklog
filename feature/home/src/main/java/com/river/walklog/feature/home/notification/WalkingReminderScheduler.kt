package com.river.walklog.feature.home.notification

interface WalkingReminderScheduler {
    fun schedule(peakHour: Int)
    fun cancel()
}
