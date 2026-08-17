package com.river.walklog.feature.reward

import androidx.compose.runtime.Immutable
import com.river.walklog.core.ui.UiText

@Immutable
data class PointsHistoryState(
    val totalNet: Int = 0,
    val groupedEntries: List<PointsHistoryDateGroup> = emptyList(),
)

@Immutable
data class PointsHistoryDateGroup(
    val dateLabel: UiText,
    val entries: List<PointsHistoryEntryUi>,
)

@Immutable
data class PointsHistoryEntryUi(
    val id: Long,
    val deltaPoints: Int,
    val reasonText: UiText,
    val createdAtEpochMillis: Long,
)
