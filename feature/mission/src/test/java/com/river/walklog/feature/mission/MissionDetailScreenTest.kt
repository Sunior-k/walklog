package com.river.walklog.feature.mission

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.core.model.MissionType
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class MissionDetailScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity get() = composeTestRule.activity

    // topbar

    @Test
    fun topBar_showsMissionDetailTitle() {
        setContent(state = MissionDetailState())

        composeTestRule
            .onNodeWithText(activity.getString(R.string.mission_detail_title))
            .assertIsDisplayed()
    }

    @Test
    fun backButton_clicked_invokesOnClickBackCallback() {
        var backClicked = false
        setContent(state = MissionDetailState(), onClickBack = { backClicked = true })

        composeTestRule
            .onNodeWithContentDescription(activity.getString(R.string.back_button_cd))
            .performClick()

        assertTrue(backClicked)
    }

    // mission type badge

    @Test
    fun dailyMission_showsTodayMissionBadge() {
        setContent(state = MissionDetailState(missionType = MissionType.DAILY, isCompleted = false))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.mission_type_today))
            .assertIsDisplayed()
    }

    @Test
    fun recoveryMission_showsRecoveryMissionBadge() {
        setContent(state = MissionDetailState(missionType = MissionType.RECOVERY, isCompleted = false))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.mission_type_recovery))
            .assertIsDisplayed()
    }

    @Test
    fun completedMission_showsAchievementCompleteBadge() {
        setContent(state = MissionDetailState(isCompleted = true))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.mission_type_completed))
            .assertIsDisplayed()
    }

    // title and description

    @Test
    fun missionTitle_isDisplayed() {
        setContent(state = MissionDetailState())

        composeTestRule
            .onNodeWithText(activity.getString(R.string.mission_default_title))
            .assertIsDisplayed()
    }

    @Test
    fun missionDescription_isDisplayed() {
        setContent(state = MissionDetailState())

        composeTestRule
            .onNodeWithText(activity.getString(R.string.mission_default_description))
            .assertIsDisplayed()
    }

    // progress card

    @Test
    fun progressCard_showsCurrentStepCount() {
        setContent(state = MissionDetailState(currentSteps = 3_500, targetSteps = 6_000))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.mission_steps_current, 3_500))
            .assertIsDisplayed()
    }

    @Test
    fun progressCard_showsTargetStepCount() {
        setContent(state = MissionDetailState(currentSteps = 3_500, targetSteps = 6_000))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.mission_steps_target, 6_000))
            .assertIsDisplayed()
    }

    @Test
    fun progressCard_showsRemainingSteps_whenNotCompleted() {
        setContent(state = MissionDetailState(currentSteps = 3_500, targetSteps = 6_000, isCompleted = false))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.mission_steps_remaining, 2_500))
            .assertIsDisplayed()
    }

    @Test
    fun progressCard_showsAchievementMessage_whenCompleted() {
        setContent(state = MissionDetailState(isCompleted = true))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.mission_goal_achieved))
            .assertIsDisplayed()
    }

    @Test
    fun rewardText_isDisplayed() {
        setContent(state = MissionDetailState(rewardPoints = 50))

        composeTestRule
            .onAllNodesWithText(activity.getString(R.string.mission_reward_default, 50))
            .onFirst()
            .assertIsDisplayed()
    }

    // bottom bar

    @Test
    fun bottomBar_showsStartWalkingLabel_whenNotCompleted() {
        setContent(state = MissionDetailState(isCompleted = false))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.mission_action_start))
            .assertIsDisplayed()
    }

    @Test
    fun bottomBarActionButton_isEnabled_whenNotCompleted() {
        setContent(state = MissionDetailState(isCompleted = false))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.mission_action_start))
            .assertIsEnabled()
    }

    @Test
    fun bottomBar_showsAlreadyAchievedLabel_whenCompleted() {
        setContent(state = MissionDetailState(isCompleted = true))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.mission_action_completed))
            .assertIsDisplayed()
    }

    @Test
    fun bottomBarActionButton_isDisabled_whenCompleted() {
        setContent(state = MissionDetailState(isCompleted = true))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.mission_action_completed))
            .assertIsNotEnabled()
    }

    @Test
    fun actionButton_clicked_whenNotCompleted_invokesCallback() {
        var actionClicked = false
        setContent(
            state = MissionDetailState(isCompleted = false),
            onClickAction = { actionClicked = true },
        )

        composeTestRule
            .onNodeWithText(activity.getString(R.string.mission_action_start))
            .performClick()

        assertTrue(actionClicked)
    }

    // guide card

    @Test
    fun guideCard_showsRecoveryMessage_forRecoveryMissionType() {
        setContent(
            state = MissionDetailState(
                missionType = MissionType.RECOVERY,
                isCompleted = false,
            ),
        )

        composeTestRule
            .onNodeWithText(activity.getString(R.string.guide_title_recovery))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun guideCard_showsCompletionMessage_whenMissionIsCompleted() {
        setContent(state = MissionDetailState(isCompleted = true))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.guide_title_completed))
            .performScrollTo()
            .assertIsDisplayed()
    }

    // helper

    private fun setContent(
        state: MissionDetailState,
        onClickBack: () -> Unit = {},
        onClickAction: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            WalkLogTheme {
                MissionDetailScreen(
                    state = state,
                    onClickBack = onClickBack,
                    onClickAction = onClickAction,
                )
            }
        }
    }
}
