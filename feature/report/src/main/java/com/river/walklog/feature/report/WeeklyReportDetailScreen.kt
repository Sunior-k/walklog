package com.river.walklog.feature.report

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.river.walklog.core.designsystem.component.WalkLogLinearProgressBar
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.ui.withComma
import com.river.walklog.feature.report.component.ShareableWeeklyReportCard
import com.river.walklog.feature.report.component.WeeklyReportError
import com.river.walklog.feature.report.component.WeeklyReportShareCard
import com.river.walklog.feature.report.component.WeeklyReportTopBar
import com.river.walklog.feature.report.extension.ReportShareManager
import com.river.walklog.feature.report.extension.toAndroidBitmapSafely
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WeeklyReportDetailRoute(
    weekStartEpochDay: Long,
    onBack: () -> Unit,
    viewModel: WeeklyReportDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shareManager = remember(context) { ReportShareManager(context) }
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val shareChooserTitle = stringResource(R.string.report_share_chooser_title)
    val shareText = stringResource(R.string.report_share_text)

    LaunchedEffect(weekStartEpochDay) {
        viewModel.loadReport(weekStartEpochDay)
    }

    val userMessage = state.userMessage
    val userMessageText = userMessage?.let { stringResource(it.stringRes) }
    LaunchedEffect(userMessageText) {
        userMessageText?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    BackHandler {
        onBack()
    }

    WeeklyReportDetailScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        graphicsLayer = graphicsLayer,
        onClickBack = onBack,
        onClickShare = {
            scope.launch {
                viewModel.startSharing()
                runCatching {
                    val bitmap = graphicsLayer.toAndroidBitmapSafely()
                    val uri = shareManager.saveBitmapToCache(
                        bitmap = bitmap,
                        fileName = "weekly_report_${System.currentTimeMillis()}.png",
                    )
                    shareManager.shareImage(
                        imageUri = uri,
                        chooserTitle = shareChooserTitle,
                        shareText = shareText,
                    )
                }.onSuccess {
                    viewModel.completeSharing()
                }.onFailure {
                    viewModel.failSharing()
                }
            }
        },
    )
}

@Composable
internal fun WeeklyReportDetailScreen(
    state: WeeklyReportDetailState,
    graphicsLayer: GraphicsLayer,
    onClickBack: () -> Unit,
    onClickShare: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { WeeklyReportTopBar(onClickBack = onClickBack) },
        containerColor = WalkLogTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WalkLogTheme.colors.background),
        ) {
            val hasContent = !state.isEmpty && !state.isError && !state.isLoading
            val locale = LocalConfiguration.current.locales[0]
            val rangeFormatter = remember(locale) { DateTimeFormatter.ofPattern("M/d", locale) }
            val monthFormatter = remember(locale) { DateTimeFormatter.ofPattern("MMMM", locale) }
            val fullDateFormatPattern = stringResource(R.string.report_full_date_format_pattern)
            val fullDateFormatter = remember(locale, fullDateFormatPattern) {
                DateTimeFormatter.ofPattern(fullDateFormatPattern, locale)
            }
            val dayOfWeekFormatter = remember(locale) { DateTimeFormatter.ofPattern("EEEE", locale) }
            val weekStart = state.weekStart
            val weekEnd = state.weekEnd
            val shareWeekRangeText = state.weekRangeText(monthFormatter, rangeFormatter)
            val shareHeadline = state.summaryMessageText()
            val shareTotalStepsText = stringResource(R.string.steps_with_suffix, state.totalSteps.withComma())
            val bestDayText = state.bestDayText(dayOfWeekFormatter)
            val shareBestTimeText = state.bestTimeText()
            val shareStreakText = state.bestStreakText()

            if (hasContent) {
                ShareableWeeklyReportCard(
                    weekRangeText = shareWeekRangeText,
                    headline = shareHeadline,
                    totalStepsText = shareTotalStepsText,
                    achievementRateText = state.achievementRateText,
                    bestDayText = bestDayText,
                    bestTimeText = shareBestTimeText,
                    streakText = shareStreakText,
                    graphicsLayer = graphicsLayer,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 20.dp)
                        .graphicsLayer { alpha = 0f }
                        .zIndex(-1f),
                )
            }

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
                            dateRangeSubtitle = if (weekStart != null && weekEnd != null) {
                                "${weekStart.format(fullDateFormatter)} — ${weekEnd.format(fullDateFormatter)}"
                            } else {
                                ""
                            },
                        )

                        WeeklyBarChartCard(dailyCounts = state.dailyCounts)

                        WeeklyTotalStepsCard(totalStepsText = shareTotalStepsText)

                        WeeklyGoalCard(
                            achievedDays = state.achievedDays,
                            totalDays = state.totalDays,
                            achievementRate = state.achievementRate,
                        )

                        WeeklyInsightCard(
                            bestDayText = bestDayText,
                            bestTimeText = shareBestTimeText,
                            bestStreakText = shareStreakText,
                        )

                        if (hasContent) {
                            WeeklyReportSharePreview(
                                weekRangeText = shareWeekRangeText,
                                headline = shareHeadline,
                                totalStepsText = shareTotalStepsText,
                                achievementRateText = state.achievementRateText,
                                bestDayText = bestDayText,
                                bestTimeText = shareBestTimeText,
                                streakText = shareStreakText,
                            )
                        }

                        Spacer(modifier = Modifier.height(96.dp))
                    }
                }
            }

            if (!state.isEmpty && !state.isError && !state.isLoading) {
                WeeklyReportBottomBar(
                    isSharing = state.isSharing,
                    onClickShare = onClickShare,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }

            if (state.isSharing) SharingOverlay()
        }
    }
}

