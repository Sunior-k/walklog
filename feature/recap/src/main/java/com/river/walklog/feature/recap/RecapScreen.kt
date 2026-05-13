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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
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
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import com.river.walklog.feature.recap.R as RecapR

@Composable
fun RecapScreen(
    state: RecapState,
    onClose: () -> Unit,
    initialPage: Int = 0,
    autoAdvance: Boolean = true,
) {
    if (state.isError) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RecapColors.LoadingBackground),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(RecapR.string.recap_error),
                    style = WalkLogTheme.typography.typography4SB,
                    color = WalkLogColor.StaticWhite,
                    textAlign = TextAlign.Center,
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                        contentDescription = stringResource(RecapR.string.action_close),
                        tint = WalkLogColor.StaticWhite,
                    )
                }
            }
        }
        return
    }

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

@Composable
private fun rememberIntegerFormat(): NumberFormat {
    val locale = LocalConfiguration.current.locales[0]
    return remember(locale) { NumberFormat.getIntegerInstance(locale) }
}

@Composable
private fun rememberDecimalFormat(
    minimumFractionDigits: Int,
    maximumFractionDigits: Int,
): NumberFormat {
    val locale = LocalConfiguration.current.locales[0]
    return remember(locale, minimumFractionDigits, maximumFractionDigits) {
        NumberFormat.getNumberInstance(locale).apply {
            this.minimumFractionDigits = minimumFractionDigits
            this.maximumFractionDigits = maximumFractionDigits
        }
    }
}

@Composable
private fun MonthlyRecap.monthLabelText(): String {
    val locale = LocalConfiguration.current.locales[0]
    return LocalDate.of(year, month, 1)
        .format(DateTimeFormatter.ofPattern("MMMM", locale))
}

@Composable
private fun DailyStepCount.bestDayDateText(): String {
    val locale = LocalConfiguration.current.locales[0]
    val pattern = stringResource(RecapR.string.recap_best_day_date_format_pattern)
    return LocalDate.ofEpochDay(dateEpochDay)
        .format(DateTimeFormatter.ofPattern(pattern, locale))
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
                contentDescription = if (isPaused) stringResource(RecapR.string.action_play) else stringResource(RecapR.string.action_pause),
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
                contentDescription = stringResource(RecapR.string.action_close),
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
    format: (Int) -> String = { it.toString() },
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
    val monthLabel = recap.monthLabelText()

    SlideScaffold(
        gradientColors = listOf(RecapColors.OpeningGradientStart, RecapColors.OpeningGradientEnd),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(RecapR.string.recap_title, monthLabel),
                style = WalkLogTheme.typography.typography5SB,
                color = WalkLogColor.StaticWhite.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(RecapR.string.recap_opening_body, monthLabel),
                style = WalkLogTheme.typography.typography1B,
                color = WalkLogColor.StaticWhite,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(RecapR.string.recap_opening_subtitle, monthLabel),
                style = WalkLogTheme.typography.typography6M,
                color = WalkLogColor.StaticWhite.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TotalStepsSlide(recap: MonthlyRecap) {
    val numberFormat = rememberIntegerFormat()
    val monthLabel = recap.monthLabelText()
    val stepsFmt = stringResource(RecapR.string.steps_count_format)

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
                    contentDescription = stringResource(RecapR.string.icon_desc_step),
                    tint = WalkLogColor.StaticWhite,
                    modifier = Modifier.size(32.dp),
                )
            }
            SlideLabel(stringResource(RecapR.string.recap_total_steps_label, monthLabel))
            AnimatedCounter(
                target = recap.totalSteps,
                style = WalkLogTheme.typography.typography1B,
                color = WalkLogColor.StaticWhite,
                format = { stepsFmt.format(numberFormat.format(it)) },
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(WalkLogColor.StaticWhite.copy(alpha = 0.15f))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                SlideDescription(
                    stringResource(RecapR.string.recap_total_distance, (recap.totalSteps * 0.00075f).toInt()),
                    color = WalkLogColor.StaticWhite,
                )
            }
        }
    }
}

@Composable
private fun AverageStepsSlide(recap: MonthlyRecap) {
    val numberFormat = rememberIntegerFormat()
    val stepsFmt = stringResource(RecapR.string.steps_count_format)

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
            SlideLabel(stringResource(RecapR.string.recap_avg_steps_label))
            AnimatedCounter(
                target = recap.averageStepsPerDay,
                style = WalkLogTheme.typography.typography1B,
                color = WalkLogColor.StaticWhite,
                format = { stepsFmt.format(numberFormat.format(it)) },
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(WalkLogColor.StaticWhite.copy(alpha = 0.15f))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                SlideDescription(
                    stringResource(RecapR.string.recap_avg_steps_desc, recap.activeDays),
                    color = WalkLogColor.StaticWhite,
                )
            }
        }
    }
}

