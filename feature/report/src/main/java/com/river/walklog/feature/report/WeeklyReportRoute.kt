package com.river.walklog.feature.report

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.river.walklog.core.designsystem.R
import com.river.walklog.core.designsystem.component.WalkLogLinearProgressBar
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.feature.report.component.ShareableWeeklyReportCard
import com.river.walklog.feature.report.extension.ReportShareManager
import com.river.walklog.feature.report.extension.toAndroidBitmapSafely
import com.river.walklog.feature.report.model.WeeklyReportShareCardUiModel
import kotlinx.coroutines.launch

@Composable
fun WeeklyReportRoute(
    onBack: () -> Unit,
    viewModel: WeeklyReportViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shareManager = remember(context) { ReportShareManager(context) }
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()

    WeeklyReportScreen(
        state = state,
        graphicsLayer = graphicsLayer,
        onClickBack = {
            viewModel.handleIntent(WeeklyReportIntent.OnClickBack)
            onBack()
        },
        onClickShare = {
            scope.launch {
                viewModel.setSharing(true)
                runCatching {
                    val bitmap = graphicsLayer.toAndroidBitmapSafely()
                    val uri = shareManager.saveBitmapToCache(
                        bitmap = bitmap,
                        fileName = "weekly_report_${System.currentTimeMillis()}.png",
                    )
                    shareManager.shareImage(uri)
                    viewModel.handleIntent(WeeklyReportIntent.OnClickShare)
                }.onFailure {
                    Toast.makeText(context, "리포트 공유에 실패했어요.", Toast.LENGTH_SHORT).show()
                }
                viewModel.setSharing(false)
            }
        },
    )
}

@Composable
internal fun WeeklyReportScreen(
    state: WeeklyReportState,
    graphicsLayer: GraphicsLayer,
    onClickBack: () -> Unit,
    onClickShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSharePreview by rememberSaveable { mutableStateOf(false) }

    androidx.compose.material3.Scaffold(
        modifier = modifier,
        topBar = { WeeklyReportTopBar(onClickBack = onClickBack) },
        bottomBar = {
            if (!state.isEmpty && !state.isError && !state.isLoading) {
                WeeklyReportBottomBar(isSharing = state.isSharing, onClickShare = onClickShare)
            }
        },
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
                    WeeklyReportErrorState(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(padding),
                    )
                }

                state.isEmpty -> {
                    WeeklyReportEmptyState(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(padding),
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        WeeklyReportHeader(
                            dateRangeSubtitle = state.dateRangeSubtitle,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        WeeklyBarChartCard(dailyCounts = state.dailyCounts)

                        WeeklyTotalStepsCard(totalStepsText = state.totalStepsText)

                        WeeklyGoalCard(
                            achievedDays = state.achievedDays,
                            totalDays = state.totalDays,
                            achievementRate = state.achievementRate,
                        )

                        SharePreviewHeader(
                            isExpanded = showSharePreview,
                            onClickToggle = { showSharePreview = !showSharePreview },
                        )

                        AnimatedVisibility(visible = showSharePreview) {
                            ShareableWeeklyReportCard(
                                model = WeeklyReportShareCardUiModel(
                                    weekRangeText = state.weekRangeText,
                                    headline = state.summaryMessage,
                                    totalStepsText = state.totalStepsText,
                                    achievementRateText = state.achievementRateText,
                                    bestDayText = state.bestDayText,
                                    bestTimeText = state.bestTimeText,
                                    streakText = state.bestStreakText,
                                ),
                                graphicsLayer = graphicsLayer,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            if (state.isSharing) SharingOverlay()
        }
    }
}

@Composable
private fun SharePreviewHeader(
    isExpanded: Boolean,
    onClickToggle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "공유 카드 미리보기",
            style = WalkLogTheme.typography.typography7M,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
        TextButton(
            onClick = onClickToggle,
            contentPadding = PaddingValues(horizontal = 8.dp),
        ) {
            Text(
                text = if (isExpanded) "접기" else "보기",
                style = WalkLogTheme.typography.typography7SB,
                color = WalkLogTheme.colors.primary,
            )
        }
    }
}

// ─── Top / Bottom bars ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeeklyReportTopBar(onClickBack: () -> Unit) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back),
                    contentDescription = "뒤로가기",
                    tint = WalkLogTheme.colors.onSurface,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = WalkLogTheme.colors.background),
    )
}

@Composable
private fun WeeklyReportBottomBar(isSharing: Boolean, onClickShare: () -> Unit) {
    Surface(shadowElevation = 8.dp, color = WalkLogTheme.colors.background) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Button(
                onClick = onClickShare,
                enabled = !isSharing,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WalkLogTheme.colors.primary,
                    contentColor = WalkLogTheme.colors.onPrimary,
                    disabledContainerColor = WalkLogTheme.colors.outlineVariant,
                    disabledContentColor = WalkLogTheme.colors.onSurfaceVariant,
                ),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                if (isSharing) {
                    CircularProgressIndicator(
                        Modifier.size(18.dp),
                        WalkLogTheme.colors.onPrimary,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.size(8.dp))
                }
                Text(
                    text = if (isSharing) "공유 준비 중..." else "리포트 공유하기",
                    style = WalkLogTheme.typography.typography6SB,
                )
            }
        }
    }
}

// ─── Content composables ──────────────────────────────────────────────────────

