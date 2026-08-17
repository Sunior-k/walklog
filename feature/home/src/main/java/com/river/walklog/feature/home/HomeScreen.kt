package com.river.walklog.feature.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.river.walklog.core.designsystem.R
import com.river.walklog.core.designsystem.component.BadgeColor
import com.river.walklog.core.designsystem.component.BadgeSize
import com.river.walklog.core.designsystem.component.BadgeVariant
import com.river.walklog.core.designsystem.component.BaseBadge
import com.river.walklog.core.designsystem.component.PremiumBackgroundEffect
import com.river.walklog.core.designsystem.component.PremiumChip
import com.river.walklog.core.designsystem.component.WeatherSummaryCard
import com.river.walklog.core.designsystem.foundation.RecapColors
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.core.model.ActivityState
import com.river.walklog.core.ui.temperatureText
import com.river.walklog.core.ui.withComma
import com.river.walklog.feature.home.component.ForecastBanner
import com.river.walklog.feature.home.component.ForecastBottomSheet
import com.river.walklog.feature.home.component.MissionCard
import com.river.walklog.feature.home.component.PermissionRequiredCard
import com.river.walklog.feature.home.component.SensorUnavailableCard
import com.river.walklog.feature.home.component.StreakBadge
import com.river.walklog.feature.home.component.WalkProgressRing
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import com.river.walklog.feature.home.R as HomeR

