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
import java.util.Locale

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class WalkProgressRingTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity get() = composeTestRule.activity

    @Test
    fun ring_showsCurrentStepCount() {
        Locale.setDefault(Locale.US)
        composeTestRule.setContent {
            WalkLogTheme {
                WalkProgressRing(currentSteps = 5_000, targetSteps = 10_000)
            }
        }

        composeTestRule
            .onNodeWithText(activity.getString(R.string.steps_format, "5,000"))
            .assertIsDisplayed()
    }

    @Test
    fun ring_showsZeroStepsWhenCurrentIsNegative() {
        Locale.setDefault(Locale.US)
        composeTestRule.setContent {
            WalkLogTheme {
                WalkProgressRing(currentSteps = -100, targetSteps = 6_000)
            }
        }

        composeTestRule
            .onNodeWithText(activity.getString(R.string.steps_format, "0"))
            .assertIsDisplayed()
    }

    @Test
    fun ring_showsZeroSteps_whenCurrentIsZero() {
        Locale.setDefault(Locale.US)
        composeTestRule.setContent {
            WalkLogTheme {
                WalkProgressRing(currentSteps = 0, targetSteps = 6_000)
            }
        }

        composeTestRule
            .onNodeWithText(activity.getString(R.string.steps_format, "0"))
            .assertIsDisplayed()
    }
}
