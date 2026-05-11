package com.river.walklog.feature.report

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.river.walklog.core.designsystem.component.WalkLogLinearProgressBar
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.core.ui.withComma
import com.river.walklog.feature.report.component.WeeklyReportError
import com.river.walklog.feature.report.component.WeeklyReportTopBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WeeklyReportArchiveRoute(
    onBack: () -> Unit,
    onClickReport: (Long) -> Unit,
    viewModel: WeeklyReportArchiveViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler { onBack() }

    WeeklyReportArchiveScreen(
        state = state,
        onClickBack = onBack,
        onClickReport = onClickReport,
    )
}

@Composable
internal fun WeeklyReportArchiveScreen(
    state: WeeklyReportArchiveState,
    onClickBack: () -> Unit,
    onClickReport: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { WeeklyReportTopBar(onClickBack = onClickBack) },
        containerColor = WalkLogTheme.colors.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WalkLogTheme.colors.background),
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = WalkLogTheme.colors.primary,
                    )
                }

                state.isError -> {
                    WeeklyReportError(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(padding),
                    )
                }

                else -> {
                    WeeklyReportArchiveContent(
                        items = state.archiveItems,
                        modifier = Modifier.padding(padding),
                        onClickReport = onClickReport,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyReportArchiveContent(
    items: List<WeeklyReportArchiveItemUiState>,
    onClickReport: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val unlockedItems = items.filterNot { it.isLocked }
    val lockedItem = items.firstOrNull { it.isLocked }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.report_title),
                style = WalkLogTheme.typography.subTypography2B,
                color = WalkLogTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.report_archive_subtitle),
                style = WalkLogTheme.typography.typography7M,
                color = WalkLogTheme.colors.onSurfaceVariant,
            )
        }

        lockedItem?.let { item ->
            WeeklyReportArchiveCard(
                item = item,
                isFeatured = true,
                onClick = { onClickReport(item.weekStartEpochDay) },
            )
        }

        if (unlockedItems.isNotEmpty()) {
            Text(
                text = stringResource(R.string.report_past_title),
                style = WalkLogTheme.typography.typography7SB,
                color = WalkLogTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        unlockedItems.forEachIndexed { index, item ->
            WeeklyReportArchiveCard(
                item = item,
                isFeatured = index == 0,
                onClick = { onClickReport(item.weekStartEpochDay) },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun WeeklyReportArchiveCard(
    item: WeeklyReportArchiveItemUiState,
    isFeatured: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(if (isFeatured) 28.dp else 22.dp)
    val locale = LocalConfiguration.current.locales[0]
    val rangeFormatter = remember(locale) { DateTimeFormatter.ofPattern("M/d", locale) }
    val monthFormatter = remember(locale) { DateTimeFormatter.ofPattern("MMMM", locale) }
    val fullDateFormatPattern = stringResource(R.string.report_full_date_format_pattern)
    val unlockDateFormatter = remember(locale, fullDateFormatPattern) {
        DateTimeFormatter.ofPattern(fullDateFormatPattern, locale)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(enabled = !item.isLocked, onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = WalkLogTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFeatured) 4.dp else 1.dp),
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = if (isFeatured) {
                        Brush.horizontalGradient(
                            listOf(
                                WalkLogTheme.colors.primaryContainer.copy(alpha = 0.55f),
                                WalkLogTheme.colors.surface,
                            ),
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(
                                WalkLogTheme.colors.surface,
                                WalkLogTheme.colors.surface,
                            ),
                        )
                    },
                )
                .border(
                    width = 1.dp,
                    color = WalkLogTheme.colors.outlineVariant.copy(alpha = if (isFeatured) 0.55f else 0.32f),
                    shape = shape,
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (item.isLocked) Modifier.blur(3.dp) else Modifier)
                    .padding(if (isFeatured) 22.dp else 18.dp),
                verticalArrangement = Arrangement.spacedBy(if (isFeatured) 18.dp else 14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (item.isLocked) stringResource(R.string.report_in_progress) else stringResource(R.string.report_completed),
                            style = WalkLogTheme.typography.subTypography12R,
                            color = WalkLogTheme.colors.primary,
                        )
                        Text(
                            text = stringResource(
                                R.string.report_week_range,
                                item.weekStart.format(monthFormatter),
                                item.weekOfMonth,
                            ),
                            style = if (isFeatured) {
                                WalkLogTheme.typography.typography3B
                            } else {
                                WalkLogTheme.typography.typography5SB
                            },
                            color = WalkLogTheme.colors.onSurface,
                        )
                        Text(
                            text = stringResource(
                                R.string.report_date_range,
                                item.weekStart.format(rangeFormatter),
                                item.weekEnd.format(rangeFormatter),
                            ),
                            style = WalkLogTheme.typography.typography7M,
                            color = WalkLogTheme.colors.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = if (item.isLocked) "LOCKED" else "OPEN",
                        style = WalkLogTheme.typography.typography7SB,
                        color = WalkLogTheme.colors.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(WalkLogTheme.colors.primary.copy(alpha = 0.10f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ArchiveMetric(
                        label = stringResource(R.string.report_total_steps_label),
                        value = stringResource(R.string.steps_with_suffix, item.totalSteps.withComma()),
                        modifier = Modifier.weight(1f),
                    )
                    ArchiveMetric(
                        label = stringResource(R.string.report_achievement_rate_label),
                        value = item.achievementRateText,
                        modifier = Modifier.weight(1f),
                    )
                }

                WalkLogLinearProgressBar(
                    progress = item.achievementRate.coerceIn(0f, 1f),
                    color = WalkLogTheme.colors.primary,
                    trackColor = WalkLogTheme.colors.surfaceVariant,
                    height = 7.dp,
                )
            }

            if (item.isLocked) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(WalkLogTheme.colors.surface.copy(alpha = 0.82f))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(text = "🔒", style = WalkLogTheme.typography.typography2B)
                        Text(
                            text = stringResource(R.string.report_locked_message),
                            style = WalkLogTheme.typography.typography5SB,
                            color = WalkLogTheme.colors.onSurface,
                        )
                        Text(
                            text = stringResource(
                                R.string.report_unlock_from,
                                item.unlockDate.format(unlockDateFormatter),
                            ),
                            style = WalkLogTheme.typography.typography7M,
                            color = WalkLogTheme.colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchiveMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(WalkLogTheme.colors.background.copy(alpha = 0.72f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = WalkLogTheme.typography.typography7M,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
        Text(
            text = value,
            style = WalkLogTheme.typography.typography5SB,
            color = WalkLogTheme.colors.onSurface,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeeklyReportArchiveScreenPreview() {
    WalkLogTheme {
        WeeklyReportArchiveScreen(
            state = WeeklyReportArchiveState(
                isLoading = false,
                isError = false,
                archiveItems = listOf(
                    WeeklyReportArchiveItemUiState(
                        weekStartEpochDay = 0L,
                        unlockDate = LocalDate.ofEpochDay(7L),
                        totalSteps = 42_069,
                        achievementPct = 84,
                        achievementRate = 0.84f,
                        isLocked = false,
                    ),
                    WeeklyReportArchiveItemUiState(
                        weekStartEpochDay = 0L,
                        unlockDate = LocalDate.ofEpochDay(7L),
                        totalSteps = 37_502,
                        achievementPct = 75,
                        achievementRate = 0.75f,
                        isLocked = true,
                    ),
                ),
            ),
            onClickBack = {},
            onClickReport = {},
        )
    }
}
