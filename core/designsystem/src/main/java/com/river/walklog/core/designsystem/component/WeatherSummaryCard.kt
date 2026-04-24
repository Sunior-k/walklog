package com.river.walklog.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.R
import com.river.walklog.core.designsystem.foundation.WalkLogTheme

@Composable
fun WeatherSummaryCard(
    locationText: String,
    temperatureText: String,
    conditionText: String,
    adviceText: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    onRefreshClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(WalkLogTheme.colors.surface)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        WeatherConditionMark(conditionText = conditionText)
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = locationText,
                    style = WalkLogTheme.typography.typography7M,
                    color = WalkLogTheme.colors.onSurfaceVariant,
                )
                if (onRefreshClick != null) {
                    IconButton(
                        modifier = Modifier.size(36.dp),
                        onClick = onRefreshClick,
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_refresh),
                            contentDescription = "날씨 새로고침",
                            tint = WalkLogTheme.colors.onSurfaceVariant,
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = temperatureText,
                        style = WalkLogTheme.typography.typography3B,
                        color = WalkLogTheme.colors.onSurface,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = conditionText,
                        style = WalkLogTheme.typography.typography6SB,
                        color = WalkLogTheme.colors.onSurface,
                    )
                }
                Text(
                    text = adviceText,
                    style = WalkLogTheme.typography.subTypography12SB,
                    color = WalkLogTheme.colors.onSurfaceVariant,
                )
                if (supportingText != null) {
                    Text(
                        text = supportingText,
                        style = WalkLogTheme.typography.subTypography12M,
                        color = WalkLogTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherConditionMark(conditionText: String) {
    Column(
        modifier = Modifier
            .size(56.dp)
            .background(WalkLogTheme.colors.primaryContainer, CircleShape),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = conditionText.take(2),
            style = WalkLogTheme.typography.typography7SB,
            color = WalkLogTheme.colors.onPrimaryContainer,
        )
    }
}

@Preview
@Composable
private fun WeatherSummaryCardPreview() {
    WalkLogTheme {
        WeatherSummaryCard(
            locationText = "서울 기준",
            temperatureText = "18°",
            conditionText = "맑음",
            adviceText = "맑은 날씨예요. 가볍게 걷기 좋아요",
            supportingText = "습도 45% · 강수 0%",
            onRefreshClick = {},
        )
    }
}
