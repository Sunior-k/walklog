package com.river.walklog.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.feature.home.R

@Composable
fun SensorUnavailableCard(modifier: Modifier = Modifier) {
    SensorStatusCard(
        title = stringResource(R.string.sensor_unavailable_title),
        description = stringResource(R.string.sensor_unavailable_desc),
        modifier = modifier,
    )
}

@Composable
fun PermissionRequiredCard(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SensorStatusCard(
        title = stringResource(R.string.sensor_permission_title),
        description = stringResource(R.string.sensor_permission_desc),
        modifier = modifier,
        action = {
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = WalkLogColor.Primary),
            ) {
                Text(
                    text = stringResource(R.string.sensor_permission_button),
                    style = WalkLogTheme.typography.typography6SB,
                    color = WalkLogColor.StaticWhite,
                )
            }
        },
    )
}

@Composable
fun StepDataEmptyCard(modifier: Modifier = Modifier) {
    SensorStatusCard(
        title = stringResource(R.string.sensor_no_data_title),
        description = stringResource(R.string.sensor_no_data_desc),
        modifier = modifier,
    )
}

@Composable
private fun SensorStatusCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(WalkLogTheme.colors.surface, RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = WalkLogTheme.typography.typography5SB,
            color = WalkLogTheme.colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = description,
            style = WalkLogTheme.typography.typography6M,
            color = WalkLogTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(4.dp))
            action()
        }
    }
}
