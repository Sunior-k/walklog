package com.river.walklog.feature.reward

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.river.walklog.core.designsystem.component.PremiumBackgroundEffect
import com.river.walklog.core.designsystem.component.WalkLogLottie
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.core.ui.UiText
import com.river.walklog.core.ui.asString
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.river.walklog.core.designsystem.R as DesignR

@Composable
fun PointsHistoryRoute(
    onBack: () -> Unit,
    viewModel: PointsHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PointsHistoryScreen(state = state, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PointsHistoryScreen(
    state: PointsHistoryState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPremium = WalkLogTheme.isPremium

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.reward_feature1_title),
                        style = WalkLogTheme.typography.typography5SB,
                        color = WalkLogTheme.colors.onSurface,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = ImageVector.vectorResource(DesignR.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back_button_cd),
                            tint = WalkLogTheme.colors.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WalkLogTheme.colors.background),
            )
        },
        containerColor = WalkLogTheme.colors.background,
    ) { padding ->
        if (state.groupedEntries.isEmpty()) {
            PointsHistoryEmptyState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item {
                    PointsHistorySummaryCard(totalNet = state.totalNet, isPremium = isPremium)
                }
                state.groupedEntries.forEach { group ->
                    item {
                        Text(
                            text = group.dateLabel.asString(),
                            style = WalkLogTheme.typography.subTypography12SB,
                            color = WalkLogTheme.colors.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                    items(group.entries, key = { it.id }) { entry ->
                        PointsHistoryRow(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun PointsHistorySummaryCard(totalNet: Int, isPremium: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(WalkLogTheme.colors.surfaceVariant)
            .then(
                if (isPremium) {
                    Modifier.background(
                        Brush.linearGradient(
                            listOf(
                                WalkLogTheme.colors.primary.copy(alpha = 0.12f),
                                WalkLogTheme.colors.surfaceVariant,
                            ),
                        ),
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        if (isPremium) {
            PremiumBackgroundEffect(
                mode = WalkLogTheme.premiumVisualMode,
                modifier = Modifier.matchParentSize(),
            )
        }
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.points_history_summary_title),
                style = WalkLogTheme.typography.subTypography12R,
                color = WalkLogTheme.colors.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(
                    R.string.points_history_delta_format,
                    if (totalNet >= 0) "+" else "",
                    totalNet,
                ),
                style = WalkLogTheme.typography.typography3B,
                color = WalkLogTheme.colors.primary,
            )
        }
    }
}

@Composable
private fun PointsHistoryRow(entry: PointsHistoryEntryUi, modifier: Modifier = Modifier) {
    val isPositive = entry.deltaPoints >= 0
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WalkLogTheme.colors.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PointsDeltaIcon(isPositive = isPositive)
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                Text(
                    text = entry.reasonText.asString(),
                    style = WalkLogTheme.typography.typography6R,
                    color = WalkLogTheme.colors.onSurface,
                )
                Text(
                    text = formatEntryTime(entry.createdAtEpochMillis),
                    style = WalkLogTheme.typography.subTypography13R,
                    color = WalkLogTheme.colors.onSurfaceVariant,
                )
            }
        }
        Text(
            text = stringResource(
                R.string.points_history_delta_format,
                if (isPositive) "+" else "",
                entry.deltaPoints,
            ),
            style = WalkLogTheme.typography.typography6SB,
            color = if (isPositive) WalkLogTheme.colors.primary else WalkLogTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun PointsDeltaIcon(isPositive: Boolean, modifier: Modifier = Modifier) {
    val backgroundColor = if (isPositive) {
        WalkLogTheme.colors.primary.copy(alpha = 0.15f)
    } else {
        WalkLogTheme.colors.onSurfaceVariant.copy(alpha = 0.12f)
    }
    val iconColor = if (isPositive) WalkLogTheme.colors.primary else WalkLogTheme.colors.onSurfaceVariant

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(14.dp)) {
            val path = Path().apply {
                if (isPositive) {
                    moveTo(size.width / 2f, 0f)
                    lineTo(size.width, size.height * 0.6f)
                    lineTo(size.width * 0.65f, size.height * 0.6f)
                    lineTo(size.width * 0.65f, size.height)
                    lineTo(size.width * 0.35f, size.height)
                    lineTo(size.width * 0.35f, size.height * 0.6f)
                    lineTo(0f, size.height * 0.6f)
                    close()
                } else {
                    moveTo(size.width / 2f, size.height)
                    lineTo(size.width, size.height * 0.4f)
                    lineTo(size.width * 0.65f, size.height * 0.4f)
                    lineTo(size.width * 0.65f, 0f)
                    lineTo(size.width * 0.35f, 0f)
                    lineTo(size.width * 0.35f, size.height * 0.4f)
                    lineTo(0f, size.height * 0.4f)
                    close()
                }
            }
            drawPath(path = path, color = iconColor)
        }
    }
}

@Composable
private fun PointsHistoryEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        WalkLogLottie(
            resId = DesignR.raw.clock,
            modifier = Modifier.size(140.dp),
        )
        Text(
            text = stringResource(R.string.points_history_empty_title),
            style = WalkLogTheme.typography.typography6SB,
            color = WalkLogTheme.colors.onSurface,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.points_history_empty_subtitle),
            style = WalkLogTheme.typography.subTypography13R,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
    }
}

private fun formatEntryTime(epochMillis: Long): String =
    DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        .format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))
