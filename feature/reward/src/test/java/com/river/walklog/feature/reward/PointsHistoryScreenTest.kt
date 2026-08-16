package com.river.walklog.feature.reward

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.core.ui.UiText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class PointsHistoryScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity get() = composeTestRule.activity

    @Test
    fun emptyState_showsEmptyMessage() {
        setContent(state = PointsHistoryState(groupedEntries = emptyList()))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.points_history_empty_title))
            .assertIsDisplayed()
    }

    @Test
    fun entry_displaysPointsAndReason() {
        setContent(
            state = PointsHistoryState(
                totalNet = 999,
                groupedEntries = listOf(
                    PointsHistoryDateGroup(
                        dateLabel = UiText.StringRes(R.string.points_history_today),
                        entries = listOf(
                            PointsHistoryEntryUi(
                                id = 1,
                                deltaPoints = 20,
                                reasonText = UiText.StringRes(R.string.points_history_reason_daily),
                                createdAtEpochMillis = System.currentTimeMillis(),
                            ),
                        ),
                    ),
                ),
            ),
        )

        composeTestRule.onNodeWithText("+20P").assertIsDisplayed()
        composeTestRule
            .onNodeWithText(activity.getString(R.string.points_history_reason_daily))
            .assertIsDisplayed()
    }

    @Test
    fun summaryCard_displaysTotalNet() {
        setContent(
            state = PointsHistoryState(
                totalNet = 45,
                groupedEntries = listOf(
                    PointsHistoryDateGroup(
                        dateLabel = UiText.StringRes(R.string.points_history_today),
                        entries = listOf(
                            PointsHistoryEntryUi(
                                id = 1,
                                deltaPoints = 20,
                                reasonText = UiText.StringRes(R.string.points_history_reason_daily),
                                createdAtEpochMillis = System.currentTimeMillis(),
                            ),
                        ),
                    ),
                ),
            ),
        )

        composeTestRule.onNodeWithText("+45P").assertIsDisplayed()
    }

    @Test
    fun backButton_click_invokesOnBack() {
        var clicked = false
        setContent(onBack = { clicked = true })

        composeTestRule
            .onNodeWithContentDescription(activity.getString(R.string.back_button_cd))
            .performClick()

        assertTrue(clicked)
    }

    private fun setContent(
        state: PointsHistoryState = PointsHistoryState(),
        onBack: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            WalkLogTheme {
                PointsHistoryScreen(state = state, onBack = onBack)
            }
        }
    }
}