@Composable
fun HomeRoute(
    onNavigateToWeeklyReport: () -> Unit,
    onNavigateToMission: () -> Unit,
    onNavigateToRecap: () -> Unit,
    onNavigateToBadgeCollection: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showForecastSheet by rememberSaveable { mutableStateOf(false) }

    // 위치 권한 런처
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.refreshWeather()
    }

    // Health Connect 권한 요청 런처
    val healthPermissionsLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
    ) { grantedPermissions: Set<String> ->
        val granted = grantedPermissions.contains(
            HealthPermission.getReadPermission(StepsRecord::class),
        )
        viewModel.updatePermissionResult(granted)
    }

    // 위치 권한 초기 확인 — 미허용 상태면 요청
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            val shouldShowRationale = (context as? Activity)
                ?.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                ?: false
            if (!shouldShowRationale) {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
    }

    // 초기 권한 확인
    LaunchedEffect(Unit) {
        val sdkStatus = HealthConnectClient.getSdkStatus(context)
        if (sdkStatus != HealthConnectClient.SDK_AVAILABLE) {
            viewModel.updatePermissionResult(false)
            return@LaunchedEffect
        }
        val client = HealthConnectClient.getOrCreate(context)
        val granted = client.permissionController.getGrantedPermissions()
            .contains(HealthPermission.getReadPermission(StepsRecord::class))
        viewModel.updatePermissionResult(granted)
    }

    val isExpanded = currentWindowAdaptiveInfo().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    HomeScreen(
        state = state,
        isExpanded = isExpanded,
        onClickTodayMission = onNavigateToMission,
        onClickWeeklyReport = onNavigateToWeeklyReport,
        onClickForecast = { showForecastSheet = true },
        onRefresh = viewModel::refresh,
        onRefreshWeather = viewModel::refreshWeather,
        onClickRecap = onNavigateToRecap,
        onClickBadge = onNavigateToBadgeCollection,
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
            title = stringResource(HomeR.string.forecast_title),
            recommendedTimeText = state.forecastRecommendedTimeText(),
            description = state.forecastDescriptionText(),
            averageStepsAtThisTime = state.forecastAverageStepsText(),
            activeDays = state.forecastActiveDaysText(),
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
    onClickBadge: () -> Unit = {},
    isExpanded: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WalkLogTheme.colors.background),
    ) {
        if (WalkLogTheme.isPremium) {
            PremiumBackgroundEffect(
                mode = WalkLogTheme.premiumVisualMode,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (isExpanded) {
            HomeExpandedContent(
                state = state,
                onClickTodayMission = onClickTodayMission,
                onClickWeeklyReport = onClickWeeklyReport,
                onClickForecast = onClickForecast,
                onRefreshWeather = onRefreshWeather,
                onRequestPermission = onRequestPermission,
                onClickRecap = onClickRecap,
                onClickBadge = onClickBadge,
            )
        } else {
            HomeCompactContent(
                state = state,
                onClickTodayMission = onClickTodayMission,
                onClickWeeklyReport = onClickWeeklyReport,
                onClickForecast = onClickForecast,
                onRefreshWeather = onRefreshWeather,
                onRequestPermission = onRequestPermission,
                onClickRecap = onClickRecap,
                onClickBadge = onClickBadge,
            )
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

@Composable
private fun HomeCompactContent(
    state: HomeState,
    onClickTodayMission: () -> Unit,
    onClickWeeklyReport: () -> Unit,
    onClickForecast: () -> Unit,
    onRefreshWeather: () -> Unit,
    onRequestPermission: () -> Unit,
    onClickRecap: () -> Unit,
    onClickBadge: () -> Unit = {},
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
        HomeHeader(
            userName = state.userName,
            todayDateText = state.todayDate.formatAsHomeDate(),
            hasGoldBadge = state.hasGoldBadge,
            onClickBadge = onClickBadge,
        )

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
                    title = stringResource(HomeR.string.forecast_title),
                    description = state.forecastDescriptionText(),
                    onClick = onClickForecast,
                )
                MissionCard(
                    title = stringResource(HomeR.string.mission_default_title),
                    currentSteps = state.currentSteps,
                    targetSteps = state.targetSteps,
                    rewardText = stringResource(HomeR.string.mission_reward_default, state.missionRewardPoints),
                    isRecovery = state.missionIsRecovery,
                    isCompleted = state.missionIsCompleted,
                    onClick = onClickTodayMission,
                )
            }
        }

        if (state.sensorStatus != SensorStatus.Unavailable) {
            WeeklyReportSummaryCard(onClick = onClickWeeklyReport)
        }

        WeatherSummaryCard(
            locationText = stringResource(HomeR.string.weather_location, state.weather.locationName),
            temperatureText = state.weather.temperatureText,
            conditionText = stringResource(state.weather.condition.conditionRes()),
            adviceText = stringResource(state.weather.walkingAdviceRes()),
            supportingText = state.weatherSupportingText(),
            isLoading = state.isWeatherLoading,
            onRefreshClick = onRefreshWeather,
        )

        state.recapYearMonth?.let { recapYearMonth ->
            RecapCard(
                monthLabel = recapYearMonth.formatAsMonthLabel(),
                totalStepsText = stringResource(HomeR.string.steps_format, state.recapTotalSteps.withComma()),
                onClick = onClickRecap,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun HomeExpandedContent(
    state: HomeState,
    onClickTodayMission: () -> Unit,
    onClickWeeklyReport: () -> Unit,
    onClickForecast: () -> Unit,
    onRefreshWeather: () -> Unit,
    onRequestPermission: () -> Unit,
    onClickRecap: () -> Unit,
    onClickBadge: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HomeHeader(
                userName = state.userName,
                todayDateText = state.todayDate.formatAsHomeDate(),
                hasGoldBadge = state.hasGoldBadge,
                onClickBadge = onClickBadge,
            )
            StreakBadge(streakDays = state.streakDays)
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1.05f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                when (state.sensorStatus) {
                    SensorStatus.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(32.dp))
                                .background(WalkLogTheme.colors.surface)
                                .testTag(HomeTestTags.SENSOR_LOADING),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                color = WalkLogColor.Primary,
                                trackColor = WalkLogTheme.colors.onSurface.copy(alpha = IndicatorTrackAlpha),
                            )
                        }
                    }

                    SensorStatus.Unavailable -> {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            SensorUnavailableCard()
                        }
                    }

                    SensorStatus.PermissionRequired -> {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            PermissionRequiredCard(onRequestPermission = onRequestPermission)
                        }
                    }

                    SensorStatus.Available -> DashboardProgressPanel(
                        currentSteps = state.currentSteps,
                        targetSteps = state.targetSteps,
                        isWalking = state.activityState == ActivityState.WALKING,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(0.95f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.sensorStatus == SensorStatus.Available) {
                    MissionCard(
                        title = stringResource(HomeR.string.mission_default_title),
                        currentSteps = state.currentSteps,
                        targetSteps = state.targetSteps,
                        rewardText = stringResource(HomeR.string.mission_reward_default, state.missionRewardPoints),
                        isRecovery = state.missionIsRecovery,
                        isCompleted = state.missionIsCompleted,
                        onClick = onClickTodayMission,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                ForecastBanner(
                    title = stringResource(HomeR.string.forecast_title),
                    description = state.forecastDescriptionText(),
                    onClick = onClickForecast,
                    modifier = Modifier.fillMaxWidth(),
                )

                WeatherSummaryCard(
                    locationText = stringResource(HomeR.string.weather_location, state.weather.locationName),
                    temperatureText = state.weather.temperatureText,
                    conditionText = stringResource(state.weather.condition.conditionRes()),
                    adviceText = stringResource(state.weather.walkingAdviceRes()),
                    supportingText = state.weatherSupportingText(),
                    isLoading = state.isWeatherLoading,
                    onRefreshClick = onRefreshWeather,
                    modifier = Modifier.fillMaxWidth(),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (state.sensorStatus != SensorStatus.Unavailable) {
                        WeeklyReportSummaryCard(
                            onClick = onClickWeeklyReport,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        )
                    }

                    val recapYearMonth = state.recapYearMonth
                    if (recapYearMonth != null) {
                        RecapCard(
                            monthLabel = recapYearMonth.formatAsMonthLabel(),
                            totalStepsText = stringResource(HomeR.string.steps_format, state.recapTotalSteps.withComma()),
                            onClick = onClickRecap,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        )
                    } else if (!state.isRecapPreviewLoading) {
                        FoldHomeAccentCard(
                            title = stringResource(HomeR.string.home_recap_error_title),
                            description = stringResource(HomeR.string.home_recap_error_desc),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalDate.formatAsHomeDate(): String {
    val locale = LocalConfiguration.current.locales[0]
    val pattern = when (locale.language) {
        "ja" -> "M月d日 EEEE"
        "ko" -> "M월 d일 EEEE"
        else -> "MMMM d, EEEE"
    }
    return format(DateTimeFormatter.ofPattern(pattern, locale))
}

@Composable
private fun YearMonth.formatAsMonthLabel(): String {
    val locale = LocalConfiguration.current.locales[0]
    return atDay(1).format(DateTimeFormatter.ofPattern("MMMM", locale))
}

@Composable
private fun HomeState.forecastDescriptionText(): String {
    val displayHour = forecastPeakHour.toDisplayHour()
    return if (forecastPeakHour < 12) {
        stringResource(HomeR.string.forecast_description_am, displayHour)
    } else {
        stringResource(HomeR.string.forecast_description_pm, displayHour)
    }
}

@Composable
private fun HomeState.forecastRecommendedTimeText(): String {
    val displayHour = forecastPeakHour.toDisplayHour()
    return if (forecastPeakHour < 12) {
        stringResource(HomeR.string.forecast_recommended_time_am, displayHour)
    } else {
        stringResource(HomeR.string.forecast_recommended_time_pm, displayHour)
    }
}

@Composable
private fun HomeState.forecastAverageStepsText(): String =
    forecastAverageStepsAtPeakHour?.let {
        stringResource(HomeR.string.forecast_avg_steps, it.withComma())
    } ?: "-"

@Composable
private fun HomeState.forecastActiveDaysText(): String =
    if (forecastTotalDays > 0) {
        stringResource(HomeR.string.forecast_active_days, forecastTotalDays, forecastActiveDays)
    } else {
        "-"
    }

@Composable
private fun HomeState.weatherSupportingText(): String? {
    val locale = LocalConfiguration.current.locales[0]
    val windFormat = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }
    return listOfNotNull(
        weather.precipitationProbability?.let { stringResource(HomeR.string.weather_precipitation, it) },
        weather.humidity?.let { stringResource(HomeR.string.weather_humidity, it) },
        weather.windSpeedMetersPerSecond?.let {
            stringResource(HomeR.string.weather_wind_speed, windFormat.format(it))
        },
    ).joinToString(" · ").ifBlank { null }
}

private fun Int.toDisplayHour(): Int = when {
    this == 0 -> 12
    this <= 12 -> this
    else -> this - 12
}

@Composable
private fun DashboardProgressPanel(
    currentSteps: Int,
    targetSteps: Int,
    isWalking: Boolean,
    modifier: Modifier = Modifier,
) {
    val remainingSteps = (targetSteps - currentSteps).coerceAtLeast(0)
    val progressPercent =
        if (targetSteps <= 0) 0 else ((currentSteps * 100) / targetSteps).coerceIn(0, 100)
    val statusText = if (remainingSteps == 0) {
        stringResource(HomeR.string.home_goal_achieved)
    } else {
        stringResource(HomeR.string.home_steps_remaining, remainingSteps)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(34.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WalkLogTheme.colors.surface,
                        WalkLogTheme.colors.primaryContainer.copy(alpha = 0.9f),
                    ),
                ),
            )
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            WalkProgressRing(
                currentSteps = currentSteps,
                targetSteps = targetSteps,
                isWalking = isWalking,
                modifier = Modifier
                    .fillMaxWidth(0.66f)
                    .aspectRatio(1f),
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = statusText,
                    style = WalkLogTheme.typography.typography3B,
                    color = WalkLogTheme.colors.onSurface,
                )
                Text(
                    text = stringResource(HomeR.string.home_progress_detail, progressPercent, targetSteps),
                    style = WalkLogTheme.typography.typography6M,
                    color = WalkLogTheme.colors.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DashboardMetricChip(
                    label = stringResource(HomeR.string.home_current_label),
                    value = stringResource(HomeR.string.home_steps_value, currentSteps.coerceAtLeast(0)),
                    modifier = Modifier.weight(1f),
                )
                DashboardMetricChip(
                    label = stringResource(HomeR.string.home_remaining_label),
                    value = stringResource(HomeR.string.home_steps_value, remainingSteps),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DashboardMetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(WalkLogColor.StaticWhite.copy(alpha = 0.76f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = WalkLogTheme.typography.subTypography12M,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
        Text(
            text = value,
            style = WalkLogTheme.typography.typography6B,
            color = WalkLogTheme.colors.onSurface,
        )
    }
}

@Composable
private fun FoldHomeAccentCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(WalkLogTheme.colors.primaryContainer)
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                style = WalkLogTheme.typography.typography6SB,
                color = WalkLogTheme.colors.onPrimaryContainer,
            )
            Text(
                text = description,
                style = WalkLogTheme.typography.subTypography12M,
                color = WalkLogTheme.colors.onPrimaryContainer.copy(alpha = 0.74f),
            )
        }
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_forward),
            contentDescription = null,
            tint = WalkLogTheme.colors.onPrimaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.End),
        )
    }
}

