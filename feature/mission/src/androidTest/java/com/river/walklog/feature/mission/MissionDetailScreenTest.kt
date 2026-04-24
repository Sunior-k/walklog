package com.river.walklog.feature.mission

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.core.model.MissionType
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MissionDetailScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // ─── TopBar ────────────────────────────────────────────────────────────

    @Test
    fun topBar_showsMissionDetailTitle() {
        setContent(state = MissionDetailState())

        composeTestRule
            .onNodeWithText("미션 상세")
            .assertIsDisplayed()
    }

    @Test
    fun backButton_clicked_invokesOnClickBackCallback() {
        var backClicked = false
        setContent(state = MissionDetailState(), onClickBack = { backClicked = true })

        composeTestRule.onNodeWithContentDescription("뒤로가기").performClick()

        assertTrue(backClicked)
    }

    // ─── Mission type badge ────────────────────────────────────────────────

    @Test
    fun dailyMission_showsTodayMissionBadge() {
        setContent(state = MissionDetailState(missionType = MissionType.DAILY, isCompleted = false))

        composeTestRule
            .onNodeWithText("오늘 미션")
            .assertIsDisplayed()
    }

    @Test
    fun recoveryMission_showsRecoveryMissionBadge() {
        setContent(state = MissionDetailState(missionType = MissionType.RECOVERY, isCompleted = false))

        composeTestRule
            .onNodeWithText("회복 미션")
            .assertIsDisplayed()
    }

    @Test
    fun completedMission_showsAchievementCompleteBadge() {
        setContent(state = MissionDetailState(isCompleted = true))

        composeTestRule
            .onNodeWithText("달성 완료")
            .assertIsDisplayed()
    }

    // ─── Title & description ───────────────────────────────────────────────

    @Test
    fun missionTitle_isDisplayed() {
        setContent(state = MissionDetailState(title = "오늘 만보 걷기 도전"))

        composeTestRule
            .onNodeWithText("오늘 만보 걷기 도전")
            .assertIsDisplayed()
    }

    @Test
    fun missionDescription_isDisplayed() {
        setContent(state = MissionDetailState(description = "매일 꾸준히 걷는 습관을 만들어봐요."))

        composeTestRule
            .onNodeWithText("매일 꾸준히 걷는 습관을 만들어봐요.")
            .assertIsDisplayed()
    }

    // ─── Progress card ─────────────────────────────────────────────────────

    @Test
    fun progressCard_showsCurrentStepCount() {
        setContent(state = MissionDetailState(currentSteps = 3_500, targetSteps = 6_000))

        composeTestRule
            .onNodeWithText("3500보")
            .assertIsDisplayed()
    }

    @Test
    fun progressCard_showsTargetStepCount() {
        setContent(state = MissionDetailState(currentSteps = 3_500, targetSteps = 6_000))

        composeTestRule
            .onNodeWithText("/ 6000보")
            .assertIsDisplayed()
    }

    @Test
    fun progressCard_showsRemainingSteps_whenNotCompleted() {
        setContent(state = MissionDetailState(currentSteps = 3_500, targetSteps = 6_000, isCompleted = false))

        composeTestRule
            .onNodeWithText("2500보 남았어요")
            .assertIsDisplayed()
    }

    @Test
    fun progressCard_showsAchievementMessage_whenCompleted() {
        setContent(state = MissionDetailState(isCompleted = true))

        composeTestRule
            .onNodeWithText("목표를 달성했어요 🎉")
            .assertIsDisplayed()
    }

    @Test
    fun rewardText_isDisplayed() {
        setContent(state = MissionDetailState(rewardText = "+50 캐시"))

        composeTestRule
            .onNodeWithText("+50 캐시")
            .assertIsDisplayed()
    }

    // ─── Bottom bar ────────────────────────────────────────────────────────

    @Test
    fun bottomBar_showsStartWalkingLabel_whenNotCompleted() {
        setContent(state = MissionDetailState(isCompleted = false))

        composeTestRule
            .onNodeWithText("지금 걸으러 가기")
            .assertIsDisplayed()
    }

    @Test
    fun bottomBarActionButton_isEnabled_whenNotCompleted() {
        setContent(state = MissionDetailState(isCompleted = false))

        composeTestRule
            .onNodeWithText("지금 걸으러 가기")
            .assertIsEnabled()
    }

    @Test
    fun bottomBar_showsAlreadyAchievedLabel_whenCompleted() {
        setContent(state = MissionDetailState(isCompleted = true))

        composeTestRule
            .onNodeWithText("이미 달성했어요")
            .assertIsDisplayed()
    }

    @Test
    fun bottomBarActionButton_isDisabled_whenCompleted() {
        setContent(state = MissionDetailState(isCompleted = true))

        composeTestRule
            .onNodeWithText("이미 달성했어요")
            .assertIsNotEnabled()
    }

    @Test
    fun actionButton_clicked_whenNotCompleted_invokesCallback() {
        var actionClicked = false
        setContent(
            state = MissionDetailState(isCompleted = false),
            onClickAction = { actionClicked = true },
        )

        composeTestRule.onNodeWithText("지금 걸으러 가기").performClick()

        assertTrue(actionClicked)
    }

    // ─── Guide card ────────────────────────────────────────────────────────

    @Test
    fun guideCard_showsRecoveryMessage_forRecoveryMissionType() {
        setContent(
            state = MissionDetailState(
                missionType = MissionType.RECOVERY,
                isCompleted = false,
            ),
        )

        composeTestRule
            .onNodeWithText("어제 놓친 목표를 다시 이어가보세요")
            .assertIsDisplayed()
    }

    @Test
    fun guideCard_showsCompletionMessage_whenMissionIsCompleted() {
        setContent(state = MissionDetailState(isCompleted = true))

        composeTestRule
            .onNodeWithText("오늘도 꾸준히 걸어주셨네요")
            .assertIsDisplayed()
    }

    // ─── Helper ────────────────────────────────────────────────────────────

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
