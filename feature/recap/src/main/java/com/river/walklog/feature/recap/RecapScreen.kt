package com.river.walklog.feature.recap

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.river.walklog.core.designsystem.R
import com.river.walklog.core.designsystem.foundation.RecapColors
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.MonthlyRecap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun RecapScreen(
    state: RecapState,
    onClose: () -> Unit,
    initialPage: Int = 0,
    autoAdvance: Boolean = true,
) {
    if (state.isLoading || state.recap == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RecapColors.LoadingBackground)
                .testTag(RecapTestTags.LOADING),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = WalkLogColor.StaticWhite)
        }
        return
    }

    RecapPager(
        recap = state.recap,
        onClose = onClose,
        initialPage = initialPage,
        autoAdvance = autoAdvance,
    )
}

private const val SLIDE_COUNT = 8
private const val SLIDE_DURATION_MS = 5_000L

private val TransitionSpec = tween<Float>(durationMillis = 500, easing = FastOutSlowInEasing)

@Composable
private fun RecapPager(
    recap: MonthlyRecap,
    onClose: () -> Unit,
    initialPage: Int,
    autoAdvance: Boolean,
) {
    val startPage = initialPage.coerceIn(0, SLIDE_COUNT - 1)
    val pagerState = rememberPagerState(initialPage = startPage, pageCount = { SLIDE_COUNT })
    val scope = rememberCoroutineScope()
    var progressState by remember(startPage) { mutableStateOf(startPage to 0f) }
    var isPaused by remember { mutableStateOf(false) }

    var screenVisible by remember { mutableStateOf(false) }
    val screenAlpha by animateFloatAsState(
        targetValue = if (screenVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "screen_alpha",
    )
    LaunchedEffect(Unit) { screenVisible = true }

    LaunchedEffect(pagerState.settledPage) {
        if (!autoAdvance) return@LaunchedEffect

        val page = pagerState.settledPage
        progressState = page to 0f // page + progress 동시 리셋
        var elapsed = 0L
        while (elapsed < SLIDE_DURATION_MS) {
            delay(16L)
            if (!pagerState.isScrollInProgress && !isPaused) {
                elapsed += 16L
                progressState = page to (elapsed.toFloat() / SLIDE_DURATION_MS).coerceIn(0f, 1f)
            }
        }
        if (page < SLIDE_COUNT - 1) {
            pagerState.animateScrollToPage(page = page + 1, animationSpec = TransitionSpec)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = screenAlpha },
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val offset = (page - pagerState.currentPage) -
                            pagerState.currentPageOffsetFraction
                        translationX = -offset * size.width
                        alpha = (1f - abs(offset)).coerceIn(0f, 1f)
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { tapOffset ->
                                scope.launch {
                                    if (tapOffset.x > size.width / 2) {
                                        if (pagerState.currentPage < SLIDE_COUNT - 1) {
                                            pagerState.animateScrollToPage(
                                                page = pagerState.currentPage + 1,
                                                animationSpec = TransitionSpec,
                                            )
                                        }
                                    } else {
                                        if (pagerState.currentPage > 0) {
                                            pagerState.animateScrollToPage(
                                                page = pagerState.currentPage - 1,
                                                animationSpec = TransitionSpec,
                                            )
                                        }
                                    }
                                }
                            },
                        )
                    },
            ) {
                when (page) {
                    0 -> OpeningSlide(recap)
                    1 -> TotalStepsSlide(recap)
                    2 -> AverageStepsSlide(recap)
                    3 -> CaloriesSlide(recap)
                    4 -> AchievementSlide(recap)
                    5 -> BestDaySlide(recap)
                    6 -> StreakSlide(recap)
                    7 -> PersonaSlide(recap)
                }
            }
        }

        StoryProgressBar(
            totalSlides = SLIDE_COUNT,
            currentSlide = progressState.first,
            currentSlideProgress = progressState.second,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
        )

        IconButton(
            onClick = { isPaused = !isPaused },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 12.dp)
                .padding(end = 44.dp),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = if (isPaused) R.drawable.ic_play else R.drawable.ic_pause),
                contentDescription = if (isPaused) "재생" else "일시정지",
                tint = WalkLogColor.StaticWhite.copy(alpha = 0.8f),
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 12.dp)
                .padding(end = 4.dp),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                contentDescription = "닫기",
                tint = WalkLogColor.StaticWhite.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun StoryProgressBar(
    totalSlides: Int,
    currentSlide: Int,
    currentSlideProgress: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(totalSlides) { index ->
            val fraction = when {
                index < currentSlide -> 1f
                index == currentSlide -> currentSlideProgress
                else -> 0f
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(WalkLogColor.StaticWhite.copy(alpha = 0.3f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(3.dp)
                        .background(WalkLogColor.StaticWhite),
                )
            }
        }
    }
}