@Composable
private fun CaloriesSlide(recap: MonthlyRecap) {
    val numberFormat = rememberIntegerFormat()
    val monthLabel = recap.monthLabelText()

    SlideScaffold(
        gradientColors = listOf(RecapColors.CaloriesGradientStart, RecapColors.CaloriesGradientEnd),
    ) {
        val caloriesText = when {
            recap.estimatedCalories >= 500 -> stringResource(RecapR.string.calories_comparison_burger, recap.estimatedCalories / 500)
            recap.estimatedCalories >= 200 -> stringResource(RecapR.string.calories_comparison_icecream, recap.estimatedCalories / 200)
            else -> "${numberFormat.format(recap.estimatedCalories)}kcal"
        }
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
                    contentDescription = stringResource(RecapR.string.icon_desc_calories),
                    tint = WalkLogColor.StaticWhite,
                    modifier = Modifier.size(32.dp),
                )
            }
            SlideLabel(stringResource(RecapR.string.recap_calories_label, monthLabel))
            AnimatedCounter(
                target = recap.estimatedCalories,
                style = WalkLogTheme.typography.typography1B,
                color = WalkLogColor.StaticWhite,
                format = { "${numberFormat.format(it)}kcal" },
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(WalkLogColor.StaticWhite.copy(alpha = 0.15f))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                SlideDescription(
                    text = stringResource(RecapR.string.recap_calories_burned, caloriesText),
                    color = WalkLogColor.StaticWhite,
                )
            }
        }
    }
}

@Composable
private fun AchievementSlide(recap: MonthlyRecap) {
    val pct = if (recap.totalDays == 0) 0 else (recap.achievedDays * 100 / recap.totalDays)

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
            SlideLabel(stringResource(RecapR.string.recap_achievement_label))
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
                    text = stringResource(RecapR.string.day_suffix),
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
                    stringResource(RecapR.string.recap_achievement_desc, recap.totalDays, recap.achievedDays, pct),
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
                                if (day.steps >= day.targetSteps) {
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
            SlideLabel(stringResource(RecapR.string.recap_best_day_label))
            val bestDay = recap.bestDay
            if (bestDay != null) {
                Text(
                    text = bestDay.bestDayDateText(),
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
                    text = stringResource(RecapR.string.recap_best_day_no_record),
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
    val numberFormat = rememberIntegerFormat()
    val stepsFmt = stringResource(RecapR.string.steps_count_format)
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
            SlideDescription(stringResource(RecapR.string.recap_step_count_label), color = WalkLogColor.StaticWhite.copy(alpha = 0.72f))
            AnimatedCounter(
                target = steps,
                style = WalkLogTheme.typography.typography1B,
                color = WalkLogColor.StaticWhite,
                format = { stepsFmt.format(numberFormat.format(it)) },
            )
        }
    }
}

@Composable
private fun BestDayMetrics(steps: Int) {
    val numberFormat = rememberIntegerFormat()
    val distanceFormat = rememberDecimalFormat(minimumFractionDigits = 1, maximumFractionDigits = 1)
    val calories = (steps * 0.04f).toInt()
    val distanceKm = steps * 0.00075f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BestDayMetricBox(
            label = stringResource(RecapR.string.recap_calories_unit_label),
            value = "${numberFormat.format(calories)}kcal",
            modifier = Modifier.weight(1f),
        )
        BestDayMetricBox(
            label = stringResource(RecapR.string.recap_distance_unit_label),
            value = "${distanceFormat.format(distanceKm)}km",
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
                    contentDescription = stringResource(RecapR.string.icon_desc_calories),
                    tint = WalkLogColor.StaticWhite,
                    modifier = Modifier.size(32.dp),
                )
            }
            SlideLabel(stringResource(RecapR.string.recap_streak_label))
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
                    text = stringResource(RecapR.string.day_suffix),
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
                        recap.longestStreak >= 14 -> stringResource(RecapR.string.recap_streak_14_plus)
                        recap.longestStreak >= 7 -> stringResource(RecapR.string.recap_streak_7_plus)
                        recap.longestStreak >= 3 -> stringResource(RecapR.string.recap_streak_3_plus)
                        recap.longestStreak > 0 -> stringResource(RecapR.string.recap_streak_positive)
                        else -> stringResource(RecapR.string.recap_streak_zero)
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
    val monthLabel = recap.monthLabelText()

    SlideScaffold(
        gradientColors = listOf(RecapColors.PersonaGradientStart, RecapColors.PersonaGradientEnd),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(RecapR.string.recap_persona_label, monthLabel),
                style = WalkLogTheme.typography.typography5SB,
                color = WalkLogColor.StaticWhite.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(recap.walkerPersonaRes()),
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
                    text = stringResource(recap.walkerPersonaDescRes()),
                    style = WalkLogTheme.typography.typography6M,
                    color = WalkLogColor.StaticWhite.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(RecapR.string.recap_persona_closing),
                style = WalkLogTheme.typography.typography6SB,
                color = WalkLogColor.StaticWhite.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
