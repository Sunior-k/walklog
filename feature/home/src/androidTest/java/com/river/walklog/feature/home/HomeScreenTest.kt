package com.river.walklog.feature.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.feature.home.model.MissionCardUiModel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // ─── SensorStatus.Loading ──────────────────────────────────────────────

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
            .onNodeWithText("권한 허용하기")
            .assertDoesNotExist()
    }

    // ─── SensorStatus.Unavailable ──────────────────────────────────────────

    @Test
    fun unavailableState_showsSensorUnavailableMessage() {
        setContent(state = HomeState(sensorStatus = SensorStatus.Unavailable))

        composeTestRule
            .onNodeWithText("만보계를 지원하지 않는 기기예요")
            .assertIsDisplayed()
    }

    @Test
    fun unavailableState_doesNotShowWeeklyReportCard() {
        setContent(state = HomeState(sensorStatus = SensorStatus.Unavailable))

        composeTestRule
            .onNodeWithText("주간 리포트 모아보기")
            .assertDoesNotExist()
    }

    // ─── SensorStatus.PermissionRequired ──────────────────────────────────

    @Test
    fun permissionRequired_showsPermissionRequestMessage() {
        setContent(state = HomeState(sensorStatus = SensorStatus.PermissionRequired))

        composeTestRule
            .onNodeWithText("걸음 수 측정 권한이 필요해요")
            .assertIsDisplayed()
    }

    @Test
    fun permissionRequired_showsAllowButton() {
        setContent(state = HomeState(sensorStatus = SensorStatus.PermissionRequired))

        composeTestRule
            .onNodeWithText("권한 허용하기")
            .assertIsDisplayed()
    }

    @Test
    fun allowButton_clicked_invokesOnRequestPermissionCallback() {
        var permissionRequested = false
        setContent(
            state = HomeState(sensorStatus = SensorStatus.PermissionRequired),
            onRequestPermission = { permissionRequested = true },
        )

        composeTestRule.onNodeWithText("권한 허용하기").performClick()

        assertTrue(permissionRequested)
    }

    // ─── SensorStatus.Available ────────────────────────────────────────────

    @Test
    fun availableState_showsCurrentStepCount() {
        setContent(
            state = HomeState(
                sensorStatus = SensorStatus.Available,
                currentSteps = 4_200,
            ),
        )

        composeTestRule
            .onNodeWithText("4200보")
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
        )

        composeTestRule
            .onNodeWithText("3000보 남았어요")
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
        )

        composeTestRule
            .onNodeWithText("오늘 목표를 달성했어요")
            .assertIsDisplayed()
    }

    @Test
    fun availableState_showsMissionCard() {
        setContent(
            state = HomeState(
                sensorStatus = SensorStatus.Available,
                mission = MissionCardUiModel(
                    title = "오늘의 만보 걷기",
                    currentSteps = 3_000,
                    targetSteps = 6_000,
                    rewardText = "+20 캐시",
                ),
            ),
        )

        composeTestRule
            .onNodeWithText("오늘의 만보 걷기")
            .assertIsDisplayed()
    }

    @Test
    fun missionCard_clicked_invokesOnClickTodayMissionCallback() {
        var clicked = false
        setContent(
            state = HomeState(sensorStatus = SensorStatus.Available),
            onClickTodayMission = { clicked = true },
        )

        composeTestRule.onNodeWithText("오늘 미션").performClick()

        assertTrue(clicked)
    }

    // ─── Header ────────────────────────────────────────────────────────────

    @Test
    fun header_showsUserGreetingWithName() {
        setContent(state = HomeState(userName = "익명"))

        composeTestRule
            .onNodeWithText("익명님, 오늘도 걸어볼까요?")
            .assertIsDisplayed()
    }

    @Test
    fun header_showsTodayDateText() {
        setContent(state = HomeState(todayDateText = "4월 16일 수요일"))

        composeTestRule
            .onNodeWithText("4월 16일 수요일")
            .assertIsDisplayed()
    }

    // ─── Recap card ────────────────────────────────────────────────────────

    @Test
    fun recapCard_isHidden_whenMonthLabelIsEmpty() {
        setContent(state = HomeState(recapMonthLabel = ""))

        composeTestRule
            .onNodeWithText("리캡 보기")
            .assertDoesNotExist()
    }

    @Test
    fun recapCard_isVisible_whenMonthLabelIsSet() {
        setContent(
            state = HomeState(
                recapMonthLabel = "3월",
                recapTotalStepsText = "120,000보",
            ),
        )

        composeTestRule
            .onNodeWithText("3월 리캡")
            .assertIsDisplayed()
    }

    // ─── Helper ────────────────────────────────────────────────────────────

    private fun setContent(
        state: HomeState,
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
