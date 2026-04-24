package com.river.walklog.feature.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * 오늘 미션 위젯 BroadcastReceiver
 *
 * 시스템이 APPWIDGET_UPDATE 브로드캐스트를 전달하면 Glance가 받아 처리
 *
 * 위젯 생명주기:
 * - [onEnabled]: 첫 번째 인스턴스 추가 시 WorkManager 주기 작업을 등록하고 즉시 1회 갱신.
 * - [onUpdate]: 시스템 재시작 등으로 onUpdate가 호출될 때 즉시 1회 갱신.
 * - [onDeleted]: 인스턴스 제거 시 별도 처리 불필요 (Glance가 상태 정리).
 * - [onDisabled]: 마지막 인스턴스 제거 시 WorkManager 작업을 취소.
 */
class TodayMissionWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = TodayMissionWidget()

    /** 홈화면에 첫 위젯이 추가될 때: 주기 업데이트 등록 + 즉시 1회 갱신. */
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        schedulePeriodicUpdate(context)
        enqueueImmediateUpdate(context)
    }

    /** 시스템 재시작 / 기간 만료로 onUpdate가 호출될 때: 즉시 1회 갱신. */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        enqueueImmediateUpdate(context)
    }

    /** 마지막 위젯 인스턴스가 제거될 때: WorkManager 주기 작업 취소. */
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WorkManager.getInstance(context)
            .cancelUniqueWork(TodayMissionWidgetWorker.WORK_NAME)
    }

    // ─── Private ─────────────────────────────────────────────────────────────

    /**
     * WorkManager 15분 주기 작업 등록.
     * [ExistingPeriodicWorkPolicy.KEEP]: 이미 등록된 작업이 있으면 유지 (중복 방지).
     */
    private fun schedulePeriodicUpdate(context: Context) {
        val request = PeriodicWorkRequestBuilder<TodayMissionWidgetWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TodayMissionWidgetWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    /** 지금 즉시 1회 갱신 (OneTimeWorkRequest). */
    private fun enqueueImmediateUpdate(context: Context) {
        val request = androidx.work.OneTimeWorkRequestBuilder<TodayMissionWidgetWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
