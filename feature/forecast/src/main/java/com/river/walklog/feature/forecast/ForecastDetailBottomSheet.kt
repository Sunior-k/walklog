package com.river.walklog.feature.forecast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastDetailBottomSheet(
    state: ForecastDetailState,
    onDismiss: () -> Unit,
    onClickStartWalking: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = WalkLogTheme.colors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ForecastSheetHeader(title = state.title, recommendedTimeText = state.recommendedTimeText)
            ForecastDescriptionCard(description = state.description)
            ForecastPatternCard(
                averageStepsAtThisTimeText = state.averageStepsAtThisTimeText,
                activeDaysText = state.activeDaysText,
                bestPatternText = state.bestPatternText,
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
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
}

@Composable
private fun ForecastSheetHeader(title: String, recommendedTimeText: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = title, style = WalkLogTheme.typography.typography5SB, color = WalkLogTheme.colors.onSurfaceVariant)
        Text(text = recommendedTimeText, style = WalkLogTheme.typography.typography3B, color = WalkLogTheme.colors.onSurface)
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
        Text("왜 이 시간대를 추천하나요?", style = WalkLogTheme.typography.typography6SB, color = WalkLogTheme.colors.onSurface)
        Text(description, style = WalkLogTheme.typography.typography6M, color = WalkLogTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun ForecastPatternCard(
    averageStepsAtThisTimeText: String,
    activeDaysText: String,
    bestPatternText: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(WalkLogTheme.colors.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("최근 7일 패턴", style = WalkLogTheme.typography.typography6SB, color = WalkLogTheme.colors.onSurface)
        ForecastInfoRow("이 시간 평균 걸음 수", averageStepsAtThisTimeText)
        ForecastInfoRow("활동이 있었던 날", activeDaysText)
        ForecastInfoRow("가장 활발한 패턴", bestPatternText)
    }
}

@Composable
private fun ForecastInfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(label, style = WalkLogTheme.typography.typography6M, color = WalkLogTheme.colors.onSurfaceVariant)
        Text(value, style = WalkLogTheme.typography.typography6SB, color = WalkLogTheme.colors.onSurface)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844)
@Composable
private fun ForecastDetailBottomSheetPreview() {
    WalkLogTheme { ForecastDetailBottomSheet(state = ForecastDetailState(), onDismiss = {}, onClickStartWalking = {}) }
}
