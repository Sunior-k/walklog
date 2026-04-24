package com.river.walklog.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.foundation.WalkLogTheme

@Composable
fun StreakBadge(
    streakDays: Int,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (streakDays > 0) {
        WalkLogTheme.colors.primaryContainer
    } else {
        WalkLogTheme.colors.surfaceVariant
    }
    val textColor = if (streakDays > 0) {
        WalkLogTheme.colors.onPrimaryContainer
    } else {
        WalkLogTheme.colors.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .background(color = backgroundColor, shape = CircleShape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (streakDays > 0) "🔥 ${streakDays}일 연속" else "스트릭 없음",
            style = WalkLogTheme.typography.typography6SB,
            color = textColor,
        )
    }
}

@Preview @Composable
private fun StreakBadgePreview() {
    WalkLogTheme { StreakBadge(streakDays = 5) }
}
