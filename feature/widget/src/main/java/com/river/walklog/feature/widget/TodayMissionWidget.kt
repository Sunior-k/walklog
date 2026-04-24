package com.river.walklog.feature.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition

/**
 * 오늘 미션 홈화면 위젯
 *
 * 상태 저장: [PreferencesGlanceStateDefinition]
 * 레이아웃 크기: [SizeMode.Exact] — [LocalSize.current] 로 정확한 dp 크기를 얻어 프로그레스 바 너비를 동적으로 계산.
 * 업데이트 주기: [TodayMissionWidgetWorker] 가 WorkManager를 통해 15분마다 실행하고 DataStore 상태를 갱신한 뒤 [update] 를 호출한다.
 */
class TodayMissionWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { Content() }
    }

    @Composable
    private fun Content() {
        val prefs = currentState<Preferences>()
        TodayMissionWidgetContent(
            currentSteps = prefs[CURRENT_STEPS_KEY] ?: 0,
            targetSteps = prefs[TARGET_STEPS_KEY] ?: DEFAULT_TARGET_STEPS,
            isLoading = prefs[IS_LOADING_KEY] ?: true,
        )
    }

    companion object {
        val CURRENT_STEPS_KEY = intPreferencesKey("widget_current_steps")
        val TARGET_STEPS_KEY = intPreferencesKey("widget_target_steps")
        val IS_LOADING_KEY = booleanPreferencesKey("widget_is_loading")
        const val DEFAULT_TARGET_STEPS = 6_000
    }
}
