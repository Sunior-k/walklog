package com.river.walklog.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.feature.home.R

@Composable
fun ForecastBanner(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(WalkLogTheme.colors.secondaryContainer, RoundedCornerShape(20.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    color = WalkLogTheme.colors.onSecondaryContainer.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_walking),
                contentDescription = null,
                tint = WalkLogTheme.colors.onSecondaryContainer,
                modifier = Modifier.size(28.dp),
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = WalkLogTheme.typography.typography6SB,
                color = WalkLogTheme.colors.onSecondaryContainer,
            )
            Text(
                text = description,
                style = WalkLogTheme.typography.subTypography12SB,
                color = WalkLogTheme.colors.onSecondaryContainer,
            )
        }
    }
}

@Preview @Composable
private fun ForecastBannerPreview() {
    WalkLogTheme {
        ForecastBanner(title = "걷기 예보", description = "오늘 오후 3시는 평소 가장 많이 걷는 시간이에요")
    }
}
