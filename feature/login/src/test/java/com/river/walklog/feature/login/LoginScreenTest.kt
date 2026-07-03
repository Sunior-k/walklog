package com.river.walklog.feature.login

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
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity get() = composeTestRule.activity

    @Test
    fun loginTitle_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText(activity.getString(R.string.login_title))
            .assertIsDisplayed()
    }

    @Test
    fun loginSubtitle_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText(activity.getString(R.string.login_subtitle))
            .assertIsDisplayed()
    }

    @Test
    fun googleSignInButton_isDisplayed_whenNotLoading() {
        setContent(state = LoginState(isLoading = false))
        composeTestRule
            .onNodeWithText(activity.getString(R.string.login_google_button))
            .assertIsDisplayed()
    }

    @Test
    fun googleSignInButton_isNotDisplayed_whenLoading() {
        setContent(state = LoginState(isLoading = true))
        composeTestRule
            .onNodeWithText(activity.getString(R.string.login_google_button))
            .assertDoesNotExist()
    }

    private fun setContent(
        state: LoginState = LoginState(),
        onGoogleSignInClick: () -> Unit = {},
        onErrorShown: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            WalkLogTheme {
                LoginContent(
                    state = state,
                    onGoogleSignInClick = onGoogleSignInClick,
                    onErrorShown = onErrorShown,
                )
            }
        }
    }
}