@Composable
private fun WeeklyReportHeader(dateRangeSubtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "주간 리포트",
            style = WalkLogTheme.typography.subTypography2B,
            color = WalkLogTheme.colors.onSurface,
        )
        Text(
            text = dateRangeSubtitle,
            style = WalkLogTheme.typography.typography7M,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun WeeklyBarChartCard(dailyCounts: List<DailyStepCount>) {
    val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")
    val maxSteps = dailyCounts.maxOfOrNull { it.steps }?.coerceAtLeast(1) ?: 1
    val bestEpochDay = dailyCounts.maxByOrNull { it.steps }?.takeIf { it.steps > 0 }?.dateEpochDay
    val maxBarHeight = 160.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WalkLogTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            dailyCounts.forEachIndexed { index, dayCount ->
                val isBest = dayCount.dateEpochDay == bestEpochDay
                val fraction = dayCount.steps.toFloat() / maxSteps

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier.height(maxBarHeight),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .fillMaxHeight(fraction.coerceAtLeast(if (dayCount.steps > 0) 0.06f else 0.02f))
                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                .background(
                                    if (isBest) {
                                        WalkLogTheme.colors.primary
                                    } else {
                                        WalkLogTheme.colors.outlineVariant
                                    },
                                ),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = dayLabels.getOrElse(index) { "" },
                        style = if (isBest) {
                            WalkLogTheme.typography.subTypography13B
                        } else {
                            WalkLogTheme.typography.subTypography13M
                        },
                        color = if (isBest) WalkLogTheme.colors.primary else WalkLogTheme.colors.onSurfaceVariant,
                    )
                    Text(
                        text = formatStepsShort(dayCount.steps),
                        style = if (isBest) {
                            WalkLogTheme.typography.subTypography13B
                        } else {
                            WalkLogTheme.typography.subTypography13M
                        },
                        color = if (isBest) WalkLogTheme.colors.primary else WalkLogTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyTotalStepsCard(totalStepsText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WalkLogTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "이번 주 총 걸음",
                    style = WalkLogTheme.typography.typography7M,
                    color = WalkLogTheme.colors.onSurfaceVariant,
                )
                Text(
                    text = totalStepsText,
                    style = WalkLogTheme.typography.subTypography5B,
                    color = WalkLogTheme.colors.onSurface,
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(WalkLogTheme.colors.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(com.river.walklog.feature.report.R.drawable.ic_footprint),
                    contentDescription = null,
                    tint = WalkLogTheme.colors.primary,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}

@Composable
private fun WeeklyGoalCard(
    achievedDays: Int,
    totalDays: Int,
    achievementRate: Float,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WalkLogTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "목표 달성",
                    style = WalkLogTheme.typography.typography7M,
                    color = WalkLogTheme.colors.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(WalkLogTheme.colors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "★",
                        style = WalkLogTheme.typography.subTypography11B,
                        color = WalkLogTheme.colors.onPrimary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$achievedDays/$totalDays",
                    style = WalkLogTheme.typography.typography1B,
                    color = WalkLogTheme.colors.onSurface,
                )
                Text(
                    text = " 일",
                    style = WalkLogTheme.typography.subTypography9M,
                    color = WalkLogTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            WalkLogLinearProgressBar(
                progress = achievementRate,
                color = WalkLogTheme.colors.primary,
                trackColor = WalkLogTheme.colors.surfaceVariant,
                height = 8.dp,
            )
        }
    }
}

// ─── Overlays / States ────────────────────────────────────────────────────────

@Composable
private fun SharingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WalkLogTheme.colors.scrim.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            color = WalkLogTheme.colors.surface,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 4.dp,
            shadowElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(
                    color = WalkLogTheme.colors.primary,
                    trackColor = WalkLogTheme.colors.onSurface.copy(alpha = 0.18f),
                )
                Text(
                    text = "공유 이미지를 만드는 중이에요",
                    style = WalkLogTheme.typography.typography6SB,
                    color = WalkLogTheme.colors.onSurface,
                )
            }
        }
    }
}

@Composable
private fun WeeklyReportEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "📭", style = WalkLogTheme.typography.typography1B)
        Text(
            text = "이번 주 걸음 기록이 없어요",
            style = WalkLogTheme.typography.typography4SB,
            color = WalkLogTheme.colors.onSurface,
        )
        Text(
            text = "걷기 시작하면 주간 리포트가 만들어져요",
            style = WalkLogTheme.typography.typography6M,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun WeeklyReportErrorState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "⚠️", style = WalkLogTheme.typography.typography1B)
        Text(
            text = "데이터를 불러오지 못했어요",
            style = WalkLogTheme.typography.typography4SB,
            color = WalkLogTheme.colors.onSurface,
        )
        Text(
            text = "잠시 후 다시 시도해 주세요",
            style = WalkLogTheme.typography.typography6M,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun formatStepsShort(steps: Int): String = when {
    steps <= 0 -> ""
    steps >= 1_000 -> "${steps / 1_000}k"
    else -> "$steps"
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun WeeklyReportScreenPreview() {
    WalkLogTheme {
        val fakeCounts = listOf(4200, 8100, 12300, 7400, 9000, 5100, 6200).mapIndexed { i, steps ->
            DailyStepCount(dateEpochDay = 19_823L + i, steps = steps)
        }
        WeeklyReportScreen(
            state = WeeklyReportState(
                dateRangeSubtitle = "4월 14일 — 4월 20일",
                weekRangeText = "4월 3주차 · 4/14~4/20",
                totalStepsText = "52,300보",
                achievementRateText = "86%",
                achievedDays = 6,
                totalDays = 7,
                achievementRate = 6f / 7f,
                bestDayText = "수요일",
                bestTimeText = "오후 3시",
                bestStreakText = "5일",
                summaryMessage = "훌륭해요! 목표에 가까워지고 있어요",
                dailyCounts = fakeCounts,
                isLoading = false,
            ),
            graphicsLayer = rememberGraphicsLayer(),
            onClickBack = {},
            onClickShare = {},
        )
    }
}
