package com.river.walklog.feature.report.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.river.walklog.core.designsystem.R
import com.river.walklog.core.designsystem.foundation.WalkLogTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WeeklyReportTopBar(onClickBack: () -> Unit) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back),
                    contentDescription = "뒤로가기",
                    tint = WalkLogTheme.colors.onSurface,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = WalkLogTheme.colors.background),
    )
}

@Preview(showBackground = true)
@Composable
private fun WeeklyReportTopBarPreview() {
    WalkLogTheme {
        WeeklyReportTopBar(onClickBack = {})
    }
}