private const val IndicatorTrackAlpha = 0.18f

@Composable
private fun HomeHeader(
    userName: String,
    todayDateText: String,
    hasGoldBadge: Boolean = false,
    onClickBadge: () -> Unit = {},
) {
    val displayName = userName.ifBlank { stringResource(HomeR.string.anonymous) }
    val goldBadgeChipCd = stringResource(HomeR.string.home_gold_badge_chip_cd)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(HomeR.string.home_greeting, displayName),
                style = WalkLogTheme.typography.typography3B,
                color = WalkLogTheme.colors.onBackground,
            )
            if (hasGoldBadge) {
                BaseBadge(
                    modifier = Modifier
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .clickable(onClick = onClickBadge)
                        .semantics { contentDescription = goldBadgeChipCd },
                    text = stringResource(HomeR.string.home_gold_badge_chip),
                    size = BadgeSize.Small,
                    variant = BadgeVariant.Fill,
                    color = BadgeColor.Yellow,
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = todayDateText,
                style = WalkLogTheme.typography.typography6M,
                color = WalkLogTheme.colors.onSurfaceVariant,
            )
            if (WalkLogTheme.isPremium) {
                PremiumChip(text = stringResource(HomeR.string.home_premium_chip_label))
            }
        }
    }
}

@Composable
private fun ProgressSection(currentSteps: Int, targetSteps: Int, isWalking: Boolean = false) {
    val remainingSteps = (targetSteps - currentSteps).coerceAtLeast(0)
    val progressPercent =
        if (targetSteps <= 0) 0 else ((currentSteps * 100) / targetSteps).coerceIn(0, 100)
    val isPremium = WalkLogTheme.isPremium

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .then(
                if (isPremium) {
                    // 프리미엄일 때는 채우지 않는다 — 홈 화면 전체에 깔린 배경 효과(유성우/태양/빗줄기)가
                    // 이 카드 안까지 그대로 이어져 보이도록 비워둔다.
                    Modifier
                } else {
                    Modifier.background(WalkLogTheme.colors.background)
                },
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                text = stringResource(HomeR.string.home_goal_steps, targetSteps),
                style = WalkLogTheme.typography.typography7M,
                color = WalkLogTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WeeklyReportSummaryCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(WalkLogTheme.colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(HomeR.string.home_weekly_report_title),
                style = WalkLogTheme.typography.typography5SB,
                color = WalkLogTheme.colors.onSurface,
            )
            Text(
                text = stringResource(HomeR.string.home_weekly_report_subtitle),
                style = WalkLogTheme.typography.typography7M,
                color = WalkLogTheme.colors.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_forward),
            contentDescription = null,
            tint = WalkLogTheme.colors.primary,
        )
    }
}

