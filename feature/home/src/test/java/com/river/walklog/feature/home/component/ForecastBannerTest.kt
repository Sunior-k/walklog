package com.river.walklog.feature.home.component

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
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class ForecastBannerTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun banner_showsTitle() {
        composeTestRule.setContent {
            WalkLogTheme {
                ForecastBanner(title = "Best hour: 9AM", description = "Walk now for best results")
            }
        }

        composeTestRule
            .onNodeWithText("Best hour: 9AM")
            .assertIsDisplayed()
    }

    @Test
    fun banner_showsDescription() {
        composeTestRule.setContent {
            WalkLogTheme {
                ForecastBanner(title = "Best hour: 9AM", description = "Walk now for best results")
            }
        }

        composeTestRule
            .onNodeWithText("Walk now for best results")
            .assertIsDisplayed()
    }

    @Test
    fun banner_onClick_invokesCallback_whenProvided() {
        var clicked = false
        composeTestRule.setContent {
            WalkLogTheme {
                ForecastBanner(
                    title = "Best hour: 9AM",
                    description = "Walk now",
                    onClick = { clicked = true },
                )
            }
        }

        composeTestRule
            .onNodeWithText("Best hour: 9AM")
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun banner_withNullOnClick_doesNotCrash() {
        composeTestRule.setContent {
            WalkLogTheme {
                ForecastBanner(
                    title = "Best hour: 9AM",
                    description = "Walk now",
                    onClick = null,
                )
            }
        }

        composeTestRule
            .onNodeWithText("Best hour: 9AM")
            .assertIsDisplayed()
    }
}
