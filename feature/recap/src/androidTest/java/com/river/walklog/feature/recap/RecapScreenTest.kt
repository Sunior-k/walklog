package com.river.walklog.feature.recap

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.core.model.DailyStepCount
import com.river.walklog.core.model.MonthlyRecap
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class RecapScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // ─── Loading state ─────────────────────────────────────────────────────

    @Test
    fun loadingState_showsLoadingIndicator() {
        setContent(state = RecapState(isLoading = true, recap = null))

        composeTestRule
            .onNodeWithTag(RecapTestTags.LOADING)
            .assertIsDisplayed()
    }

    @Test
    fun loadingState_doesNotShowRecapContent() {
        setContent(state = RecapState(isLoading = true, recap = null))

        composeTestRule
            .onNodeWithText("3월을\n돌아볼게요")
            .assertDoesNotExist()
    }

    @Test
    fun nullRecap_showsLoadingIndicator_evenWhenIsLoadingIsFalse() {
        setContent(state = RecapState(isLoading = false, recap = null))

        composeTestRule
            .onNodeWithTag(RecapTestTags.LOADING)
            .assertIsDisplayed()
    }

    // ─── Loaded state — Opening slide ──────────────────────────────────────

    @Test
    fun loadedState_showsMonthLabelOnOpeningSlide() {
        setContent(
            state = RecapState(
                isLoading = false,
                recap = monthlyRecap(month = 3),
            ),
        )

        composeTestRule
            .onNodeWithText("3월 리캡")
            .assertIsDisplayed()
    }

    @Test
    fun loadedState_showsOpeningSlideHeadline() {
        setContent(
            state = RecapState(
                isLoading = false,
                recap = monthlyRecap(month = 3),
            ),
        )

        composeTestRule
            .onNodeWithText("3월을\n돌아볼게요")
            .assertIsDisplayed()
    }

    @Test
    fun loadedState_showsWalkingStorySubtitle() {
        setContent(
            state = RecapState(
                isLoading = false,
                recap = monthlyRecap(month = 4),
            ),
        )

        composeTestRule
            .onNodeWithText("걸음으로 만든 4월 이야기")
            .assertIsDisplayed()
    }

    // ─── Close button ──────────────────────────────────────────────────────

    @Test
    fun closeButton_clicked_invokesOnCloseCallback() {
        var closed = false
        setContent(
            state = RecapState(
                isLoading = false,
                recap = monthlyRecap(month = 3),
            ),
            onClose = { closed = true },
        )

        composeTestRule
            .onNodeWithContentDescription("닫기")
            .performClick()

        assertTrue(closed)
    }

    // ─── Helper ────────────────────────────────────────────────────────────

    private fun setContent(
        state: RecapState,
        onClose: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            WalkLogTheme {
                RecapScreen(state = state, onClose = onClose)
            }
        }
    }

    private fun monthlyRecap(
        year: Int = 2025,
        month: Int = 3,
    ) = MonthlyRecap(
        year = year,
        month = month,
        totalSteps = 120_000,
        averageStepsPerDay = 4_000,
        bestDay = DailyStepCount(
            dateEpochDay = LocalDate.of(year, month, 15).toEpochDay(),
            steps = 9_000,
            targetSteps = 6_000,
        ),
        achievedDays = 15,
        totalDays = 30,
        longestStreak = 5,
        activeDays = 20,
        estimatedCalories = 4_800,
        dailyCounts = emptyList(),
    )
}