@Composable
private fun WeeklyReportBottomBar(
    isSharing: Boolean,
    onClickShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Button(
            onClick = onClickShare,
            enabled = !isSharing,
            modifier = Modifier.fillMaxWidth(),
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
                text = if (isSharing) stringResource(R.string.report_share_button_sharing) else stringResource(R.string.report_share_button),
                style = WalkLogTheme.typography.typography6SB,
            )
        }
    }
}

@Composable
private fun WeeklyReportHeader(dateRangeSubtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.report_title),
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
private fun WeeklyReportDetailState.weekRangeText(
    monthFormatter: DateTimeFormatter,
    rangeFormatter: DateTimeFormatter,
): String {
    val start = weekStart ?: return ""
    val end = weekEnd ?: return ""
    val weekOfMonth = weekOfMonth ?: return ""
    return stringResource(
        R.string.report_week_range_full,
        start.format(monthFormatter),
        weekOfMonth,
        start.format(rangeFormatter),
        end.format(rangeFormatter),
    )
}

@Composable
private fun WeeklyReportDetailState.summaryMessageText(): String = stringResource(
    when (summaryMessageType) {
        WeeklyReportSummaryMessageType.AllAchieved -> R.string.report_summary_all_achieved
        WeeklyReportSummaryMessageType.GoodProgress -> R.string.report_summary_good_progress
        WeeklyReportSummaryMessageType.KeepGoing -> R.string.report_summary_keep_going
    },
)

private fun WeeklyReportDetailState.bestDayText(formatter: DateTimeFormatter): String =
    bestDayEpochDay?.let { LocalDate.ofEpochDay(it).format(formatter) } ?: "-"

@Composable
private fun WeeklyReportDetailState.bestTimeText(): String {
    val hour = bestHour ?: return "-"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return if (hour < 12) {
        stringResource(R.string.report_best_time_am, displayHour)
    } else {
        stringResource(R.string.report_best_time_pm, displayHour)
    }
}

@Composable
private fun WeeklyReportDetailState.bestStreakText(): String =
    if (longestAchievedStreak > 0) {
        stringResource(R.string.report_streak_days, longestAchievedStreak)
    } else {
        "-"
    }

