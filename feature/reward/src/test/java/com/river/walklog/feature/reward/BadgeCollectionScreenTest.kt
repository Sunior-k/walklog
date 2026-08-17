package com.river.walklog.feature.reward

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class BadgeCollectionScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity get() = composeTestRule.activity

    @Test
    fun ownedBadge_showsOwnedStatus() {
        setContent(state = BadgeCollectionState(isGoldBadgeOwned = true))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.badge_collection_owned))
            .assertIsDisplayed()
    }

    @Test
    fun lockedBadge_showsLockedMessage() {
        setContent(state = BadgeCollectionState(isGoldBadgeOwned = false))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.badge_collection_locked))
            .assertIsDisplayed()
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
        state: BadgeCollectionState = BadgeCollectionState(),
        onBack: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            WalkLogTheme {
                BadgeCollectionScreen(state = state, onBack = onBack)
            }
        }
    }
}
