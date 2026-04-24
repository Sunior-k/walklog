package com.river.walklog.feature.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.data.repository.StepRepository
import com.river.walklog.core.model.DailyStepCount
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * WorkManager 기반 위젯 데이터 갱신 Worker
 *
 * 실행 흐름:
 * 1. [StepRepository] 로 오늘의 걸음 수 조회.
 * 2. [PreferencesGlanceStateDefinition] 상태(DataStore) 갱신.
 * 3. [TodayMissionWidget.update] 로 Glance UI 재구성 트리거.
 * 4. Android 15+: [AppWidgetManager.setWidgetPreview] 로 picker 미리보기 동적 갱신.
 * 5. 실패 시 Crashlytics non-fatal 로깅 후 [Result.retry].
 */
@HiltWorker
class TodayMissionWidgetWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val stepRepository: StepRepository,
    private val crashReporter: CrashReporter,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        crashReporter.log("WidgetWorker started: attempt=$runAttemptCount")
        crashReporter.setKey(CrashKeys.WORKER_RUN_ATTEMPT, runAttemptCount)

        return runCatching {
            val stepCount = stepRepository.getStepsForDay(
                LocalDate.now().toEpochDay(),
            ).first()
            updateGlanceWidget(stepCount)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                updatePickerPreview(stepCount)
            }
        }.fold(
            onSuccess = {
                crashReporter.log("WidgetWorker completed: steps updated")
                Result.success()
            },
            onFailure = { throwable ->
                // 위젯 갱신 실패는 앱 크래시가 아니므로 non-fatal 로 기록한다.
                crashReporter.log("WidgetWorker failed: ${throwable.message}")
                crashReporter.recordException(throwable)
                Result.retry()
            },
        )
    }

    /** Glance DataStore 상태 + UI 갱신 */
    private suspend fun updateGlanceWidget(stepCount: DailyStepCount) {
        val glanceIds = GlanceAppWidgetManager(context)
            .getGlanceIds(TodayMissionWidget::class.java)

        glanceIds.forEach { id ->
            updateAppWidgetState(
                context = context,
                definition = PreferencesGlanceStateDefinition,
                glanceId = id,
            ) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[TodayMissionWidget.CURRENT_STEPS_KEY] = stepCount.steps
                    this[TodayMissionWidget.TARGET_STEPS_KEY] = stepCount.targetSteps
                    this[TodayMissionWidget.IS_LOADING_KEY] = false
                }
            }
            TodayMissionWidget().update(context, id)
        }
    }

    /**
     * Android 15+ 위젯 picker 미리보기를 실제 걸음 수로 동적 업데이트.
     * 시간당 약 2회 호출 제한 — Worker 주기(15분)와 잘 맞는다.
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun updatePickerPreview(stepCount: DailyStepCount) {
        val remaining = (stepCount.targetSteps - stepCount.steps).coerceAtLeast(0)
        val remainingText = if (stepCount.isAchieved) {
            "오늘 목표 달성!"
        } else {
            "%,d보 남았어요".format(remaining)
        }

        val views = RemoteViews(context.packageName, R.layout.widget_preview).apply {
            setTextViewText(R.id.tv_preview_steps, "%,d보".format(stepCount.steps))
            setTextViewText(R.id.tv_preview_remaining, remainingText)
        }

        AppWidgetManager.getInstance(context).setWidgetPreview(
            ComponentName(context, TodayMissionWidgetReceiver::class.java),
            AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN,
            views,
        )
    }

    companion object {
        const val WORK_NAME = "today_mission_widget_periodic_update"
    }
}
