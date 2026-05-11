package com.river.walklog.feature.report.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.feature.report.R

@Composable
fun WeeklyReportShareCard(
    weekRangeText: String,
    headline: String,
    totalStepsText: String,
    achievementRateText: String,
    bestDayText: String,
    bestTimeText: String,
    streakText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(WalkLogTheme.colors.background)
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ShareCardHeader(weekRangeText = weekRangeText)
        ShareCardHeadline(headline = headline)
        ShareCardHeroMetric(totalStepsText = totalStepsText, achievementRateText = achievementRateText)
        HorizontalDivider(color = WalkLogTheme.colors.outlineVariant)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ShareCardInfoRow("📅", stringResource(R.string.report_share_card_best_day), bestDayText)
            ShareCardInfoRow("⏰", stringResource(R.string.report_share_card_best_time), bestTimeText)
            ShareCardInfoRow("🔥", stringResource(R.string.report_share_card_best_streak), streakText)
        }
        Spacer(modifier = Modifier.weight(1f))
        ShareCardFooter(footerText = stringResource(R.string.report_share_footer))
    }
}

@Composable
private fun ShareCardHeader(weekRangeText: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(weekRangeText, style = WalkLogTheme.typography.typography6SB, color = WalkLogTheme.colors.onSurfaceVariant)
        Box(
            modifier = Modifier.clip(CircleShape).background(WalkLogTheme.colors.primaryContainer).padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("WALKLOG", style = WalkLogTheme.typography.subTypography12B, color = WalkLogTheme.colors.onPrimaryContainer)
        }
    }
}

@Composable
private fun ShareCardHeadline(headline: String) {
    Text(headline, style = WalkLogTheme.typography.typography3B, color = WalkLogTheme.colors.onSurface)
}

@Composable
private fun ShareCardHeroMetric(totalStepsText: String, achievementRateText: String) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(WalkLogTheme.colors.primaryContainer).padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(stringResource(R.string.report_share_card_total_steps), style = WalkLogTheme.typography.typography6M, color = WalkLogTheme.colors.onPrimaryContainer)
        Text(totalStepsText, style = WalkLogTheme.typography.typography2B, color = WalkLogTheme.colors.onPrimaryContainer)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.clip(CircleShape).background(WalkLogTheme.colors.primary).padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.report_share_card_achievement_rate, achievementRateText), style = WalkLogTheme.typography.subTypography12B, color = WalkLogTheme.colors.onPrimary)
            }
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.report_share_card_walking_note), style = WalkLogTheme.typography.typography7M, color = WalkLogTheme.colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun ShareCardInfoRow(emoji: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(WalkLogTheme.colors.surfaceVariant).padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, style = WalkLogTheme.typography.typography5M)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = WalkLogTheme.typography.typography7M, color = WalkLogTheme.colors.onSurfaceVariant)
            Text(value, style = WalkLogTheme.typography.typography6SB, color = WalkLogTheme.colors.onSurface)
        }
    }
}

@Composable
private fun ShareCardFooter(footerText: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        HorizontalDivider(color = WalkLogTheme.colors.outlineVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(footerText, style = WalkLogTheme.typography.subTypography12M, color = WalkLogTheme.colors.onSurfaceVariant)
    }
}
