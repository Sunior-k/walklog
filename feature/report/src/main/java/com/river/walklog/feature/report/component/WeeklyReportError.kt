package com.river.walklog.feature.report.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.feature.report.R

@Composable
internal fun WeeklyReportError(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "⚠️", style = WalkLogTheme.typography.typography1B)
        Text(
            text = stringResource(R.string.report_error_title),
            style = WalkLogTheme.typography.typography4SB,
            color = WalkLogTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.report_error_desc),
            style = WalkLogTheme.typography.typography6M,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeeklyReportErrorStatePreview() {
    WalkLogTheme {
        WeeklyReportError(modifier = Modifier.padding(16.dp))
    }
}
