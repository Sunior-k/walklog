package com.river.walklog.feature.report

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeeklyReportScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // ─── TopBar ────────────────────────────────────────────────────────────

    @Test
    fun topBar_showsWeeklyReportTitle() {
        setContent(state = WeeklyReportState())

        composeTestRule
            .onNodeWithText("주간 리포트")
            .assertIsDisplayed()
    }

    @Test
    fun backButton_clicked_invokesOnClickBackCallback() {
        var backClicked = false
        setContent(state = WeeklyReportState(), onClickBack = { backClicked = true })

        composeTestRule.onNodeWithContentDescription("뒤로가기").performClick()

        assertTrue(backClicked)
    }

    // ─── Header ────────────────────────────────────────────────────────────

    @Test
    fun weekRangeText_isDisplayed() {
        setContent(state = WeeklyReportState(weekRangeText = "4월 2주차 · 4/7~4/13"))

        composeTestRule
            .onNodeWithText("4월 2주차 · 4/7~4/13")
            .assertIsDisplayed()
    }

    @Test
    fun summaryMessage_isDisplayed() {
        setContent(state = WeeklyReportState(summaryMessage = "지난주보다 12% 더 걸었어요"))

        composeTestRule
            .onNodeWithText("지난주보다 12% 더 걸었어요")
            .assertIsDisplayed()
    }

    @Test
    fun detailDescription_isDisplayed() {
        setContent(state = WeeklyReportState(detailDescription = "한 주 동안 꾸준히 걸으며 목표에 가까워졌어요."))

        composeTestRule
            .onNodeWithText("한 주 동안 꾸준히 걸으며 목표에 가까워졌어요.")
            .assertIsDisplayed()
    }

    // ─── Share button ──────────────────────────────────────────────────────

    @Test
    fun shareButton_showsShareLabel_whenNotSharing() {
        setContent(state = WeeklyReportState(isSharing = false))

        composeTestRule
            .onNodeWithText("리포트 공유하기")
            .assertIsDisplayed()
    }

    @Test
    fun shareButton_isEnabled_whenNotSharing() {
        setContent(state = WeeklyReportState(isSharing = false))

        composeTestRule
            .onNodeWithText("리포트 공유하기")
            .assertIsEnabled()
    }

    @Test
    fun shareButton_showsLoadingLabel_whenSharing() {
        setContent(state = WeeklyReportState(isSharing = true))

        composeTestRule
            .onNodeWithText("공유 준비 중...")
            .assertIsDisplayed()
    }

    @Test
    fun shareButton_isDisabled_whenSharing() {
        setContent(state = WeeklyReportState(isSharing = true))

        composeTestRule
            .onNodeWithText("공유 준비 중...")
            .assertIsNotEnabled()
    }

    @Test
    fun shareButton_clicked_invokesOnClickShareCallback() {
        var shareClicked = false
        setContent(
            state = WeeklyReportState(isSharing = false),
            onClickShare = { shareClicked = true },
        )

        composeTestRule.onNodeWithText("리포트 공유하기").performClick()

        assertTrue(shareClicked)
    }

    // ─── Sharing overlay ───────────────────────────────────────────────────

    @Test
    fun sharingOverlay_isShown_whenIsSharing() {
        setContent(state = WeeklyReportState(isSharing = true))

        composeTestRule
            .onNodeWithText("공유 이미지를 만드는 중이에요")
            .assertIsDisplayed()
    }

    @Test
    fun sharingOverlay_isHidden_whenNotSharing() {
        setContent(state = WeeklyReportState(isSharing = false))

        composeTestRule
            .onNodeWithText("공유 이미지를 만드는 중이에요")
            .assertDoesNotExist()
    }

    // ─── Helper ────────────────────────────────────────────────────────────

    private fun setContent(
        state: WeeklyReportState,
        onClickBack: () -> Unit = {},
        onClickShare: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            WalkLogTheme {
                WeeklyReportScreen(
                    state = state,
                    graphicsLayer = rememberGraphicsLayer(),
                    onClickBack = onClickBack,
                    onClickShare = onClickShare,
                )
            }
        }
    }
}
