package com.river.walklog.feature.forecast

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ForecastDetailBottomSheetTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // ─── Header ────────────────────────────────────────────────────────────

    @Test
    fun title_isDisplayed() {
        setContent(state = ForecastDetailState(title = "걷기 예보"))

        composeTestRule
            .onNodeWithText("걷기 예보")
            .assertIsDisplayed()
    }

    @Test
    fun recommendedTimeText_isDisplayed() {
        setContent(state = ForecastDetailState(recommendedTimeText = "오늘 오후 3시"))

        composeTestRule
            .onNodeWithText("오늘 오후 3시")
            .assertIsDisplayed()
    }

    @Test
    fun todayRecommendedTimeBadge_isDisplayed() {
        setContent(state = ForecastDetailState())

        composeTestRule
            .onNodeWithText("오늘의 추천 시간")
            .assertIsDisplayed()
    }

    // ─── Description card ──────────────────────────────────────────────────

    @Test
    fun whyThisTimeSectionTitle_isDisplayed() {
        setContent(state = ForecastDetailState())

        composeTestRule
            .onNodeWithText("왜 이 시간대를 추천하나요?")
            .assertIsDisplayed()
    }

    @Test
    fun descriptionText_isDisplayed() {
        setContent(state = ForecastDetailState(description = "평소 이 시간대에 가장 많이 걷고 있어요."))

        composeTestRule
            .onNodeWithText("평소 이 시간대에 가장 많이 걷고 있어요.")
            .assertIsDisplayed()
    }

    // ─── Pattern card ──────────────────────────────────────────────────────

    @Test
    fun recentSevenDayPatternSectionTitle_isDisplayed() {
        setContent(state = ForecastDetailState())

        composeTestRule
            .onNodeWithText("최근 7일 패턴")
            .assertIsDisplayed()
    }

    @Test
    fun averageStepsAtThisTime_isDisplayed() {
        setContent(state = ForecastDetailState(averageStepsAtThisTimeText = "평균 1,240보"))

        composeTestRule
            .onNodeWithText("평균 1,240보")
            .assertIsDisplayed()
    }

    @Test
    fun activeDaysText_isDisplayed() {
        setContent(state = ForecastDetailState(activeDaysText = "최근 7일 중 5일"))

        composeTestRule
            .onNodeWithText("최근 7일 중 5일")
            .assertIsDisplayed()
    }

    @Test
    fun bestPatternText_isDisplayed() {
        setContent(state = ForecastDetailState(bestPatternText = "평일 오후 시간대"))

        composeTestRule
            .onNodeWithText("평일 오후 시간대")
            .assertIsDisplayed()
    }

    // ─── Start walking button ──────────────────────────────────────────────

    @Test
    fun startWalkingButton_isDisplayed() {
        setContent(state = ForecastDetailState())

        composeTestRule
            .onNodeWithText("지금 걸으러 가기")
            .assertIsDisplayed()
    }

    @Test
    fun startWalkingButton_clicked_invokesOnClickStartWalkingCallback() {
        var clicked = false
        setContent(
            state = ForecastDetailState(),
            onClickStartWalking = { clicked = true },
        )

        composeTestRule.onNodeWithText("지금 걸으러 가기").performClick()

        assertTrue(clicked)
    }

    // ─── Helper ────────────────────────────────────────────────────────────

    private fun setContent(
        state: ForecastDetailState,
        onDismiss: () -> Unit = {},
        onClickStartWalking: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            WalkLogTheme {
                ForecastDetailBottomSheet(
                    state = state,
                    onDismiss = onDismiss,
                    onClickStartWalking = onClickStartWalking,
                )
            }
        }
    }
}
