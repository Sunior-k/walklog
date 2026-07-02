package com.river.walklog.feature.home.component

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.feature.home.R
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class StepSensorCardTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity get() = composeTestRule.activity

    // SensorUnavailableCard

    @Test
    fun sensorUnavailableCard_showsTitle() {
        composeTestRule.setContent {
            WalkLogTheme { SensorUnavailableCard() }
        }

        composeTestRule
            .onNodeWithText(activity.getString(R.string.sensor_unavailable_title))
            .assertIsDisplayed()
    }

    @Test
    fun sensorUnavailableCard_showsDescription() {
        composeTestRule.setContent {
            WalkLogTheme { SensorUnavailableCard() }
        }

        composeTestRule
            .onNodeWithText(activity.getString(R.string.sensor_unavailable_desc))
            .assertIsDisplayed()
    }

    // PermissionRequiredCard

    @Test
    fun permissionRequiredCard_showsTitle() {
        composeTestRule.setContent {
            WalkLogTheme { PermissionRequiredCard(onRequestPermission = {}) }
        }

        composeTestRule
            .onNodeWithText(activity.getString(R.string.sensor_permission_title))
            .assertIsDisplayed()
    }

    @Test
    fun permissionRequiredCard_showsPermissionButton() {
        composeTestRule.setContent {
            WalkLogTheme { PermissionRequiredCard(onRequestPermission = {}) }
        }

        composeTestRule
            .onNodeWithText(activity.getString(R.string.sensor_permission_button))
            .assertIsDisplayed()
    }

    @Test
    fun permissionRequiredCard_buttonClick_invokesCallback() {
        var clicked = false
        composeTestRule.setContent {
            WalkLogTheme { PermissionRequiredCard(onRequestPermission = { clicked = true }) }
        }

        composeTestRule
            .onNodeWithText(activity.getString(R.string.sensor_permission_button))
            .performClick()

        assertTrue(clicked)
    }

    // StepDataEmptyCard

    @Test
    fun stepDataEmptyCard_showsTitle() {
        composeTestRule.setContent {
            WalkLogTheme { StepDataEmptyCard() }
        }

        composeTestRule
            .onNodeWithText(activity.getString(R.string.sensor_no_data_title))
            .assertIsDisplayed()
    }

    @Test
    fun stepDataEmptyCard_showsDescription() {
        composeTestRule.setContent {
            WalkLogTheme { StepDataEmptyCard() }
        }

        composeTestRule
            .onNodeWithText(activity.getString(R.string.sensor_no_data_desc))
            .assertIsDisplayed()
    }
}
