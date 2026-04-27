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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.river.walklog.core.designsystem.component.WalkLogLinearProgressBar
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.feature.report.component.WeeklyReportError
import com.river.walklog.feature.report.component.WeeklyReportTopBar
import com.river.walklog.feature.report.model.WeeklyReportArchiveItemUiModel

@Composable
fun WeeklyReportArchiveRoute(
    onBack: () -> Unit,
    onClickReport: (Long) -> Unit,
    viewModel: WeeklyReportArchiveViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler {
        viewModel.handleIntent(WeeklyReportArchiveIntent.OnClickBack)
        onBack()
    }

    WeeklyReportArchiveScreen(
        state = state,
        onClickBack = {
            viewModel.handleIntent(WeeklyReportArchiveIntent.OnClickBack)
            onBack()
        },
        onClickReport = { weekStartEpochDay ->
            viewModel.handleIntent(WeeklyReportArchiveIntent.OnClickReport(weekStartEpochDay))
            onClickReport(weekStartEpochDay)
        },
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
    items: List<WeeklyReportArchiveItemUiModel>,
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
                text = "주간 리포트",
                style = WalkLogTheme.typography.subTypography2B,
                color = WalkLogTheme.colors.onSurface,
            )
            Text(
                text = "최근 12주 기록을 모아봤어요",
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
                text = "지난 리포트",
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
    item: WeeklyReportArchiveItemUiModel,
    isFeatured: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(if (isFeatured) 28.dp else 22.dp)
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
                            text = if (item.isLocked) "진행 중인 주" else "완료된 리포트",
                            style = WalkLogTheme.typography.subTypography12R,
                            color = WalkLogTheme.colors.primary,
                        )
                        Text(
                            text = item.weekRangeText,
                            style = if (isFeatured) {
                                WalkLogTheme.typography.typography3B
                            } else {
                                WalkLogTheme.typography.typography5SB
                            },
                            color = WalkLogTheme.colors.onSurface,
                        )
                        Text(
                            text = item.dateRangeText,
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
                        label = "총 걸음",
                        value = item.totalStepsText,
                        modifier = Modifier.weight(1f),
                    )
                    ArchiveMetric(
                        label = "달성률",
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
                            text = "이번 주 리포트는 아직 준비 중이에요",
                            style = WalkLogTheme.typography.typography5SB,
                            color = WalkLogTheme.colors.onSurface,
                        )
                        Text(
                            text = item.unlockMessage,
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
                    WeeklyReportArchiveItemUiModel(
                        weekStartEpochDay = 0L,
                        weekRangeText = "3월 4일 - 3월 10일",
                        dateRangeText = "2024.03.04 - 2024.03.10",
                        totalStepsText = "42,069",
                        achievementRateText = "84%",
                        achievementRate = 0.84f,
                        isLocked = false,
                        unlockMessage = "",
                    ),
                    WeeklyReportArchiveItemUiModel(
                        weekStartEpochDay = 0L,
                        weekRangeText = "3월 11일 - 3월 17일",
                        dateRangeText = "2024.03.11 - 2024.03.17",
                        totalStepsText = "37,502",
                        achievementRateText = "75%",
                        achievementRate = 0.75f,
                        isLocked = true,
                        unlockMessage = "3월 18일에 공개돼요",
                    ),
                ),
            ),
            onClickBack = {},
            onClickReport = {},
        )
    }
}
