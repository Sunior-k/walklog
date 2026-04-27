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
import com.river.walklog.feature.report.model.WeeklyReportArchiveItemUiModel
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
        setContent(state = archiveState())

        composeTestRule
            .onNodeWithText("주간 리포트")
            .assertIsDisplayed()
    }

    @Test
    fun backButton_clicked_invokesOnClickBackCallback() {
        var backClicked = false
        setArchiveContent(state = WeeklyReportArchiveState(), onClickBack = { backClicked = true })

        composeTestRule.onNodeWithContentDescription("뒤로가기").performClick()

        assertTrue(backClicked)
    }

    // ─── Header ────────────────────────────────────────────────────────────

    @Test
    fun weekRangeText_isDisplayed() {
        setDetailContent(state = detailState(dateRangeSubtitle = "4월 7일 — 4월 13일"))

        composeTestRule
            .onNodeWithText("4월 7일 — 4월 13일")
            .assertIsDisplayed()
    }

    @Test
    fun archiveSubtitle_isDisplayed() {
        setContent(state = archiveState())

        composeTestRule
            .onNodeWithText("최근 12주 기록을 모아봤어요")
            .assertIsDisplayed()
    }

    @Test
    fun lockedReport_showsUnlockMessage() {
        setContent(
            state = archiveState(
                items = listOf(
                    archiveItem(isLocked = true, unlockMessage = "4월 20일 00:00부터 볼 수 있어요"),
                ),
            ),
        )

        composeTestRule
            .onNodeWithText("4월 20일 00:00부터 볼 수 있어요")
            .assertIsDisplayed()
    }

    // ─── Share button ──────────────────────────────────────────────────────

    @Test
    fun shareButton_showsShareLabel_whenNotSharing() {
        setDetailContent(state = detailState(isSharing = false))

        composeTestRule
            .onNodeWithText("리포트 공유하기")
            .assertIsDisplayed()
    }

    @Test
    fun shareButton_isEnabled_whenNotSharing() {
        setDetailContent(state = detailState(isSharing = false))

        composeTestRule
            .onNodeWithText("리포트 공유하기")
            .assertIsEnabled()
    }

    @Test
    fun shareButton_showsLoadingLabel_whenSharing() {
        setDetailContent(state = detailState(isSharing = true))

        composeTestRule
            .onNodeWithText("공유 준비 중...")
            .assertIsDisplayed()
    }

    @Test
    fun shareButton_isDisabled_whenSharing() {
        setDetailContent(state = detailState(isSharing = true))

        composeTestRule
            .onNodeWithText("공유 준비 중...")
            .assertIsNotEnabled()
    }

    @Test
    fun shareButton_clicked_invokesOnClickShareCallback() {
        var shareClicked = false
        setDetailContent(
            state = detailState(isSharing = false),
            onClickShare = { shareClicked = true },
        )

        composeTestRule.onNodeWithText("리포트 공유하기").performClick()

        assertTrue(shareClicked)
    }

    // ─── Sharing overlay ───────────────────────────────────────────────────

    @Test
    fun sharingOverlay_isShown_whenIsSharing() {
        setDetailContent(state = detailState(isSharing = true))

        composeTestRule
            .onNodeWithText("공유 이미지를 만드는 중이에요")
            .assertIsDisplayed()
    }

    @Test
    fun sharingOverlay_isHidden_whenNotSharing() {
        setDetailContent(state = detailState(isSharing = false))

        composeTestRule
            .onNodeWithText("공유 이미지를 만드는 중이에요")
            .assertDoesNotExist()
    }

    // ─── Helper ────────────────────────────────────────────────────────────

    private fun setContent(
        state: WeeklyReportArchiveState,
        onClickBack: () -> Unit = {},
        onClickReport: (Long) -> Unit = {},
    ) = setArchiveContent(state, onClickBack, onClickReport)

    private fun setArchiveContent(
        state: WeeklyReportArchiveState,
        onClickBack: () -> Unit = {},
        onClickReport: (Long) -> Unit = {},
    ) {
        composeTestRule.setContent {
            WalkLogTheme {
                WeeklyReportArchiveScreen(
                    state = state,
                    onClickBack = onClickBack,
                    onClickReport = onClickReport,
                )
            }
        }
    }

    private fun setDetailContent(
        state: WeeklyReportDetailState,
        onClickBack: () -> Unit = {},
        onClickShare: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            WalkLogTheme {
                WeeklyReportDetailScreen(
                    state = state,
                    graphicsLayer = rememberGraphicsLayer(),
                    onClickBack = onClickBack,
                    onClickShare = onClickShare,
                )
            }
        }
    }

    private fun archiveState(
        items: List<WeeklyReportArchiveItemUiModel> = listOf(archiveItem()),
    ) = WeeklyReportArchiveState(
        archiveItems = items,
        isLoading = false,
    )

    private fun detailState(
        dateRangeSubtitle: String = "4월 7일 — 4월 13일",
        isSharing: Boolean = false,
    ) = WeeklyReportDetailState(
        dateRangeSubtitle = dateRangeSubtitle,
        weekRangeText = "4월 2주차 · 4/7~4/13",
        totalStepsText = "42,000보",
        achievementRateText = "71%",
        achievedDays = 5,
        totalDays = 7,
        achievementRate = 5f / 7f,
        bestDayText = "수요일",
        bestTimeText = "오후 3시",
        bestStreakText = "3일",
        summaryMessage = "훌륭해요! 목표에 가까워지고 있어요",
        dailyCounts = List(7) { index ->
            com.river.walklog.core.model.DailyStepCount(19_000L + index, 6_000)
        },
        isLoading = false,
        isSharing = isSharing,
    )

    private fun archiveItem(
        isLocked: Boolean = false,
        unlockMessage: String = "",
    ) = WeeklyReportArchiveItemUiModel(
        weekStartEpochDay = 19_000L,
        weekRangeText = "4월 2주차",
        dateRangeText = "4/7~4/13",
        totalStepsText = "42,000보",
        achievementRateText = "71%",
        achievementRate = 0.71f,
        isLocked = isLocked,
        unlockMessage = unlockMessage,
    )
}
