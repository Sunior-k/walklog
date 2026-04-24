package com.river.walklog.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme

/**
 * 홈 화면에서 예보 배너 클릭 시 표시되는 바텀시트.
 *
 * 별도 Nav destination이 아닌 HomeRoute 내부 로컬 상태로 관리
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastBottomSheet(
    onDismiss: () -> Unit,
    onClickStartWalking: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = WalkLogTheme.colors.background,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ForecastSheetHeader(
                title = "걷기 예보",
                recommendedTimeText = "오늘 오후 3시가 최적이에요",
            )
            ForecastDescriptionCard(
                description = "평소 이 시간대에 가장 많이 걷고 있어요. 지금 움직이면 목표 달성 확률이 높아요.",
            )
            ForecastPatternCard(
                averageStepsAtThisTime = "평균 1,240보",
                activeDays = "최근 7일 중 5일",
                bestPattern = "평일 오후 시간대",
            )
            Button(
                onClick = onClickStartWalking,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WalkLogColor.Primary,
                    contentColor = WalkLogColor.StaticBlack,
                ),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                Text(text = "지금 걸으러 가기", style = WalkLogTheme.typography.typography6SB)
            }
        }
    }
}

@Composable
private fun ForecastSheetHeader(title: String, recommendedTimeText: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = WalkLogTheme.typography.typography5SB,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
        Text(
            text = recommendedTimeText,
            style = WalkLogTheme.typography.typography3B,
            color = WalkLogTheme.colors.onSurface,
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(WalkLogTheme.colors.secondaryContainer)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = "오늘의 추천 시간",
                style = WalkLogTheme.typography.typography7SB,
                color = WalkLogTheme.colors.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun ForecastDescriptionCard(description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(WalkLogTheme.colors.surfaceVariant)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "왜 이 시간대를 추천하나요?",
            style = WalkLogTheme.typography.typography6SB,
            color = WalkLogTheme.colors.onSurface,
        )
        Text(
            text = description,
            style = WalkLogTheme.typography.typography6M,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ForecastPatternCard(
    averageStepsAtThisTime: String,
    activeDays: String,
    bestPattern: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(WalkLogTheme.colors.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "최근 7일 패턴",
            style = WalkLogTheme.typography.typography6SB,
            color = WalkLogTheme.colors.onSurface,
        )
        ForecastInfoRow("이 시간 평균 걸음 수", averageStepsAtThisTime)
        ForecastInfoRow("활동이 있었던 날", activeDays)
        ForecastInfoRow("가장 활발한 패턴", bestPattern)
    }
}

@Composable
private fun ForecastInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = WalkLogTheme.typography.typography6M,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
        Text(
            text = value,
            style = WalkLogTheme.typography.typography6SB,
            color = WalkLogTheme.colors.onSurface,
        )
    }
}
