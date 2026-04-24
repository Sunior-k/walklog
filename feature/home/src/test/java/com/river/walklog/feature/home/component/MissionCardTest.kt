package com.river.walklog.feature.home.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.feature.home.model.MissionCardUiModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class MissionCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ─── Badge text ────────────────────────────────────────────────────────

    @Test
    fun `default state shows 오늘 미션 badge`() {
        setContent(model = model())

        composeTestRule
            .onNodeWithText("오늘 미션")
            .assertIsDisplayed()
    }

    @Test
    fun `recovery state shows 회복 미션 badge`() {
        setContent(model = model(isRecovery = true))

        composeTestRule
            .onNodeWithText("회복 미션")
            .assertIsDisplayed()
    }

    @Test
    fun `completed state shows 달성 완료 badge`() {
        setContent(model = model(isCompleted = true))

        composeTestRule
            .onNodeWithText("달성 완료")
            .assertIsDisplayed()
    }

    // ─── Title ─────────────────────────────────────────────────────────────

    @Test
    fun `title is displayed`() {
        setContent(model = model(title = "오늘의 만보 걷기"))

        composeTestRule
            .onNodeWithText("오늘의 만보 걷기")
            .assertIsDisplayed()
    }

    // ─── Step count ────────────────────────────────────────────────────────

    @Test
    fun `current steps are displayed`() {
        setContent(model = model(currentSteps = 3_200, targetSteps = 6_000))

        composeTestRule
            .onNodeWithText("3200보")
            .assertIsDisplayed()
    }

    @Test
    fun `target steps are displayed with separator`() {
        setContent(model = model(currentSteps = 3_200, targetSteps = 6_000))

        composeTestRule
            .onNodeWithText("/ 6000보")
            .assertIsDisplayed()
    }

    // ─── Reward text ───────────────────────────────────────────────────────

    @Test
    fun `reward text is displayed`() {
        setContent(model = model(rewardText = "+20 캐시"))

        composeTestRule
            .onNodeWithText("+20 캐시")
            .assertIsDisplayed()
    }

    // ─── Zero steps edge case ──────────────────────────────────────────────

    @Test
    fun `zero current steps renders without error`() {
        setContent(model = model(currentSteps = 0, targetSteps = 6_000))

        composeTestRule
            .onNodeWithText("0보")
            .assertIsDisplayed()
    }

    // ─── Helper ────────────────────────────────────────────────────────────

    private fun setContent(model: MissionCardUiModel) {
        composeTestRule.setContent {
            WalkLogTheme {
                MissionCard(model = model)
            }
        }
    }

    private fun model(
        title: String = "오늘 목표까지 조금만 더",
        currentSteps: Int = 3_000,
        targetSteps: Int = 6_000,
        rewardText: String = "+20 캐시",
        isRecovery: Boolean = false,
        isCompleted: Boolean = false,
    ) = MissionCardUiModel(
        title = title,
        currentSteps = currentSteps,
        targetSteps = targetSteps,
        rewardText = rewardText,
        isRecovery = isRecovery,
        isCompleted = isCompleted,
    )
}
