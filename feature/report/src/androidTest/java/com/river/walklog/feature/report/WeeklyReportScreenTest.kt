package com.river.walklog.feature.report

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class WeeklyReportScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity get() = composeTestRule.activity

    // topbar

    @Test
    fun topBar_showsWeeklyReportTitle() {
        setContent(state = archiveState())

        composeTestRule
            .onNodeWithText(activity.getString(R.string.report_title))
            .assertIsDisplayed()
    }

    @Test
    fun backButton_clicked_invokesOnClickBackCallback() {
        var backClicked = false
        setArchiveContent(state = WeeklyReportArchiveState(), onClickBack = { backClicked = true })

        composeTestRule
            .onNodeWithContentDescription(activity.getString(R.string.back_button_cd))
            .performClick()

        assertTrue(backClicked)
    }

    // header

    @Test
    fun weekRangeText_isDisplayed() {
        val weekStart = LocalDate.of(2026, 4, 7)
        val weekEnd = weekStart.plusDays(6)
        setDetailContent(state = detailState(weekStart = weekStart))

        composeTestRule
            .onNodeWithText("${weekStart.format(fullDateFormatter())} — ${weekEnd.format(fullDateFormatter())}")
            .assertIsDisplayed()
    }

    @Test
    fun archiveSubtitle_isDisplayed() {
        setContent(state = archiveState())

        composeTestRule
            .onNodeWithText(activity.getString(R.string.report_archive_subtitle))
            .assertIsDisplayed()
    }

    @Test
    fun lockedReport_showsUnlockMessage() {
        val unlockDate = LocalDate.of(2026, 4, 20)
        setContent(
            state = archiveState(
                items = listOf(
                    archiveItem(isLocked = true, unlockDate = unlockDate),
                ),
            ),
        )

        composeTestRule
            .onNodeWithText(
                activity.getString(
                    R.string.report_unlock_from,
                    unlockDate.format(unlockDateFormatter()),
                ),
            )
            .assertIsDisplayed()
    }

    // share button

    @Test
    fun shareButton_showsShareLabel_whenNotSharing() {
        setDetailContent(state = detailState(isSharing = false))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.report_share_button))
            .assertIsDisplayed()
    }

    @Test
    fun shareButton_isEnabled_whenNotSharing() {
        setDetailContent(state = detailState(isSharing = false))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.report_share_button))
            .assertIsEnabled()
    }

    @Test
    fun shareButton_showsLoadingLabel_whenSharing() {
        setDetailContent(state = detailState(isSharing = true))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.report_share_button_sharing))
            .assertIsDisplayed()
    }

    @Test
    fun shareButton_isDisabled_whenSharing() {
        setDetailContent(state = detailState(isSharing = true))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.report_share_button_sharing))
            .assertIsNotEnabled()
    }

    @Test
    fun shareButton_clicked_invokesOnClickShareCallback() {
        var shareClicked = false
        setDetailContent(
            state = detailState(isSharing = false),
            onClickShare = { shareClicked = true },
        )

        composeTestRule
            .onNodeWithText(activity.getString(R.string.report_share_button))
            .performClick()

        assertTrue(shareClicked)
    }

    // overlay

    @Test
    fun sharingOverlay_isShown_whenIsSharing() {
        setDetailContent(state = detailState(isSharing = true))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.report_sharing_preparing))
            .assertIsDisplayed()
    }

    @Test
    fun sharingOverlay_isHidden_whenNotSharing() {
        setDetailContent(state = detailState(isSharing = false))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.report_sharing_preparing))
            .assertDoesNotExist()
    }

    // helpers

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
                    snackbarHostState = remember { SnackbarHostState() },
                    graphicsLayer = rememberGraphicsLayer(),
                    onClickBack = onClickBack,
                    onClickShare = onClickShare,
                )
            }
        }
    }

    private fun archiveState(
        items: List<WeeklyReportArchiveItemUiState> = listOf(archiveItem()),
    ) = WeeklyReportArchiveState(
        archiveItems = items,
        isLoading = false,
    )

    private fun detailState(
        weekStart: LocalDate = LocalDate.of(2026, 4, 7),
        isSharing: Boolean = false,
    ) = WeeklyReportDetailState(
        weekStartEpochDay = weekStart.toEpochDay(),
        totalSteps = 42_000,
        achievementPct = 71,
        achievedDays = 5,
        totalDays = 7,
        achievementRate = 5f / 7f,
        bestDayEpochDay = weekStart.plusDays(2).toEpochDay(),
        bestHour = 15,
        longestAchievedStreak = 3,
        summaryMessageType = WeeklyReportSummaryMessageType.GoodProgress,
        dailyCounts = List(7) { index ->
            com.river.walklog.core.model.DailyStepCount(weekStart.toEpochDay() + index, 6_000)
        },
        isLoading = false,
        isSharing = isSharing,
    )

    private fun archiveItem(
        isLocked: Boolean = false,
        unlockDate: LocalDate = LocalDate.ofEpochDay(19_007L),
    ) = WeeklyReportArchiveItemUiState(
        weekStartEpochDay = 19_000L,
        unlockDate = unlockDate,
        totalSteps = 42_000,
        achievementPct = 71,
        achievementRate = 0.71f,
        isLocked = isLocked,
    )

    private fun unlockDateFormatter(): DateTimeFormatter {
        val locale = activity.resources.configuration.locales[0]
        return DateTimeFormatter.ofPattern(activity.getString(R.string.report_full_date_format_pattern), locale)
    }

    private fun fullDateFormatter(): DateTimeFormatter {
        val locale = activity.resources.configuration.locales[0]
        return DateTimeFormatter.ofPattern(activity.getString(R.string.report_full_date_format_pattern), locale)
    }
}