@Composable
private fun WeeklyReportSharePreview(
    weekRangeText: String,
    headline: String,
    totalStepsText: String,
    achievementRateText: String,
    bestDayText: String,
    bestTimeText: String,
    streakText: String,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(24.dp)

    Card(
        onClick = { isExpanded = !isExpanded },
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = WalkLogTheme.colors.primaryContainer.copy(
                alpha = 0.34f,
            ),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, WalkLogTheme.colors.primary.copy(alpha = 0.26f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            WalkLogTheme.colors.primary.copy(alpha = 0.08f),
                            WalkLogTheme.colors.surface,
                        ),
                    ),
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(WalkLogTheme.colors.primary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "↗",
                                style = WalkLogTheme.typography.typography5SB,
                                color = WalkLogTheme.colors.primary,
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = stringResource(R.string.report_share_preview_title),
                                style = WalkLogTheme.typography.typography6SB,
                                color = WalkLogTheme.colors.onSurface,
                            )
                            Text(
                                text = if (isExpanded) {
                                    stringResource(R.string.report_share_preview_expanded)
                                } else {
                                    stringResource(R.string.report_share_preview_collapsed)
                                },
                                style = WalkLogTheme.typography.typography7M,
                                color = WalkLogTheme.colors.onSurfaceVariant,
                            )
                        }
                    }
                    Text(
                        text = if (isExpanded) stringResource(R.string.report_share_collapse) else stringResource(R.string.report_share_expand),
                        style = WalkLogTheme.typography.typography7M,
                        color = WalkLogTheme.colors.onPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(WalkLogTheme.colors.primary)
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }

                if (isExpanded) {
                    WeeklyReportShareCard(
                        weekRangeText = weekRangeText,
                        headline = headline,
                        totalStepsText = totalStepsText,
                        achievementRateText = achievementRateText,
                        bestDayText = bestDayText,
                        bestTimeText = bestTimeText,
                        streakText = streakText,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyBarChartCard(dailyCounts: List<DailyStepCount>) {
    val dayLabels = listOf(
        stringResource(R.string.report_day_mon),
        stringResource(R.string.report_day_tue),
        stringResource(R.string.report_day_wed),
        stringResource(R.string.report_day_thu),
        stringResource(R.string.report_day_fri),
        stringResource(R.string.report_day_sat),
        stringResource(R.string.report_day_sun),
    )
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
                    text = stringResource(R.string.report_weekly_total_steps_label),
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
                    imageVector = ImageVector.vectorResource(R.drawable.ic_footprint),
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
                    text = stringResource(R.string.report_goal_label),
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
                    text = stringResource(R.string.report_days_suffix),
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

@Composable
private fun WeeklyInsightCard(
    bestDayText: String,
    bestTimeText: String,
    bestStreakText: String,
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.report_highlight_title),
                style = WalkLogTheme.typography.typography7M,
                color = WalkLogTheme.colors.onSurfaceVariant,
            )
            InsightRow(label = stringResource(R.string.report_best_day_label), value = bestDayText)
            InsightRow(label = stringResource(R.string.report_best_time_label), value = bestTimeText)
            InsightRow(label = stringResource(R.string.report_best_streak_label), value = bestStreakText)
        }
    }
}

@Composable
private fun InsightRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = WalkLogTheme.typography.typography7M,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
        Text(
            text = value,
            style = WalkLogTheme.typography.typography6SB,
            color = WalkLogTheme.colors.onSurface,
        )
    }
}

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
                    text = stringResource(R.string.report_sharing_preparing),
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
            text = stringResource(R.string.report_empty_title),
            style = WalkLogTheme.typography.typography4SB,
            color = WalkLogTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.report_empty_desc),
            style = WalkLogTheme.typography.typography6M,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
    }
}

private fun formatStepsShort(steps: Int): String = when {
    steps <= 0 -> ""
    steps >= 1_000 -> "${steps / 1_000}k"
    else -> "$steps"
}

private val WeeklyReportUserMessage.stringRes: Int
    @StringRes get() = when (this) {
        WeeklyReportUserMessage.ShareFailed -> R.string.report_share_failed
    }

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun WeeklyReportScreenPreview() {
    WalkLogTheme {
        val fakeCounts = listOf(4200, 8100, 12300, 7400, 9000, 5100, 6200).mapIndexed { i, steps ->
            DailyStepCount(dateEpochDay = 19_823L + i, steps = steps)
        }
        WeeklyReportDetailScreen(
            state = WeeklyReportDetailState(
                weekStartEpochDay = LocalDate.of(2026, 4, 14).toEpochDay(),
                totalSteps = 52_300,
                achievementPct = 86,
                achievedDays = 6,
                totalDays = 7,
                achievementRate = 6f / 7f,
                bestDayEpochDay = LocalDate.of(2026, 4, 15).toEpochDay(),
                bestHour = 15,
                longestAchievedStreak = 5,
                summaryMessageType = WeeklyReportSummaryMessageType.GoodProgress,
                dailyCounts = fakeCounts,
                isLoading = false,
            ),
            graphicsLayer = rememberGraphicsLayer(),
            onClickBack = {},
            onClickShare = {},
        )
    }
}
