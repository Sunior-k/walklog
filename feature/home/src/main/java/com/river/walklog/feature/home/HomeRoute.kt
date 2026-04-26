package com.river.walklog.feature.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.river.walklog.core.designsystem.R
import com.river.walklog.core.designsystem.component.WeatherSummaryCard
import com.river.walklog.core.designsystem.foundation.RecapColors
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.core.engine.ActivityState
import com.river.walklog.feature.home.component.ForecastBanner
import com.river.walklog.feature.home.component.ForecastBottomSheet
import com.river.walklog.feature.home.component.MissionCard
import com.river.walklog.feature.home.component.PermissionRequiredCard
import com.river.walklog.feature.home.component.SensorUnavailableCard
import com.river.walklog.feature.home.component.StreakBadge
import com.river.walklog.feature.home.component.WalkProgressRing

@Composable
fun HomeRoute(
    onNavigateToWeeklyReport: () -> Unit,
    onNavigateToMission: () -> Unit,
    onNavigateToRecap: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showForecastSheet by rememberSaveable { mutableStateOf(false) }

    // ─── Health Connect READ_STEPS 권한 요청 ────────────────────────────────
    val healthPermissionsLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
    ) { grantedPermissions: Set<String> ->
        val granted = grantedPermissions.contains(
            HealthPermission.getReadPermission(StepsRecord::class),
        )
        viewModel.handleIntent(HomeIntent.OnPermissionResult(granted))
    }

    // ─── 초기 Health Connect 권한 상태 확인 ────────────────────────────────
    LaunchedEffect(Unit) {
        val sdkStatus = HealthConnectClient.getSdkStatus(context)
        if (sdkStatus != HealthConnectClient.SDK_AVAILABLE) {
            viewModel.handleIntent(HomeIntent.OnPermissionResult(false))
            return@LaunchedEffect
        }
        val client = HealthConnectClient.getOrCreate(context)
        val granted = client.permissionController.getGrantedPermissions()
            .contains(HealthPermission.getReadPermission(StepsRecord::class))
        viewModel.handleIntent(HomeIntent.OnPermissionResult(granted))
    }

    HomeScreen(
        state = state,
        onClickTodayMission = {
            viewModel.handleIntent(HomeIntent.OnClickTodayMission)
            onNavigateToMission()
        },
        onClickWeeklyReport = {
            viewModel.handleIntent(HomeIntent.OnClickWeeklyReport)
            onNavigateToWeeklyReport()
        },
        onClickForecast = {
            viewModel.handleIntent(HomeIntent.OnClickForecast)
            showForecastSheet = true
        },
        onRefresh = { viewModel.handleIntent(HomeIntent.OnRefresh) },
        onRefreshWeather = { viewModel.handleIntent(HomeIntent.OnRefreshWeather) },
        onClickRecap = onNavigateToRecap,
        onRequestPermission = {
            if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
                healthPermissionsLauncher.launch(
                    setOf(HealthPermission.getReadPermission(StepsRecord::class)),
                )
            }
        },
    )

    if (showForecastSheet) {
        ForecastBottomSheet(
            title = state.forecastTitle,
            recommendedTimeText = state.forecastRecommendedTimeText.ifEmpty { "오늘 오후 3시가 최적이에요" },
            description = state.forecastDescription,
            averageStepsAtThisTime = state.forecastAverageStepsText.ifEmpty { "-" },
            activeDays = state.forecastActiveDaysText.ifEmpty { "-" },
            hourlyAverages = state.forecastHourlyAverages,
            peakHour = state.forecastPeakHour,
            onDismiss = { showForecastSheet = false },
            onClickStartWalking = { showForecastSheet = false },
        )
    }
}

@Composable
internal fun HomeScreen(
    state: HomeState,
    onClickTodayMission: () -> Unit,
    onClickWeeklyReport: () -> Unit,
    onClickForecast: () -> Unit,
    onRefresh: () -> Unit,
    onRefreshWeather: () -> Unit = {},
    onRequestPermission: () -> Unit,
    onClickRecap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WalkLogTheme.colors.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            HomeHeader(userName = state.userName, todayDateText = state.todayDateText)

            StreakBadge(streakDays = state.streakDays)

            when (state.sensorStatus) {
                SensorStatus.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .testTag(HomeTestTags.SENSOR_LOADING),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = WalkLogColor.Primary,
                            trackColor = WalkLogTheme.colors.onSurface.copy(alpha = IndicatorTrackAlpha),
                        )
                    }
                }

                SensorStatus.Unavailable -> SensorUnavailableCard()

                SensorStatus.PermissionRequired -> {
                    PermissionRequiredCard(onRequestPermission = onRequestPermission)
                }

                SensorStatus.Available -> {
                    ProgressSection(
                        currentSteps = state.currentSteps,
                        targetSteps = state.targetSteps,
                        isWalking = state.activityState == ActivityState.WALKING,
                    )
                    ForecastBanner(
                        title = state.forecastTitle,
                        description = state.forecastDescription,
                        onClick = onClickForecast,
                    )
                    MissionCard(
                        model = state.mission,
                        onClick = onClickTodayMission,
                    )
                }
            }

            if (state.sensorStatus != SensorStatus.Unavailable) {
                WeeklyReportSummaryCard(
                    totalStepsText = state.weeklyTotalStepsText,
                    achievementRateText = state.weeklyAchievementRateText,
                    bestDayText = state.bestDayText,
                    bestTimeText = state.bestTimeText,
                    bestStreakText = state.bestStreakText,
                    onClick = onClickWeeklyReport,
                )
            }

            WeatherSummaryCard(
                locationText = state.weatherLocationText,
                temperatureText = state.weatherTemperatureText,
                conditionText = state.weatherConditionText,
                adviceText = state.weatherAdviceText,
                supportingText = state.weatherSupportingText.ifBlank { null },
                onRefreshClick = onRefreshWeather,
            )

            if (state.recapMonthLabel.isNotEmpty()) {
                RecapCard(
                    monthLabel = state.recapMonthLabel,
                    totalStepsText = state.recapTotalStepsText,
                    onClick = onClickRecap,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(WalkLogColor.StaticBlack.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = WalkLogColor.Primary,
                    trackColor = WalkLogTheme.colors.onSurface.copy(alpha = IndicatorTrackAlpha),
                )
            }
        }
    }
}

