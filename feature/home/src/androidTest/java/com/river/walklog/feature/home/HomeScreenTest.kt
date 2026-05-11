package com.river.walklog.feature.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity get() = composeTestRule.activity

    // 로딩

    @Test
    fun loadingState_showsSensorLoadingIndicator() {
        setContent(state = HomeState(sensorStatus = SensorStatus.Loading))

        composeTestRule
            .onNodeWithTag(HomeTestTags.SENSOR_LOADING)
            .assertIsDisplayed()
    }

    @Test
    fun loadingState_doesNotShowPermissionCard() {
        setContent(state = HomeState(sensorStatus = SensorStatus.Loading))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.sensor_permission_button))
            .assertDoesNotExist()
    }

    // 센서 사용 불가

    @Test
    fun unavailableState_showsSensorUnavailableMessage() {
        setContent(state = HomeState(sensorStatus = SensorStatus.Unavailable))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.sensor_unavailable_title))
            .assertIsDisplayed()
    }

    @Test
    fun unavailableState_doesNotShowWeeklyReportCard() {
        setContent(state = HomeState(sensorStatus = SensorStatus.Unavailable))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.home_weekly_report_title))
            .assertDoesNotExist()
    }

    // 센서 권한 필요

    @Test
    fun permissionRequired_showsPermissionRequestMessage() {
        setContent(state = HomeState(sensorStatus = SensorStatus.PermissionRequired))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.sensor_permission_title))
            .assertIsDisplayed()
    }

    @Test
    fun permissionRequired_showsAllowButton() {
        setContent(state = HomeState(sensorStatus = SensorStatus.PermissionRequired))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.sensor_permission_button))
            .assertIsDisplayed()
    }

    @Test
    fun allowButton_clicked_invokesOnRequestPermissionCallback() {
        var permissionRequested = false
        setContent(
            state = HomeState(sensorStatus = SensorStatus.PermissionRequired),
            onRequestPermission = { permissionRequested = true },
        )

        composeTestRule
            .onNodeWithText(activity.getString(R.string.sensor_permission_button))
            .performClick()

        assertTrue(permissionRequested)
    }

    // 센서 사용 가능

    @Test
    fun availableState_showsCurrentStepCount() {
        setContent(
            state = HomeState(
                sensorStatus = SensorStatus.Available,
                currentSteps = 4_200,
            ),
        )

        composeTestRule
            .onNodeWithText(activity.getString(R.string.steps_format, "4,200"))
            .assertIsDisplayed()
    }

    @Test
    fun availableState_showsRemainingStepsMessage_whenNotYetAchieved() {
        setContent(
            state = HomeState(
                sensorStatus = SensorStatus.Available,
                currentSteps = 3_000,
                targetSteps = 6_000,
            ),
            isExpanded = true,
        )

        composeTestRule
            .onNodeWithText(activity.getString(R.string.home_steps_remaining, 3000))
            .assertIsDisplayed()
    }

    @Test
    fun availableState_showsAchievementMessage_whenGoalIsReached() {
        setContent(
            state = HomeState(
                sensorStatus = SensorStatus.Available,
                currentSteps = 6_000,
                targetSteps = 6_000,
            ),
            isExpanded = true,
        )

        composeTestRule
            .onNodeWithText(activity.getString(R.string.home_goal_achieved))
            .assertIsDisplayed()
    }

    @Test
    fun availableState_showsMissionCard() {
        setContent(
            state = HomeState(
                sensorStatus = SensorStatus.Available,
                currentSteps = 3_000,
                targetSteps = 6_000,
            ),
        )

        composeTestRule
            .onNodeWithText(activity.getString(R.string.mission_default_title))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun missionCard_clicked_invokesOnClickTodayMissionCallback() {
        var clicked = false
        setContent(
            state = HomeState(sensorStatus = SensorStatus.Available),
            onClickTodayMission = { clicked = true },
        )

        composeTestRule
            .onNodeWithText(activity.getString(R.string.mission_today))
            .performScrollTo()
            .performClick()

        assertTrue(clicked)
    }

    // 헤더

    @Test
    fun header_showsUserGreetingWithName() {
        setContent(state = HomeState(userName = "익명"))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.home_greeting, "익명"))
            .assertIsDisplayed()
    }

    @Test
    fun header_showsTodayDateText() {
        setContent(state = HomeState(todayDate = LocalDate.of(2025, 4, 16)))

        composeTestRule
            .onNodeWithText("4월 16일 수요일")
            .assertIsDisplayed()
    }

    // 리캡 카드

    @Test
    fun recapCard_isHidden_whenMonthLabelIsEmpty() {
        setContent(state = HomeState(recapYearMonth = null))

        composeTestRule
            .onNodeWithText(activity.getString(R.string.home_recap_view))
            .assertDoesNotExist()
    }

    @Test
    fun recapCard_isVisible_whenMonthLabelIsSet() {
        setContent(
            state = HomeState(
                recapYearMonth = YearMonth.of(2025, 3),
                recapTotalSteps = 120_000,
            ),
        )

        composeTestRule
            .onNodeWithText(activity.getString(R.string.home_recap_label, "3월"))
            .performScrollTo()
            .assertIsDisplayed()
    }

    // helper

    private fun setContent(
        state: HomeState,
        isExpanded: Boolean = false,
        onClickTodayMission: () -> Unit = {},
        onClickWeeklyReport: () -> Unit = {},
        onClickForecast: () -> Unit = {},
        onRefresh: () -> Unit = {},
        onRequestPermission: () -> Unit = {},
        onClickRecap: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            WalkLogTheme {
                HomeScreen(
                    state = state,
                    isExpanded = isExpanded,
                    onClickTodayMission = onClickTodayMission,
                    onClickWeeklyReport = onClickWeeklyReport,
                    onClickForecast = onClickForecast,
                    onRefresh = onRefresh,
                    onRequestPermission = onRequestPermission,
                    onClickRecap = onClickRecap,
                )
            }
        }
    }
}
