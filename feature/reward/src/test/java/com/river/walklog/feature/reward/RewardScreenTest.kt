package com.river.walklog.feature.reward

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

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

    private fun setContent() {
        composeTestRule.setContent {
            WalkLogTheme {
                RewardScreen(state = RewardState())
            }
        }
    }
}
