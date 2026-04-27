package com.river.walklog.feature.onboarding

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RawRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.river.walklog.core.designsystem.R
import com.river.walklog.core.designsystem.component.CustomSlider
import com.river.walklog.core.designsystem.component.WalkLogLottie
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme

private data class OnboardingPageConfig(
    val illustrationBg: Color,
    val accentColor: Color,
    @RawRes val lottieResId: Int,
    val headline: String,
    val subtitle: String,
    val ctaText: String,
)

private val pageConfigs = listOf(
    OnboardingPageConfig(
        illustrationBg = WalkLogColor.PrimaryContainer,
        accentColor = WalkLogColor.Primary,
        lottieResId = R.raw.walking,
        headline = "걸음 수 연동하기",
        subtitle = "Health Connect에서 걸음 수를\n불러올 수 있도록 권한을 허용해 주세요",
        ctaText = "권한 허용하기",
    ),
    OnboardingPageConfig(
        illustrationBg = WalkLogColor.PrimaryLight.copy(alpha = 0.35f),
        accentColor = WalkLogColor.PrimaryDark,
        lottieResId = R.raw.target,
        headline = "목표를 세워요",
        subtitle = "하루 목표 걸음 수를 설정하면\n더 나은 걷기 습관을 만들어 드려요",
        ctaText = "다음",
    ),
    OnboardingPageConfig(
        illustrationBg = WalkLogColor.Accent.copy(alpha = 0.15f),
        accentColor = WalkLogColor.Accent,
        lottieResId = R.raw.clock,
        headline = "딱 맞는 시간에 알림을",
        subtitle = "걷기 좋은 시간대에\n맞춤 알림을 보내드릴게요",
        ctaText = "시작하기",
    ),
)

@Composable
fun OnboardingRoute(
    onNavigateToHome: () -> Unit,
    onExitApp: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navigateToHome by viewModel.navigateToHome.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 시스템 뒤로가기
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    BackHandler {
        val now = System.currentTimeMillis()
        if (now - lastBackPressTime < 2000L) {
            onExitApp()
        } else {
            lastBackPressTime = now
            Toast.makeText(context, "한 번 더 누르면 앱이 종료됩니다", Toast.LENGTH_SHORT).show()
        }
    }

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
            .background(Color.White)
            .statusBarsPadding(),
    ) {
        // 페이지 컨텐츠
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { page ->
            OnboardingPage(
                page = page,
                config = pageConfigs[page],
                state = state,
                onStepGoalChanged = onStepGoalChanged,
                onNotificationsToggled = onNotificationsToggled,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = onClickNext,
                enabled = !state.isCompleting,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WalkLogColor.Primary,
                    contentColor = WalkLogColor.StaticBlack,
                    disabledContainerColor = WalkLogColor.Primary.copy(alpha = 0.4f),
                    disabledContentColor = WalkLogColor.StaticBlack.copy(alpha = 0.4f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                if (state.isCompleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = WalkLogColor.StaticBlack,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = pageConfigs[state.currentPage].ctaText,
                        style = WalkLogTheme.typography.typography5SB,
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(
    page: Int,
    config: OnboardingPageConfig,
    state: OnboardingState,
    onStepGoalChanged: (Int) -> Unit,
    onNotificationsToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blob_$page")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f)
                .background(config.illustrationBg),
            contentAlignment = Alignment.Center,
        ) {
            // 소프트 blob
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    color = config.accentColor.copy(alpha = 0.11f),
                    radius = size.width * 0.52f * pulseScale,
                    center = Offset(size.width * 0.90f, size.height * 0.10f),
                )
                drawCircle(
                    color = config.accentColor.copy(alpha = 0.07f),
                    radius = size.width * 0.40f,
                    center = Offset(size.width * 0.06f, size.height * 0.90f),
                )
                drawCircle(
                    color = config.accentColor.copy(alpha = 0.50f),
                    radius = 8.dp.toPx() * pulseScale,
                    center = Offset(size.width * 0.16f, size.height * 0.20f),
                )
                drawCircle(
                    color = config.accentColor.copy(alpha = 0.35f),
                    radius = 6.dp.toPx(),
                    center = Offset(size.width * 0.84f, size.height * 0.74f),
                )
                drawCircle(
                    color = config.accentColor.copy(alpha = 0.55f),
                    radius = 5.dp.toPx() * (1f + (pulseScale - 1f) * 0.5f),
                    center = Offset(size.width * 0.66f, size.height * 0.13f),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(config.accentColor.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "0${page + 1} / 03",
                    style = WalkLogTheme.typography.typography7SB,
                    color = config.accentColor,
                )
            }

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(28.dp),
                        clip = false,
                        ambientColor = config.accentColor.copy(alpha = 0.2f),
                        spotColor = config.accentColor.copy(alpha = 0.3f),
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                WalkLogLottie(
                    resId = config.lottieResId,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f)
                .background(Color.White)
                .padding(horizontal = 28.dp)
                .padding(top = 32.dp),
        ) {
            Text(
                text = config.headline,
                style = WalkLogTheme.typography.subTypography2B,
                color = WalkLogColor.TextPrimary,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = config.subtitle,
                style = WalkLogTheme.typography.typography6R,
                color = WalkLogColor.TextSecondary,
                lineHeight = 22.sp,
            )

            if (page == 1) {
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%,d".format(state.dailyStepGoal),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = WalkLogColor.Primary,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "보",
                        style = WalkLogTheme.typography.typography5M,
                        color = WalkLogColor.Primary,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
                CustomSlider(
                    value = state.dailyStepGoal.toFloat(),
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
                    Text(
                        text = "5,000",
                        style = WalkLogTheme.typography.subTypography12R,
                        color = WalkLogColor.TextSecondary,
                    )
                    Text(
                        text = "20,000",
                        style = WalkLogTheme.typography.subTypography12R,
                        color = WalkLogColor.TextSecondary,
                    )
                }
            }

            if (page == 2) {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(WalkLogColor.Gray50)
                        .border(
                            width = 1.dp,
                            color = WalkLogColor.Gray100,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.notificationsEnabled) "알림 켜기" else "알림 끄기",
                            style = WalkLogTheme.typography.typography6SB,
                            color = WalkLogColor.TextPrimary,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (state.notificationsEnabled) {
                                "맞춤 걷기 알림을 받아요"
                            } else {
                                "설정에서 언제든지 변경할 수 있어요"
                            },
                            style = WalkLogTheme.typography.subTypography12R,
                            color = WalkLogColor.TextSecondary,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = onNotificationsToggled,
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
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingScreenPreview() {
    var state by remember { mutableStateOf(OnboardingState()) }
    WalkLogTheme {
        OnboardingScreen(
            state = state,
            onClickNext = {
                val next = (state.currentPage + 1) % 3
                state = state.copy(currentPage = next)
            },
            onStepGoalChanged = { state = state.copy(dailyStepGoal = it) },
            onNotificationsToggled = { state = state.copy(notificationsEnabled = it) },
        )
    }
}
