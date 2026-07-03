package com.river.walklog.feature.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class OnboardingScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity get() = composeTestRule.activity

    // Page 0 — 로그인

    @Test
    fun page0_showsWelcomeHeadline() {
        setContent(state = OnboardingState(currentPage = 0))
        composeTestRule
            .onNodeWithText(activity.getString(R.string.onboarding_page0_headline))
            .assertIsDisplayed()
    }

    @Test
    fun page0_showsGoogleSignUpButton_whenNotSignedIn() {
        setContent(state = OnboardingState(currentPage = 0, isSignedIn = false))
        composeTestRule
            .onNodeWithText(activity.getString(R.string.onboarding_cta_google_signup))
            .assertIsDisplayed()
    }

    @Test
    fun page0_showsSignedInLabel_whenAlreadySignedIn() {
        setContent(state = OnboardingState(currentPage = 0, isSignedIn = true))
        composeTestRule
            .onNodeWithText(activity.getString(R.string.onboarding_signup_signed_in))
            .assertIsDisplayed()
    }

    @Test
    fun page0_showsSkipButton_whenNotSignedIn() {
        setContent(state = OnboardingState(currentPage = 0, isSignedIn = false))
        composeTestRule
            .onNodeWithText(activity.getString(R.string.onboarding_cta_skip))
            .assertIsDisplayed()
    }

    // Skip 다이얼로그

    @Test
    fun skipDialog_isDisplayed_whenShowSkipConfirmDialogIsTrue() {
        setContent(state = OnboardingState(showSkipConfirmDialog = true))
        composeTestRule
            .onNodeWithText(activity.getString(R.string.onboarding_skip_dialog_title))
            .assertIsDisplayed()
    }

    @Test
    fun skipDialog_isNotDisplayed_byDefault() {
        setContent(state = OnboardingState(showSkipConfirmDialog = false))
        composeTestRule
            .onNodeWithText(activity.getString(R.string.onboarding_skip_dialog_title))
            .assertDoesNotExist()
    }

    @Test
    fun skipDialog_showsMessage() {
        setContent(state = OnboardingState(showSkipConfirmDialog = true))
        composeTestRule
            .onNodeWithText(activity.getString(R.string.onboarding_skip_dialog_message), substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun skipDialog_hasTwoNodesWithSkipText_whenDialogShown() {
        // onboarding_skip_dialog_confirm == onboarding_cta_skip ("건너뛰기")
        // skip 버튼 + dialog confirm 버튼 2개 모두 assertIsDisplayed 가능해야 함
        setContent(state = OnboardingState(showSkipConfirmDialog = true))
        composeTestRule
            .onAllNodesWithText(activity.getString(R.string.onboarding_skip_dialog_confirm))[0]
            .assertIsDisplayed()
        composeTestRule
            .onAllNodesWithText(activity.getString(R.string.onboarding_skip_dialog_confirm))[1]
            .assertIsDisplayed()
    }

    @Test
    fun skipDialog_showsDismissButton() {
        setContent(state = OnboardingState(showSkipConfirmDialog = true))
        composeTestRule
            .onNodeWithText(activity.getString(R.string.onboarding_skip_dialog_dismiss))
            .assertIsDisplayed()
    }

    // Page 2 — Health Connect 권한

    @Test
    fun page2_showsHealthConnectHeadline() {
        setContent(state = OnboardingState(currentPage = 2))
        composeTestRule
            .onNodeWithText(activity.getString(R.string.onboarding_page2_headline))
            .assertIsDisplayed()
    }

    @Test
    fun page2_showsPermissionCtaButton() {
        setContent(state = OnboardingState(currentPage = 2))
        composeTestRule
            .onNodeWithText(activity.getString(R.string.onboarding_cta_permission))
            .assertIsDisplayed()
    }

    // Page 3 — 목표 걸음 수

    @Test
    fun page3_showsStepGoalHeadline() {
        setContent(state = OnboardingState(currentPage = 3))
        composeTestRule
            .onNodeWithText(activity.getString(R.string.onboarding_page3_headline))
            .assertIsDisplayed()
    }

    // Page 4 — 알림

    @Test
    fun page4_showsNotificationsHeadline() {
        setContent(state = OnboardingState(currentPage = 4))
        composeTestRule
            .onNodeWithText(activity.getString(R.string.onboarding_page4_headline))
            .assertIsDisplayed()
    }

    @Test
    fun page4_showsStartCtaButton() {
        setContent(state = OnboardingState(currentPage = 4))
        composeTestRule
            .onNodeWithText(activity.getString(R.string.onboarding_cta_start))
            .assertIsDisplayed()
    }

    @Test
    fun page4_showsNotificationsOnLabel_whenEnabled() {
        setContent(state = OnboardingState(currentPage = 4, notificationsEnabled = true))
        // 노드는 pager 내부에 존재하지만 Robolectric 가상 스크린 높이 제약으로 off-screen일 수 있어
        // assertExists()로 composition tree에 올바른 텍스트가 포함되는지 검증
        composeTestRule
            .onNodeWithText(activity.getString(R.string.onboarding_notifications_on_title))
            .assertExists()
    }

    @Test
    fun page4_showsNotificationsOffLabel_whenDisabled() {
        setContent(state = OnboardingState(currentPage = 4, notificationsEnabled = false))
        composeTestRule
            .onNodeWithText(activity.getString(R.string.onboarding_notifications_off_title))
            .assertExists()
    }

    // 완료 중 — 버튼 텍스트 대신 스피너 표시

    @Test
    fun nextButton_showsSpinner_whenIsCompletingTrue() {
        setContent(state = OnboardingState(currentPage = 4, isCompleting = true))
        // isCompleting=true일 때 버튼 내부가 CircularProgressIndicator로 교체되므로 CTA 텍스트가 사라진다
        composeTestRule
            .onNodeWithText(activity.getString(R.string.onboarding_cta_start))
            .assertDoesNotExist()
    }

    private fun setContent(
        state: OnboardingState = OnboardingState(),
        onGoogleSignInClick: () -> Unit = {},
        onSkipSignIn: () -> Unit = {},
        onSkipConfirmed: () -> Unit = {},
        onSkipDismissed: () -> Unit = {},
        onClickNext: () -> Unit = {},
        onNicknameChanged: (String) -> Unit = {},
        onStepGoalChanged: (Int) -> Unit = {},
        onNotificationsToggled: (Boolean) -> Unit = {},
    ) {
        composeTestRule.setContent {
            WalkLogTheme {
                OnboardingScreen(
                    state = state,
                    onGoogleSignInClick = onGoogleSignInClick,
                    onSkipSignIn = onSkipSignIn,
                    onSkipConfirmed = onSkipConfirmed,
                    onSkipDismissed = onSkipDismissed,
                    onClickNext = onClickNext,
                    onNicknameChanged = onNicknameChanged,
                    onStepGoalChanged = onStepGoalChanged,
                    onNotificationsToggled = onNotificationsToggled,
                )
            }
        }
    }
}
