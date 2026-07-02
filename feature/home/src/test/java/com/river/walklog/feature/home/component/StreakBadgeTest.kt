package com.river.walklog.feature.home.component

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.feature.home.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class StreakBadgeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity get() = composeTestRule.activity

    @Test
    fun nullStreakDays_showsLoadingText() {
        composeTestRule.setContent {
            WalkLogTheme { StreakBadge(streakDays = null) }
        }

        composeTestRule
            .onNodeWithText(activity.getString(R.string.streak_loading))
            .assertIsDisplayed()
    }

    @Test
    fun zeroStreakDays_showsNoneText() {
        composeTestRule.setContent {
            WalkLogTheme { StreakBadge(streakDays = 0) }
        }

        composeTestRule
            .onNodeWithText(activity.getString(R.string.streak_none))
            .assertIsDisplayed()
    }

    @Test
    fun positiveStreakDays_showsConsecutiveText() {
        composeTestRule.setContent {
            WalkLogTheme { StreakBadge(streakDays = 5) }
        }

        composeTestRule
            .onNodeWithText(activity.getString(R.string.streak_consecutive, 5))
            .assertIsDisplayed()
    }

    @Test
    fun oneStreakDay_showsConsecutiveText() {
        composeTestRule.setContent {
            WalkLogTheme { StreakBadge(streakDays = 1) }
        }

        composeTestRule
            .onNodeWithText(activity.getString(R.string.streak_consecutive, 1))
            .assertIsDisplayed()
    }
}
