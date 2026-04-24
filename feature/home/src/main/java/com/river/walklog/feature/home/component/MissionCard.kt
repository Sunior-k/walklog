package com.river.walklog.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.component.WalkLogLinearProgressBar
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.feature.home.model.MissionCardUiModel

@Composable
fun MissionCard(
    model: MissionCardUiModel,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val progress = if (model.targetSteps <= 0) {
        0f
    } else {
        (model.currentSteps.toFloat() / model.targetSteps.toFloat()).coerceIn(0f, 1f)
    }

    val cardColor = when {
        model.isCompleted -> WalkLogTheme.colors.tertiaryContainer
        model.isRecovery -> WalkLogTheme.colors.primaryContainer
        else -> WalkLogTheme.colors.surface
    }

    val badgeText = when {
        model.isCompleted -> "달성 완료"
        model.isRecovery -> "회복 미션"
        else -> "오늘 미션"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = badgeText,
                style = WalkLogTheme.typography.typography6SB,
                color = WalkLogTheme.colors.onSurfaceVariant,
            )
            Text(
                text = model.rewardText,
                style = WalkLogTheme.typography.typography6B,
                color = WalkLogTheme.colors.primary,
            )
        }

        Text(
            text = model.title,
            style = WalkLogTheme.typography.subTypography9B,
            color = WalkLogTheme.colors.onSurface,
        )

        WalkLogLinearProgressBar(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp)),
            color = if (model.isCompleted) WalkLogColor.Success else WalkLogColor.Primary,
            trackColor = WalkLogTheme.colors.outlineVariant,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${model.currentSteps}보",
                style = WalkLogTheme.typography.typography6B,
                color = WalkLogTheme.colors.onSurface,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "/ ${model.targetSteps}보",
                style = WalkLogTheme.typography.typography7M,
                color = WalkLogTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Preview @Composable
private fun MissionCardPreview() {
    WalkLogTheme {
        MissionCard(
            model = MissionCardUiModel(
                title = "오늘의 만보 걷기",
                currentSteps = 6_500,
                targetSteps = 10_000,
                rewardText = "100캐시",
            ),
        )
    }
}
