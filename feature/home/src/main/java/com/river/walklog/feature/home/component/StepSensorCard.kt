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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme

@Composable
fun SensorUnavailableCard(modifier: Modifier = Modifier) {
    SensorStatusCard(
        title = "Health Connect를 지원하지 않는 기기예요",
        description = "이 기기에서는 Health Connect를 사용할 수 없어\n걸음 수를 측정할 수 없습니다.",
        modifier = modifier,
    )
}

@Composable
fun PermissionRequiredCard(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SensorStatusCard(
        title = "Health Connect 권한이 필요해요",
        description = "걸음 수 데이터를 읽으려면\nHealth Connect 읽기 권한을 허용해주세요.",
        modifier = modifier,
        action = {
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = WalkLogColor.Primary),
            ) {
                Text(
                    text = "권한 허용하기",
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
        title = "아직 걸음 데이터가 없어요",
        description = "조금만 움직이면 걸음 수가 측정됩니다.",
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
