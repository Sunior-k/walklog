package com.river.walklog.feature.reward

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class RewardScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity get() = composeTestRule.activity

    @Test
    fun rewardTitle_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText("REWARD")
            .assertIsDisplayed()
    }

    @Test
    fun comingSoonBadge_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText(activity.getString(R.string.reward_coming_soon))
            .assertIsDisplayed()
    }

    @Test
    fun tagline_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText(activity.getString(R.string.reward_tagline))
            .assertIsDisplayed()
    }

    @Test
    fun storeCard_click_invokesOnStoreClick() {
        var clicked = false
        setContent(onStoreClick = { clicked = true })

        composeTestRule
            .onNodeWithTag(RewardTestTags.STORE_CARD)
            .performScrollTo()
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun pointsHistoryCard_click_invokesOnPointsHistoryClick() {
        var clicked = false
        setContent(onPointsHistoryClick = { clicked = true })

        composeTestRule
            .onNodeWithTag(RewardTestTags.POINTS_HISTORY_CARD)
            .performScrollTo()
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun badgeCollectionCard_click_invokesOnBadgeCollectionClick() {
        var clicked = false
        setContent(onBadgeCollectionClick = { clicked = true })

        composeTestRule
            .onNodeWithTag(RewardTestTags.BADGE_COLLECTION_CARD)
            .performScrollTo()
            .performClick()

        assertTrue(clicked)
    }

    private fun setContent(
        onStoreClick: () -> Unit = {},
        onPointsHistoryClick: () -> Unit = {},
        onBadgeCollectionClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            WalkLogTheme {
                RewardScreen(
                    state = RewardState(),
                    onStoreClick = onStoreClick,
                    onPointsHistoryClick = onPointsHistoryClick,
                    onBadgeCollectionClick = onBadgeCollectionClick,
                )
            }
        }
    }
}
