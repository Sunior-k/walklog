package com.river.walklog.feature.mission

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.river.walklog.core.designsystem.R
import com.river.walklog.core.designsystem.component.WalkLogLinearProgressBar
import com.river.walklog.core.designsystem.component.WeatherSummaryCard
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.core.model.MissionType

@Composable
fun MissionDetailRoute(
    onBack: () -> Unit,
    viewModel: MissionDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MissionDetailScreen(
        state = state,
        onClickBack = {
            viewModel.handleIntent(MissionDetailIntent.OnClickBack)
            onBack()
        },
        onClickAction = { viewModel.handleIntent(MissionDetailIntent.OnClickStartWalking) },
        onRefreshWeather = { viewModel.handleIntent(MissionDetailIntent.OnRefreshWeather) },
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
    Scaffold(
        modifier = modifier,
        topBar = { MissionDetailTopBar(onClickBack = onClickBack) },
        bottomBar = {
            MissionDetailBottomBar(isCompleted = state.isCompleted, onClickAction = onClickAction)
        },
        containerColor = WalkLogTheme.colors.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            MissionTypeBadge(missionType = state.missionType, isCompleted = state.isCompleted)
            MissionHeadlineSection(title = state.title, description = state.description)
            MissionProgressCard(
                currentSteps = state.currentSteps,
                targetSteps = state.targetSteps,
                rewardText = state.rewardText,
                isCompleted = state.isCompleted,
            )
            MissionGuideCard(
                missionType = state.missionType,
                recommendedTimeText = state.recommendedTimeText,
                isCompleted = state.isCompleted,
            )
            WeatherSummaryCard(
                locationText = state.weatherLocationText,
                temperatureText = state.weatherTemperatureText,
                conditionText = state.weatherConditionText,
                adviceText = state.weatherAdviceText,
                supportingText = state.weatherSupportingText.ifBlank { null },
                onRefreshClick = onRefreshWeather,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MissionDetailTopBar(onClickBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "미션 상세",
                style = WalkLogTheme.typography.typography5SB,
                color = WalkLogTheme.colors.onSurface,
            )
        },
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
private fun MissionTypeBadge(missionType: MissionType, isCompleted: Boolean) {
    val text = when {
        isCompleted -> "달성 완료"
        missionType == MissionType.RECOVERY -> "회복 미션"
        else -> "오늘 미션"
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
private fun MissionProgressCard(currentSteps: Int, targetSteps: Int, rewardText: String, isCompleted: Boolean) {
    val progress = if (targetSteps <= 0) 0f else (currentSteps.toFloat() / targetSteps).coerceIn(0f, 1f)
    val remaining = (targetSteps - currentSteps).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (isCompleted) WalkLogTheme.colors.tertiaryContainer else WalkLogTheme.colors.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("보상", style = WalkLogTheme.typography.typography6M, color = WalkLogTheme.colors.onSurfaceVariant)
            Text(rewardText, style = WalkLogTheme.typography.typography6B, color = if (isCompleted) WalkLogTheme.colors.onTertiaryContainer else WalkLogTheme.colors.primary)
        }
        Text(
            text = if (isCompleted) "목표를 달성했어요 🎉" else "${remaining}보 남았어요",
            style = WalkLogTheme.typography.typography5SB,
            color = WalkLogTheme.colors.onSurface,
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text("${currentSteps}보", style = WalkLogTheme.typography.typography3B, color = WalkLogTheme.colors.onSurface)
            Spacer(Modifier.width(6.dp))
            Text("/ ${targetSteps}보", style = WalkLogTheme.typography.typography6M, color = WalkLogTheme.colors.onSurfaceVariant)
        }
        WalkLogLinearProgressBar(
            progress = progress,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(999.dp)),
            color = if (isCompleted) WalkLogColor.Success else WalkLogColor.Primary,
            trackColor = WalkLogTheme.colors.outlineVariant,
        )
    }
}

@Composable
private fun MissionGuideCard(missionType: MissionType, recommendedTimeText: String, isCompleted: Boolean) {
    val title = when {
        isCompleted -> "오늘도 꾸준히 걸어주셨네요"
        missionType == MissionType.RECOVERY -> "어제 놓친 목표를 다시 이어가보세요"
        else -> "지금이 걷기 좋은 타이밍이에요"
    }
    val desc = when {
        isCompleted -> "좋은 흐름을 유지하고 있어요. 내일도 가볍게 이어가보세요."
        missionType == MissionType.RECOVERY -> "회복 미션을 달성하면 스트릭을 유지할 수 있어요."
        else -> "$recommendedTimeText 전후가 평소 가장 많이 걷는 시간대예요."
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(0.5.dp, WalkLogTheme.colors.secondary, RoundedCornerShape(20.dp))
            .background(WalkLogTheme.colors.secondaryContainer.copy(0.1f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(com.river.walklog.feature.mission.R.drawable.ic_bulb),
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
private fun MissionDetailBottomBar(isCompleted: Boolean, onClickAction: () -> Unit) {
    Surface(shadowElevation = 8.dp, color = WalkLogTheme.colors.background) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Button(
                onClick = onClickAction,
                enabled = !isCompleted,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) WalkLogColor.Gray200 else WalkLogColor.Primary,
                    contentColor = if (isCompleted) WalkLogColor.Gray500 else WalkLogColor.StaticBlack,
                ),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                Text(
                    text = if (isCompleted) "이미 달성했어요" else "지금 걸으러 가기",
                    style = WalkLogTheme.typography.typography6SB,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844)
@Composable
private fun MissionDetailScreenPreview() {
    WalkLogTheme { MissionDetailScreen(state = MissionDetailState(), onClickBack = {}, onClickAction = {}) }
}
