package com.river.walklog.feature.reward

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetPointsHistoryUseCase
import com.river.walklog.core.model.MissionType
import com.river.walklog.core.model.PointsLedgerEntry
import com.river.walklog.core.model.RewardCatalogIds
import com.river.walklog.core.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PointsHistoryViewModel @Inject constructor(
    private val getPointsHistoryUseCase: GetPointsHistoryUseCase,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(PointsHistoryState())
    val state: StateFlow<PointsHistoryState> = _state.asStateFlow()

    init {
        crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.POINTS_HISTORY)
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            getPointsHistoryUseCase()
                .catch { e ->
                    crashReporter.log("pointsHistoryViewModel: $e")
                    crashReporter.recordException(e)
                }
                .collect { entries ->
                    val entriesUi = entries.map { entry -> entry.toUi() }
                    _state.update {
                        it.copy(
                            totalNet = entriesUi.sumOf { entry -> entry.deltaPoints },
                            groupedEntries = groupByDate(entriesUi),
                        )
                    }
                }
        }
    }

    private fun groupByDate(entries: List<PointsHistoryEntryUi>): List<PointsHistoryDateGroup> =
        entries
            .groupBy { dateKeyFor(it.createdAtEpochMillis) }
            .map { (dateKey, groupEntries) -> PointsHistoryDateGroup(dateLabelFor(dateKey), groupEntries) }

    private fun dateKeyFor(epochMillis: Long): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()

    private fun dateLabelFor(date: LocalDate): UiText {
        val today = LocalDate.now()
        return when (date) {
            today -> UiText.StringRes(R.string.points_history_today)
            today.minusDays(1) -> UiText.StringRes(R.string.points_history_yesterday)
            else -> UiText.DynamicString(DateTimeFormatter.ofPattern("M.d (E)", Locale.getDefault()).format(date))
        }
    }

    private fun PointsLedgerEntry.toUi(): PointsHistoryEntryUi = PointsHistoryEntryUi(
        id = id,
        deltaPoints = deltaPoints,
        reasonText = reasonToUiText(reason),
        createdAtEpochMillis = createdAtEpochMillis,
    )

    private fun reasonToUiText(reason: String): UiText = when (reason) {
        MissionType.DAILY.name -> UiText.StringRes(R.string.points_history_reason_daily)
        MissionType.RECOVERY.name -> UiText.StringRes(R.string.points_history_reason_recovery)
        "REDEEM_${RewardCatalogIds.COFFEE_COUPON}" -> UiText.StringRes(R.string.points_history_reason_redeem_coffee_coupon)
        "REDEEM_${RewardCatalogIds.BADGE_GOLD}" -> UiText.StringRes(R.string.points_history_reason_redeem_badge_gold)
        "REDEEM_${RewardCatalogIds.DONATION}" -> UiText.StringRes(R.string.points_history_reason_redeem_donation)
        "REDEEM_${RewardCatalogIds.THEME_PACK}" -> UiText.StringRes(R.string.points_history_reason_redeem_theme_pack)
        else -> UiText.StringRes(R.string.points_history_reason_unknown)
    }
}
