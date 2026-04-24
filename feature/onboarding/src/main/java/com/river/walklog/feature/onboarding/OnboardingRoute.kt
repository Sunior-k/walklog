package com.river.walklog.feature.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.river.walklog.core.designsystem.component.CustomSlider
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme

@Composable
fun OnboardingRoute(
    onNavigateToHome: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navigateToHome by viewModel.navigateToHome.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Health Connect READ_STEPS 권한 요청
    val healthPermissionsLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
    ) { grantedPermissions: Set<String> ->
        val granted = grantedPermissions.contains(
            HealthPermission.getReadPermission(StepsRecord::class),
        )
        viewModel.handleIntent(OnboardingIntent.OnPermissionResult(granted))
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _ ->
        viewModel.handleIntent(OnboardingIntent.OnClickComplete)
    }

    LaunchedEffect(navigateToHome) {
        if (navigateToHome) onNavigateToHome()
    }

    OnboardingScreen(
        state = state,
        onClickNext = {
            when (state.currentPage) {
                0 -> {
                    if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
                        healthPermissionsLauncher.launch(
                            setOf(HealthPermission.getReadPermission(StepsRecord::class)),
                        )
                    } else {
                        viewModel.handleIntent(OnboardingIntent.OnPermissionResult(false))
                    }
                }
                1 -> viewModel.handleIntent(OnboardingIntent.OnClickNext)
                2 -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.handleIntent(OnboardingIntent.OnClickComplete)
                    }
                }
            }
        },
        onStepGoalChanged = { viewModel.handleIntent(OnboardingIntent.OnStepGoalChanged(it)) },
        onNotificationsToggled = { viewModel.handleIntent(OnboardingIntent.OnNotificationsToggled(it)) },
    )
}

@Composable
private fun OnboardingScreen(
    state: OnboardingState,
    onClickNext: () -> Unit,
    onStepGoalChanged: (Int) -> Unit,
    onNotificationsToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { 3 })

    LaunchedEffect(state.currentPage) {
        pagerState.animateScrollToPage(state.currentPage)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WalkLogTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PageIndicator(
            pageCount = 3,
            currentPage = state.currentPage,
            modifier = Modifier.padding(top = 24.dp),
        )

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { page ->
            when (page) {
                0 -> PermissionPage()
                1 -> GoalPage(
                    stepGoal = state.dailyStepGoal,
                    onStepGoalChanged = onStepGoalChanged,
                )
                2 -> NotificationPage(
                    notificationsEnabled = state.notificationsEnabled,
                    onToggled = onNotificationsToggled,
                )
            }
        }

        Button(
            onClick = onClickNext,
            enabled = !state.isCompleting,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WalkLogColor.Primary,
                contentColor = WalkLogColor.StaticBlack,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .height(52.dp),
        ) {
            Text(
                text = when (state.currentPage) {
                    0 -> "권한 허용하기"
                    1 -> "다음"
                    else -> "시작하기"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isActive) 24.dp else 8.dp,
                animationSpec = tween(durationMillis = 300),
                label = "indicator_width",
            )
            Box(
                modifier = Modifier
                    .width(width)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) WalkLogColor.Primary else WalkLogColor.Gray200,
                    ),
            )
        }
    }
}

@Composable
private fun PermissionPage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "🚶", fontSize = 72.sp)
        Spacer(Modifier.height(32.dp))
        Text(
            text = "걸음 수를 읽으려면\nHealth Connect 권한이 필요해요",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = WalkLogTheme.colors.onSurface,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Health Connect에 저장된 걸음 수 데이터를\n읽을 수 있도록 허용해 주세요",
            fontSize = 15.sp,
            color = WalkLogTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )
    }
}

@Composable
private fun GoalPage(
    stepGoal: Int,
    onStepGoalChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "🎯", fontSize = 72.sp)
        Spacer(Modifier.height(32.dp))
        Text(
            text = "하루 목표 걸음 수를\n설정해 주세요",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = WalkLogTheme.colors.onSurface,
        )
        Spacer(Modifier.height(32.dp))
        Text(
            text = "%,d 보".format(stepGoal),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = WalkLogColor.Primary,
        )
        Spacer(Modifier.height(16.dp))
        CustomSlider(
            value = stepGoal.toFloat(),
            onValueChange = { onStepGoalChanged(it.toInt()) },
            minValue = 5_000f,
            maxValue = 20_000f,
            step = 500f,
            thumbColor = WalkLogColor.Primary,
            activeBarColor = WalkLogColor.Primary,
            inactiveBarColor = WalkLogColor.Gray200,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "5,000", fontSize = 12.sp, color = WalkLogTheme.colors.onSurfaceVariant)
            Text(text = "20,000", fontSize = 12.sp, color = WalkLogTheme.colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun NotificationPage(
    notificationsEnabled: Boolean,
    onToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "🔔", fontSize = 72.sp)
        Spacer(Modifier.height(32.dp))
        Text(
            text = "걷기 예보 알림을\n받으시겠어요?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = WalkLogTheme.colors.onSurface,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "행동 확률이 높은 시간대에\n맞춤 알림을 보내드려요",
            fontSize = 15.sp,
            color = WalkLogTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )
        Spacer(Modifier.height(32.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (notificationsEnabled) "알림 켜기" else "알림 끄기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = WalkLogTheme.colors.onSurface,
            )
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = onToggled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = WalkLogColor.StaticWhite,
                    checkedTrackColor = WalkLogColor.Primary,
                    uncheckedThumbColor = WalkLogColor.StaticWhite,
                    uncheckedTrackColor = WalkLogColor.Gray300,
                ),
            )
        }
    }
}