private const val IndicatorTrackAlpha = 0.18f

// ─── Private composables ──────────────────────────────────────────────────────

@Composable
private fun HomeHeader(userName: String, todayDateText: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "${userName}님, 오늘도 걸어볼까요?",
            style = WalkLogTheme.typography.typography3B,
            color = WalkLogTheme.colors.onBackground,
        )
        Text(
            text = todayDateText,
            style = WalkLogTheme.typography.typography6M,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProgressSection(currentSteps: Int, targetSteps: Int, isWalking: Boolean = false) {
    val remainingSteps = (targetSteps - currentSteps).coerceAtLeast(0)
    val progressPercent =
        if (targetSteps <= 0) 0 else ((currentSteps * 100) / targetSteps).coerceIn(0, 100)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WalkLogTheme.colors.background, RoundedCornerShape(28.dp))
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WalkProgressRing(currentSteps = currentSteps, targetSteps = targetSteps, isWalking = isWalking)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "$progressPercent%",
            style = WalkLogTheme.typography.typography4B,
            color = WalkLogTheme.colors.primary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "목표: $targetSteps 보",
            style = WalkLogTheme.typography.typography7M,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun WeeklyReportSummaryCard(
    totalStepsText: String,
    achievementRateText: String,
    bestDayText: String,
    bestTimeText: String,
    bestStreakText: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WalkLogTheme.colors.surface, RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "지난주 리포트",
                style = WalkLogTheme.typography.typography5SB,
                color = WalkLogTheme.colors.onSurface,
            )
            TextButton(
                onClick = onClick,
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                Text(
                    text = "전체보기",
                    style = WalkLogTheme.typography.typography7SB,
                    color = WalkLogTheme.colors.primary,
                )
            }
        }
        ReportRow("총 걸음 수", totalStepsText)
        ReportRow("달성률", achievementRateText)
        ReportRow("가장 많이 걸은 요일", bestDayText)
        ReportRow("가장 활발한 시간대", bestTimeText)
        ReportRow("최고 스트릭", bestStreakText)
    }
}

@Composable
private fun ReportRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
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
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = WalkLogTheme.colors.outlineVariant,
        )
    }
}

@Composable
private fun RecapCard(
    monthLabel: String,
    totalStepsText: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    listOf(RecapColors.CardGradientStart, RecapColors.CardGradientEnd),
                ),
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "$monthLabel 리캡",
                style = WalkLogTheme.typography.typography7SB,
                color = WalkLogColor.StaticWhite.copy(alpha = 0.6f),
            )
            Text(
                text = if (totalStepsText.isNotEmpty()) totalStepsText + " 걸었어요" else "리캡 보기",
                style = WalkLogTheme.typography.typography5SB,
                color = WalkLogColor.StaticWhite,
            )
        }
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_forward),
            contentDescription = null,
            tint = WalkLogColor.StaticWhite.copy(alpha = 0.7f),
        )
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPreview() {
    WalkLogTheme {
        HomeScreen(
            state = HomeState(
                userName = "익명",
                todayDateText = "4월 10일 목요일",
                sensorStatus = SensorStatus.Available,
                currentSteps = 4200,
                streakDays = 3,
                weeklyTotalStepsText = "42,300보",
                weeklyAchievementRateText = "86%",
                bestDayText = "수요일",
                bestTimeText = "오후 3시",
                bestStreakText = "5일",
            ),
            onClickTodayMission = {},
            onClickWeeklyReport = {},
            onClickForecast = {},
            onRefresh = {},
            onRequestPermission = {},
            onClickRecap = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390)
@Composable
private fun HomeScreenPermissionPreview() {
    WalkLogTheme {
        HomeScreen(
            state = HomeState(sensorStatus = SensorStatus.PermissionRequired),
            onClickTodayMission = {},
            onClickWeeklyReport = {},
            onClickForecast = {},
            onRefresh = {},
            onRequestPermission = {},
            onClickRecap = {},
        )
    }
}
