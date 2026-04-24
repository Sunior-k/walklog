package com.river.walklog.feature.report.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.feature.report.model.WeeklyReportShareCardUiModel

@Composable
fun ShareableWeeklyReportCard(
    model: WeeklyReportShareCardUiModel,
    graphicsLayer: GraphicsLayer,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(WalkLogTheme.colors.background)
            .drawWithContent {
                graphicsLayer.record { this@drawWithContent.drawContent() }
                drawLayer(graphicsLayer)
            },
        color = WalkLogTheme.colors.background,
    ) {
        WeeklyReportShareCard(
            model = model,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
        )
    }
}
