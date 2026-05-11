package com.river.walklog.feature.home.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.feature.home.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class MissionCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    // badge

    @Test
    fun `default state shows today mission badge`() {
        setContent()

        composeTestRule
            .onNodeWithText(context.getString(R.string.mission_today))
            .assertIsDisplayed()
    }

    @Test
    fun `recovery state shows recovery mission badge`() {
        setContent(isRecovery = true)

        composeTestRule
            .onNodeWithText(context.getString(R.string.mission_recovery))
            .assertIsDisplayed()
    }

    @Test
    fun `completed state shows completed badge`() {
        setContent(isCompleted = true)

        composeTestRule
            .onNodeWithText(context.getString(R.string.mission_completed))
            .assertIsDisplayed()
    }

    // title

    @Test
    fun `title is displayed`() {
        setContent(title = "오늘의 만보 걷기")

        composeTestRule
            .onNodeWithText("오늘의 만보 걷기")
            .assertIsDisplayed()
    }

    // step count

    @Test
    fun `current steps are displayed`() {
        setContent(currentSteps = 3_200, targetSteps = 6_000)

        composeTestRule
            .onNodeWithText(context.getString(R.string.mission_steps_format, 3_200))
            .assertIsDisplayed()
    }

    @Test
    fun `target steps are displayed`() {
        setContent(currentSteps = 3_200, targetSteps = 6_000)

        composeTestRule
            .onNodeWithText(context.getString(R.string.mission_target_steps_format, 6_000))
            .assertIsDisplayed()
    }

    // reward text

    @Test
    fun `reward text is displayed`() {
        setContent(rewardText = "+20 캐시")

        composeTestRule
            .onNodeWithText("+20 캐시")
            .assertIsDisplayed()
    }

    // edge cases

    @Test
    fun `zero current steps renders without error`() {
        setContent(currentSteps = 0, targetSteps = 6_000)

        composeTestRule
            .onNodeWithText(context.getString(R.string.mission_steps_format, 0))
            .assertIsDisplayed()
    }

    // helper

    private fun setContent(
        title: String = "오늘 목표까지 조금만 더",
        currentSteps: Int = 3_000,
        targetSteps: Int = 6_000,
        rewardText: String = "+20 캐시",
        isRecovery: Boolean = false,
        isCompleted: Boolean = false,
    ) {
        composeTestRule.setContent {
            WalkLogTheme {
                MissionCard(
                    title = title,
                    currentSteps = currentSteps,
                    targetSteps = targetSteps,
                    rewardText = rewardText,
                    isRecovery = isRecovery,
                    isCompleted = isCompleted,
                )
            }
        }
    }
}