@Composable
private fun SlideScaffold(
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 56.dp, bottom = 32.dp, start = 32.dp, end = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
private fun AnimatedCounter(
    target: Int,
    style: TextStyle,
    color: Color,
    format: (Int) -> String = { "%,d".format(it) },
) {
    var displayed by remember(target) { mutableIntStateOf(0) }

    LaunchedEffect(target) {
        val steps = 60
        val stepDelay = 1_200L / steps
        for (i in 1..steps) {
            delay(stepDelay)
            displayed = target * i / steps
        }
        displayed = target
    }

    Text(
        text = format(displayed),
        style = style,
        color = color,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun SlideLabel(text: String, color: Color = WalkLogColor.StaticWhite.copy(alpha = 0.7f)) {
    Text(
        text = text,
        style = WalkLogTheme.typography.typography6M,
        color = color,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun SlideDescription(
    text: String,
    color: Color = WalkLogColor.StaticWhite.copy(alpha = 0.85f),
) {
    Text(
        text = text,
        style = WalkLogTheme.typography.typography6M,
        color = color,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun OpeningSlide(recap: MonthlyRecap) {
    SlideScaffold(
        gradientColors = listOf(RecapColors.OpeningGradientStart, RecapColors.OpeningGradientEnd),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "${recap.monthLabel} 리캡",
                style = WalkLogTheme.typography.typography5SB,
                color = WalkLogColor.StaticWhite.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "이번 달을\n돌아볼게요",
                style = WalkLogTheme.typography.typography1B,
                color = WalkLogColor.StaticWhite,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "걸음으로 만든 ${recap.monthLabel} 이야기",
                style = WalkLogTheme.typography.typography6M,
                color = WalkLogColor.StaticWhite.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TotalStepsSlide(recap: MonthlyRecap) {
    SlideScaffold(
        gradientColors = listOf(
            RecapColors.TotalStepsGradientStart,
            RecapColors.TotalStepsGradientEnd,
        ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(WalkLogColor.StaticWhite.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_footprint),
                    contentDescription = "걸음 아이콘",
                    tint = WalkLogColor.StaticWhite,
                    modifier = Modifier.size(32.dp),
                )
            }
            SlideLabel("이번 달 총 걸음 수")
            AnimatedCounter(
                target = recap.totalSteps,
                style = WalkLogTheme.typography.typography1B,
                color = WalkLogColor.StaticWhite,
                format = { "%,d보".format(it) },
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(WalkLogColor.StaticWhite.copy(alpha = 0.15f))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                SlideDescription(
                    "약 ${recap.distanceKm}km를 이동한 거리예요",
                    color = WalkLogColor.StaticWhite,
                )
            }
        }
    }
}

@Composable
private fun AverageStepsSlide(recap: MonthlyRecap) {
    SlideScaffold(
        gradientColors = listOf(
            RecapColors.AverageStepsGradientStart,
            RecapColors.AverageStepsGradientEnd,
        ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SlideLabel("하루 평균 걸음 수")
            AnimatedCounter(
                target = recap.averageStepsPerDay,
                style = WalkLogTheme.typography.typography1B,
                color = WalkLogColor.StaticWhite,
                format = { "%,d보".format(it) },
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(WalkLogColor.StaticWhite.copy(alpha = 0.15f))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                SlideDescription(
                    "${recap.activeDays}일 동안 걸은 기록으로 계산했어요",
                    color = WalkLogColor.StaticWhite,
                )
            }
        }
    }
}

@Composable
private fun CaloriesSlide(recap: MonthlyRecap) {
    SlideScaffold(
        gradientColors = listOf(RecapColors.CaloriesGradientStart, RecapColors.CaloriesGradientEnd),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(WalkLogColor.StaticWhite.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_fire),
                    contentDescription = "칼로리 아이콘",
                    tint = WalkLogColor.StaticWhite,
                    modifier = Modifier.size(32.dp),
                )
            }
            SlideLabel("이번 달 소모한 칼로리")
            AnimatedCounter(
                target = recap.estimatedCalories,
                style = WalkLogTheme.typography.typography1B,
                color = WalkLogColor.StaticWhite,
                format = { "%,dkcal".format(it) },
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(WalkLogColor.StaticWhite.copy(alpha = 0.15f))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                SlideDescription(
                    recap.caloriesFoodComparison + "을 태웠어요",
                    color = WalkLogColor.StaticWhite,
                )
            }
        }
    }
}

@Composable
private fun AchievementSlide(recap: MonthlyRecap) {
    val pct = (recap.achievementRate * 100).toInt()

    SlideScaffold(
        gradientColors = listOf(
            RecapColors.AchievementGradientStart,
            RecapColors.AchievementGradientEnd,
        ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SlideLabel("목표를 달성한 날")
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
            ) {
                AnimatedCounter(
                    target = recap.achievedDays,
                    style = WalkLogTheme.typography.typography1B,
                    color = WalkLogColor.StaticWhite,
                    format = { it.toString() },
                )
                Text(
                    text = "일",
                    style = WalkLogTheme.typography.typography2B,
                    color = WalkLogColor.StaticWhite.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(WalkLogColor.StaticWhite.copy(alpha = 0.15f))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                SlideDescription(
                    "${recap.totalDays}일 중 ${recap.achievedDays}일 달성 ($pct%)",
                    color = WalkLogColor.StaticWhite,
                )
            }
            AchievementCalendar(dailyCounts = recap.dailyCounts)
        }
    }
}

@Composable
private fun AchievementCalendar(
    dailyCounts: List<DailyStepCount>,
) {
    val chunked = dailyCounts.chunked(7)
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        chunked.forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (day.isAchieved) {
                                    WalkLogColor.StaticWhite
                                } else {
                                    WalkLogColor.StaticWhite.copy(alpha = 0.2f)
                                },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun BestDaySlide(recap: MonthlyRecap) {
    SlideScaffold(
        gradientColors = listOf(RecapColors.BestDayGradientStart, RecapColors.BestDayGradientEnd),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SlideLabel("가장 많이 걸은 날")
            val bestDay = recap.bestDay
            if (bestDay != null) {
                Text(
                    text = recap.bestDayDateText,
                    style = WalkLogTheme.typography.typography1B,
                    color = WalkLogColor.StaticWhite,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                BestDayProgress(steps = bestDay.steps, targetSteps = bestDay.targetSteps)
                Spacer(modifier = Modifier.height(8.dp))
                BestDayMetrics(steps = bestDay.steps)
            } else {
                Text(
                    text = "아직 기록이 없어요",
                    style = WalkLogTheme.typography.typography1B,
                    color = WalkLogColor.StaticWhite.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun BestDayProgress(steps: Int, targetSteps: Int) {
    var progressStarted by remember(steps, targetSteps) { mutableStateOf(false) }
    val targetProgress = if (targetSteps > 0) {
        (steps.toFloat() / targetSteps).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = if (progressStarted) targetProgress else 0f,
        animationSpec = tween(durationMillis = 1_200, easing = FastOutSlowInEasing),
        label = "best_day_progress",
    )

    LaunchedEffect(steps, targetSteps) {
        progressStarted = true
    }

    Box(
        modifier = Modifier.size(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = RecapColors.BestDayProgress.copy(alpha = 0.9f),
            trackColor = WalkLogColor.StaticWhite.copy(alpha = 0.18f),
            strokeWidth = 12.dp,
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SlideDescription("걸음 수", color = WalkLogColor.StaticWhite.copy(alpha = 0.72f))
            AnimatedCounter(
                target = steps,
                style = WalkLogTheme.typography.typography1B,
                color = WalkLogColor.StaticWhite,
                format = { "%,d보".format(it) },
            )
        }
    }
}

@Composable
private fun BestDayMetrics(steps: Int) {
    val calories = (steps * 0.04f).toInt()
    val distanceKm = steps * 0.00075f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BestDayMetricBox(
            label = "칼로리",
            value = "%,dkcal".format(calories),
            modifier = Modifier.weight(1f),
        )
        BestDayMetricBox(
            label = "거리",
            value = "%.1fkm".format(distanceKm),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun BestDayMetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(WalkLogColor.StaticWhite.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = WalkLogTheme.typography.typography7R,
                color = WalkLogColor.StaticWhite.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = value,
                style = WalkLogTheme.typography.typography3B,
                color = WalkLogColor.StaticWhite,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun StreakSlide(recap: MonthlyRecap) {
    SlideScaffold(
        gradientColors = listOf(RecapColors.StreakGradientStart, RecapColors.StreakGradientEnd),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(WalkLogColor.StaticWhite.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_fire),
                    contentDescription = "칼로리 아이콘",
                    tint = WalkLogColor.StaticWhite,
                    modifier = Modifier.size(32.dp),
                )
            }
            SlideLabel("최장 연속 목표 달성")
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
            ) {
                AnimatedCounter(
                    target = recap.longestStreak,
                    style = WalkLogTheme.typography.typography1B,
                    color = WalkLogColor.StaticWhite,
                    format = { it.toString() },
                )
                Text(
                    text = "일",
                    style = WalkLogTheme.typography.typography2B,
                    color = WalkLogColor.StaticWhite.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            StreakCheckGrid(streakDays = recap.longestStreak)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(WalkLogColor.StaticWhite.copy(alpha = 0.15f))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                SlideDescription(
                    text = when {
                        recap.longestStreak >= 14 -> "2주 이상 쉬지 않았어요! 놀라워요 🎉"
                        recap.longestStreak >= 7 -> "일주일 연속 달성! 대단해요!"
                        recap.longestStreak >= 3 -> "3일 이상 연속 달성했어요"
                        recap.longestStreak > 0 -> "이 기세로 계속 이어가요"
                        else -> "내달엔 연속 달성에 도전해봐요!"
                    },
                    color = WalkLogColor.StaticWhite,
                )
            }
        }
    }
}

@Composable
private fun StreakCheckGrid(streakDays: Int) {
    val displayDays = if (streakDays > 0) streakDays else 7
    val rows = List(size = (displayDays + 6) / 7) { rowIndex ->
        val start = rowIndex * 7
        val end = minOf(start + 7, displayDays)
        start until end
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { dayIndex ->
                    StreakCheckTile(checked = dayIndex < streakDays)
                }
            }
        }
    }
}

@Composable
private fun StreakCheckTile(checked: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (checked) {
                    WalkLogColor.Primary.copy(alpha = 0.92f)
                } else {
                    WalkLogColor.StaticWhite.copy(alpha = 0.16f)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Canvas(modifier = Modifier.size(15.dp)) {
                val stroke = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                )
                drawLine(
                    color = RecapColors.StreakGradientStart,
                    start = center.copy(x = size.width * 0.12f, y = size.height * 0.52f),
                    end = center.copy(x = size.width * 0.42f, y = size.height * 0.82f),
                    strokeWidth = stroke.width,
                    cap = stroke.cap,
                )
                drawLine(
                    color = RecapColors.StreakGradientStart,
                    start = center.copy(x = size.width * 0.42f, y = size.height * 0.82f),
                    end = center.copy(x = size.width * 0.9f, y = size.height * 0.18f),
                    strokeWidth = stroke.width,
                    cap = stroke.cap,
                )
            }
        }
    }
}

@Composable
private fun PersonaSlide(recap: MonthlyRecap) {
    SlideScaffold(
        gradientColors = listOf(RecapColors.PersonaGradientStart, RecapColors.PersonaGradientEnd),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "이번 달 당신은",
                style = WalkLogTheme.typography.typography5SB,
                color = WalkLogColor.StaticWhite.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = recap.walkerPersona,
                style = WalkLogTheme.typography.typography1B,
                color = RecapColors.PersonaAccent,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(WalkLogColor.StaticWhite.copy(alpha = 0.08f))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                Text(
                    text = recap.walkerPersonaDescription,
                    style = WalkLogTheme.typography.typography6M,
                    color = WalkLogColor.StaticWhite.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "다음 달도 함께해요! 🚶",
                style = WalkLogTheme.typography.typography6SB,
                color = WalkLogColor.StaticWhite.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(name = "01", showBackground = true)
@Composable
private fun RecapOpeningPreview() {
    PreviewRecapScreen(initialPage = 6)
}

@Composable
private fun PreviewRecapScreen(initialPage: Int) {
    WalkLogTheme {
        RecapScreen(
            state = RecapState(
                isLoading = false,
                recap = previewRecap,
            ),
            onClose = {},
            initialPage = initialPage,
            autoAdvance = false,
        )
    }
}

private val previewRecap = MonthlyRecap(
    year = 2025,
    month = 4,
    totalSteps = 182_400,
    averageStepsPerDay = 6_080,
    bestDay = DailyStepCount(
        dateEpochDay = java.time.LocalDate.of(2025, 4, 15).toEpochDay(),
        steps = 12_340,
        targetSteps = 6_000,
    ),
    achievedDays = 18,
    totalDays = 30,
    longestStreak = 7,
    activeDays = 24,
    estimatedCalories = 7_296,
    dailyCounts = emptyList(),
)