@Composable
private fun RecapCard(
    monthLabel: String,
    totalStepsText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
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
                text = stringResource(HomeR.string.home_recap_label, monthLabel),
                style = WalkLogTheme.typography.typography7SB,
                color = WalkLogColor.StaticWhite.copy(alpha = 0.6f),
            )
            Text(
                text = if (totalStepsText.isNotEmpty()) stringResource(HomeR.string.home_recap_steps_walked, totalStepsText) else stringResource(HomeR.string.home_recap_view),
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

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPreview() {
    WalkLogTheme {
        HomeScreen(
            state = HomeState(
                userName = "익명",
                todayDate = LocalDate.of(2025, 4, 10),
                sensorStatus = SensorStatus.Available,
                currentSteps = 4200,
                streakDays = 3,
                weeklyTotalSteps = 42_300,
                weeklyAchievementRateText = "86%",
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
            state = HomeState(
                sensorStatus = SensorStatus.PermissionRequired,
                todayDate = LocalDate.of(2025, 4, 10),
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


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 900, heightDp = 840)
@Composable
private fun HomeScreenExpandedPreview() {
    WalkLogTheme {
        HomeScreen(
            state = HomeState(
                userName = "익명",
                todayDate = LocalDate.of(2025, 4, 10),
                sensorStatus = SensorStatus.Available,
                currentSteps = 4200,
                streakDays = 3,
            ),
            isExpanded = true,
            onClickTodayMission = {},
            onClickWeeklyReport = {},
            onClickForecast = {},
            onRefresh = {},
            onRequestPermission = {},
            onClickRecap = {},
        )
    }
}
