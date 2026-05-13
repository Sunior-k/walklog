package com.river.walklog.feature.mission

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.river.walklog.core.designsystem.component.WalkLogLinearProgressBar
import com.river.walklog.core.designsystem.component.WeatherSummaryCard
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.core.model.MissionType
import com.river.walklog.core.ui.temperatureText
import java.text.NumberFormat
import kotlin.math.min
import com.river.walklog.core.designsystem.R as DesignR

@Composable
fun MissionDetailRoute(
    onBack: () -> Unit,
    viewModel: MissionDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MissionDetailScreen(
        state = state,
        onClickBack = onBack,
        onClickAction = onBack,
        onRefreshWeather = viewModel::refreshWeather,
    )
}

@Composable
internal fun MissionDetailScreen(
    state: MissionDetailState,
    onClickBack: () -> Unit,
    onClickAction: () -> Unit,
    onRefreshWeather: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isExpanded = currentWindowAdaptiveInfo().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    Scaffold(
        modifier = modifier,
        topBar = { MissionDetailTopBar(onClickBack = onClickBack) },
        bottomBar = {
            MissionDetailBottomBar(
                isCompleted = state.isCompleted,
                isExpanded = isExpanded,
                onClickAction = onClickAction,
            )
        },
        containerColor = WalkLogTheme.colors.background,
    ) { padding ->
        if (isExpanded) {
            MissionDetailExpandedContent(
                state = state,
                onRefreshWeather = onRefreshWeather,
                modifier = Modifier.padding(padding),
            )
        } else {
            MissionDetailCompactContent(
                state = state,
                onRefreshWeather = onRefreshWeather,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun MissionDetailCompactContent(
    state: MissionDetailState,
    onRefreshWeather: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        MissionTypeBadge(missionType = state.missionType, isCompleted = state.isCompleted)
        MissionHeadlineSection(
            title = stringResource(R.string.mission_default_title),
            description = stringResource(R.string.mission_default_description),
        )
        MissionProgressCard(
            currentSteps = state.currentSteps,
            targetSteps = state.targetSteps,
            rewardText = stringResource(R.string.mission_reward_default, state.rewardPoints),
            isCompleted = state.isCompleted,
        )
        MissionGuideCard(
            missionType = state.missionType,
            recommendedTimeText = state.recommendedTimeText(),
            isCompleted = state.isCompleted,
        )
        MissionWeatherCard(state = state, onRefreshWeather = onRefreshWeather)
        MissionTimelineCard(
            missionType = state.missionType,
            isCompleted = state.isCompleted,
            targetSteps = state.targetSteps,
            currentSteps = state.currentSteps,
            rewardText = stringResource(R.string.mission_reward_default, state.rewardPoints),
            recommendedTimeText = state.recommendedTimeText(),
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun MissionDetailExpandedContent(
    state: MissionDetailState,
    onRefreshWeather: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1.05f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            MissionTypeBadge(missionType = state.missionType, isCompleted = state.isCompleted)
            MissionHeadlineSection(
                title = stringResource(R.string.mission_default_title),
                description = stringResource(R.string.mission_default_description),
            )
            MissionProgressCard(
                currentSteps = state.currentSteps,
                targetSteps = state.targetSteps,
                rewardText = stringResource(R.string.mission_reward_default, state.rewardPoints),
                isCompleted = state.isCompleted,
                stretchContent = true,
                modifier = Modifier.weight(1f),
            )
        }

        Column(
            modifier = Modifier
                .weight(0.95f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            MissionGuideCard(
                missionType = state.missionType,
                recommendedTimeText = state.recommendedTimeText(),
                isCompleted = state.isCompleted,
            )
            MissionWeatherCard(state = state, onRefreshWeather = onRefreshWeather)
            MissionTimelineCard(
                missionType = state.missionType,
                isCompleted = state.isCompleted,
                targetSteps = state.targetSteps,
                currentSteps = state.currentSteps,
                rewardText = stringResource(R.string.mission_reward_default, state.rewardPoints),
                recommendedTimeText = state.recommendedTimeText(),
                stretchContent = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MissionWeatherCard(
    state: MissionDetailState,
    onRefreshWeather: () -> Unit,
) {
    WeatherSummaryCard(
        locationText = stringResource(R.string.weather_location, state.weather.locationName),
        temperatureText = state.weather.temperatureText,
        conditionText = stringResource(state.weather.condition.conditionRes()),
        adviceText = stringResource(state.weather.walkingAdviceRes()),
        supportingText = state.weatherSupportingText(),
        onRefreshClick = onRefreshWeather,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MissionDetailTopBar(onClickBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.mission_detail_title),
                style = WalkLogTheme.typography.typography5SB,
                color = WalkLogTheme.colors.onSurface,
            )
        },
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(
                    imageVector = ImageVector.vectorResource(DesignR.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.back_button_cd),
                    tint = WalkLogTheme.colors.onSurface,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = WalkLogTheme.colors.background),
    )
}

@Composable
private fun MissionTypeBadge(missionType: MissionType, isCompleted: Boolean) {
    val text = when {
        isCompleted -> stringResource(R.string.mission_type_completed)
        missionType == MissionType.RECOVERY -> stringResource(R.string.mission_type_recovery)
        else -> stringResource(R.string.mission_type_today)
    }
    val bg = when {
        isCompleted -> WalkLogTheme.colors.tertiaryContainer
        missionType == MissionType.RECOVERY -> WalkLogTheme.colors.primaryContainer
        else -> WalkLogTheme.colors.surfaceVariant
    }
    val fg = when {
        isCompleted -> WalkLogTheme.colors.onTertiaryContainer
        missionType == MissionType.RECOVERY -> WalkLogTheme.colors.onPrimaryContainer
        else -> WalkLogTheme.colors.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text = text, style = WalkLogTheme.typography.typography6SB, color = fg)
    }
}

@Composable
private fun MissionHeadlineSection(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = WalkLogTheme.typography.typography3B, color = WalkLogTheme.colors.onSurface)
        Text(text = description, style = WalkLogTheme.typography.typography6M, color = WalkLogTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun MissionProgressCard(
    currentSteps: Int,
    targetSteps: Int,
    rewardText: String,
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
    stretchContent: Boolean = false,
) {
    val progress = if (targetSteps <= 0) 0f else (currentSteps.toFloat() / targetSteps).coerceIn(0f, 1f)
    val remaining = (targetSteps - currentSteps).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (isCompleted) WalkLogTheme.colors.tertiaryContainer else WalkLogTheme.colors.surface)
            .padding(20.dp),
        verticalArrangement = if (stretchContent) {
            Arrangement.SpaceBetween
        } else {
            Arrangement.spacedBy(14.dp)
        },
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(stringResource(R.string.mission_reward_label), style = WalkLogTheme.typography.typography6M, color = WalkLogTheme.colors.onSurfaceVariant)
            Text(rewardText, style = WalkLogTheme.typography.typography6B, color = if (isCompleted) WalkLogTheme.colors.onTertiaryContainer else WalkLogTheme.colors.primary)
        }
        if (stretchContent) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                MissionProgressRing(
                    currentSteps = currentSteps,
                    targetSteps = targetSteps,
                    isCompleted = isCompleted,
                    modifier = Modifier
                        .fillMaxWidth(0.58f)
                        .aspectRatio(1f),
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = if (isCompleted) stringResource(R.string.mission_goal_achieved) else stringResource(R.string.mission_steps_remaining, remaining),
                    style = WalkLogTheme.typography.typography5SB,
                    color = WalkLogTheme.colors.onSurface,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(stringResource(R.string.mission_steps_current, currentSteps), style = WalkLogTheme.typography.typography3B, color = WalkLogTheme.colors.onSurface)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.mission_steps_target, targetSteps), style = WalkLogTheme.typography.typography6M, color = WalkLogTheme.colors.onSurfaceVariant)
                }
            }
            WalkLogLinearProgressBar(
                progress = progress,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(999.dp)),
                color = if (isCompleted) WalkLogColor.Success else WalkLogColor.Primary,
                trackColor = WalkLogTheme.colors.outlineVariant,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (stretchContent) {
                Text(
                    text = if (isCompleted) stringResource(R.string.mission_goal_achieved) else stringResource(R.string.mission_steps_remaining, remaining),
                    style = WalkLogTheme.typography.typography5SB,
                    color = WalkLogTheme.colors.onSurface,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MissionFoldMetric(label = stringResource(R.string.mission_progress_rate), value = "${(progress * 100).toInt()}%", modifier = Modifier.weight(1f))
                    MissionFoldMetric(label = stringResource(R.string.mission_remaining_steps), value = stringResource(R.string.mission_steps_current, remaining), modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MissionGuideCard(
    missionType: MissionType,
    recommendedTimeText: String,
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
) {
    val title = when {
        isCompleted -> stringResource(R.string.guide_title_completed)
        missionType == MissionType.RECOVERY -> stringResource(R.string.guide_title_recovery)
        else -> stringResource(R.string.guide_title_default)
    }
    val desc = when {
        isCompleted -> stringResource(R.string.guide_desc_completed)
        missionType == MissionType.RECOVERY -> stringResource(R.string.guide_desc_recovery)
        else -> stringResource(R.string.guide_desc_default, recommendedTimeText)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(0.5.dp, WalkLogTheme.colors.secondary, RoundedCornerShape(20.dp))
            .background(WalkLogTheme.colors.secondaryContainer.copy(0.1f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_bulb),
                contentDescription = null,
                tint = WalkLogTheme.colors.secondaryContainer,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Walking Tip",
                style = WalkLogTheme.typography.typography6SB,
                color = WalkLogTheme.colors.secondaryContainer,
            )
        }
        Text(title, style = WalkLogTheme.typography.typography6B, color = WalkLogTheme.colors.secondaryContainer)
        Text(desc, style = WalkLogTheme.typography.typography6R, color = WalkLogTheme.colors.secondaryContainer)
    }
}

@Composable
private fun MissionProgressRing(
    currentSteps: Int,
    targetSteps: Int,
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
) {
    val progress = if (targetSteps <= 0) 0f else (currentSteps.toFloat() / targetSteps).coerceIn(0f, 1f)
    val trackColor = WalkLogTheme.colors.outlineVariant
    val progressColor = if (isCompleted) WalkLogColor.Success else WalkLogColor.Primary
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            val diameter = min(size.width, size.height)
            val radius = (diameter - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth),
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.mission_steps_current, currentSteps.coerceAtLeast(0)),
                style = WalkLogTheme.typography.typography3B,
                color = WalkLogTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.mission_ring_goal, targetSteps),
                style = WalkLogTheme.typography.typography7M,
                color = WalkLogTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MissionTimelineCard(
    missionType: MissionType,
    isCompleted: Boolean,
    targetSteps: Int,
    currentSteps: Int,
    rewardText: String,
    recommendedTimeText: String,
    modifier: Modifier = Modifier,
    stretchContent: Boolean = false,
) {
    val progress = if (targetSteps <= 0) 0f else (currentSteps.toFloat() / targetSteps).coerceIn(0f, 1f)
    val remaining = (targetSteps - currentSteps).coerceAtLeast(0)
    val midpointSteps = (targetSteps / 2).coerceAtLeast(1)
    val startTitle = when {
        isCompleted -> stringResource(R.string.timeline_start_completed)
        missionType == MissionType.RECOVERY -> stringResource(R.string.timeline_start_recovery)
        else -> stringResource(R.string.timeline_start_default)
    }
    val currentStage = when {
        isCompleted || progress >= 1f -> stringResource(R.string.timeline_reward_available)
        progress >= 0.5f -> stringResource(R.string.timeline_remaining, remaining)
        progress > 0f -> stringResource(R.string.timeline_midpoint_remaining, (midpointSteps - currentSteps).coerceAtLeast(0))
        else -> stringResource(R.string.timeline_start_recommend, recommendedTimeText)
    }
    val timelineItems = listOf(
        MissionTimelineItem(
            number = "1",
            title = startTitle,
            label = recommendedTimeText,
            description = if (progress > 0f || isCompleted) {
                stringResource(R.string.timeline_item1_started)
            } else {
                stringResource(R.string.timeline_item1_notstarted)
            },
            state = if (progress > 0f || isCompleted) MissionTimelineState.Done else MissionTimelineState.Active,
        ),
        MissionTimelineItem(
            number = "2",
            title = stringResource(R.string.timeline_midpoint_title),
            label = stringResource(R.string.mission_steps_current, midpointSteps),
            description = if (progress >= 0.5f || isCompleted) {
                stringResource(R.string.timeline_item2_half_done)
            } else {
                stringResource(R.string.timeline_item2_half_notdone)
            },
            state = when {
                isCompleted || progress >= 0.5f -> MissionTimelineState.Done
                progress > 0f -> MissionTimelineState.Active
                else -> MissionTimelineState.Upcoming
            },
        ),
        MissionTimelineItem(
            number = "3",
            title = if (isCompleted) stringResource(R.string.timeline_reward_earned) else stringResource(R.string.timeline_reward_pending),
            label = rewardText,
            description = if (isCompleted) {
                stringResource(R.string.timeline_reward_available_desc)
            } else {
                stringResource(R.string.timeline_reward_steps_more, remaining)
            },
            state = if (isCompleted) MissionTimelineState.Done else MissionTimelineState.Upcoming,
        ),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(WalkLogTheme.colors.surface)
            .border(0.5.dp, WalkLogTheme.colors.outlineVariant, RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = if (stretchContent) Arrangement.SpaceBetween else Arrangement.spacedBy(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.timeline_title),
                style = WalkLogTheme.typography.typography6SB,
                color = WalkLogTheme.colors.onSurface,
            )
            Text(
                text = currentStage,
                style = WalkLogTheme.typography.typography7M,
                color = WalkLogTheme.colors.onSurfaceVariant,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (stretchContent) Modifier.weight(1f) else Modifier),
            verticalArrangement = if (stretchContent) Arrangement.SpaceEvenly else Arrangement.spacedBy(10.dp),
        ) {
            timelineItems.forEachIndexed { index, item ->
                MissionTimelineStep(
                    item = item,
                    isLast = index == timelineItems.lastIndex,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(WalkLogTheme.colors.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (isCompleted) stringResource(R.string.timeline_status_completed) else stringResource(R.string.timeline_next_checkpoint),
                    style = WalkLogTheme.typography.subTypography12M,
                    color = WalkLogTheme.colors.onSurfaceVariant,
                )
                Text(
                    text = currentStage,
                    style = WalkLogTheme.typography.typography6B,
                    color = WalkLogTheme.colors.onSurface,
                )
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                style = WalkLogTheme.typography.typography5SB,
                color = if (isCompleted) WalkLogColor.Success else WalkLogTheme.colors.primary,
            )
        }
    }
}

@Composable
private fun MissionTimelineStep(
    item: MissionTimelineItem,
    isLast: Boolean,
) {
    val markerColor = when (item.state) {
        MissionTimelineState.Done -> WalkLogColor.Success
        MissionTimelineState.Active -> WalkLogTheme.colors.primary
        MissionTimelineState.Upcoming -> WalkLogTheme.colors.surfaceVariant
    }
    val markerTextColor = when (item.state) {
        MissionTimelineState.Upcoming -> WalkLogTheme.colors.onSurfaceVariant
        else -> WalkLogColor.StaticBlack
    }
    val titleColor = when (item.state) {
        MissionTimelineState.Upcoming -> WalkLogTheme.colors.onSurfaceVariant
        else -> WalkLogTheme.colors.onSurface
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        MissionTimelineMarker(
            number = item.number,
            markerColor = markerColor,
            markerTextColor = markerTextColor,
            lineColor = if (item.state == MissionTimelineState.Done) WalkLogColor.Success else WalkLogTheme.colors.outlineVariant,
            isLast = isLast,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = item.title,
                    style = WalkLogTheme.typography.typography6B,
                    color = titleColor,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = item.label,
                    style = WalkLogTheme.typography.typography7SB,
                    color = if (item.state == MissionTimelineState.Done) WalkLogColor.Success else WalkLogTheme.colors.primary,
                )
            }
            Text(
                text = item.description,
                style = WalkLogTheme.typography.typography7M,
                color = WalkLogTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MissionTimelineMarker(
    number: String,
    markerColor: Color,
    markerTextColor: Color,
    lineColor: Color,
    isLast: Boolean,
) {
    Column(
        modifier = Modifier.width(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(28.dp)
                .clip(CircleShape)
                .background(markerColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(number, style = WalkLogTheme.typography.typography7SB, color = markerTextColor)
        }
        if (!isLast) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(42.dp)
                    .background(lineColor),
            )
        }
    }
}

private data class MissionTimelineItem(
    val number: String,
    val title: String,
    val label: String,
    val description: String,
    val state: MissionTimelineState,
)

private enum class MissionTimelineState {
    Done,
    Active,
    Upcoming,
}

@Composable
private fun MissionFoldMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(WalkLogTheme.colors.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = WalkLogTheme.typography.subTypography12M, color = WalkLogTheme.colors.onSurfaceVariant)
        Text(value, style = WalkLogTheme.typography.typography6B, color = WalkLogTheme.colors.onSurface)
    }
}

@Composable
private fun MissionDetailBottomBar(
    isCompleted: Boolean,
    isExpanded: Boolean,
    onClickAction: () -> Unit,
) {
    Surface(shadowElevation = 8.dp, color = WalkLogTheme.colors.background) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isExpanded) 28.dp else 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Button(
                onClick = onClickAction,
                enabled = !isCompleted,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isExpanded) Modifier.widthIn(max = 360.dp) else Modifier)
                    .navigationBarsPadding(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) WalkLogColor.Gray200 else WalkLogColor.Primary,
                    contentColor = if (isCompleted) WalkLogColor.Gray500 else WalkLogColor.StaticBlack,
                ),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                Text(
                    text = if (isCompleted) stringResource(R.string.mission_action_completed) else stringResource(R.string.mission_action_start),
                    style = WalkLogTheme.typography.typography6SB,
                )
            }
        }
    }
}

@Composable
private fun MissionDetailState.recommendedTimeText(): String {
    val hour = recommendedPeakHour ?: DEFAULT_RECOMMENDED_HOUR
    val displayHour = when {
        hour == 0 -> 12
        hour <= 12 -> hour
        else -> hour - 12
    }
    return if (hour < 12) {
        stringResource(R.string.recommended_time_am, displayHour)
    } else {
        stringResource(R.string.recommended_time_pm, displayHour)
    }
}

@Composable
private fun MissionDetailState.weatherSupportingText(): String? {
    val locale = LocalConfiguration.current.locales[0]
    val windFormat = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }
    return listOfNotNull(
        weather.precipitationProbability?.let { stringResource(R.string.weather_precipitation, it) },
        weather.humidity?.let { stringResource(R.string.weather_humidity, it) },
        weather.windSpeedMetersPerSecond?.let {
            stringResource(R.string.weather_wind_speed, windFormat.format(it))
        },
    ).joinToString(" · ").ifBlank { null }
}

private const val DEFAULT_RECOMMENDED_HOUR = 15

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844)
@Composable
private fun MissionDetailScreenPreview() {
    WalkLogTheme { MissionDetailScreen(state = MissionDetailState(), onClickBack = {}, onClickAction = {}) }
}
