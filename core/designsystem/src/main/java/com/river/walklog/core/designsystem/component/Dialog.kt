package com.river.walklog.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.river.walklog.core.designsystem.component.preview.BasePreview
import com.river.walklog.core.designsystem.component.preview.walklogPreview
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme

@Composable
fun WalkLogDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = WalkLogColor.Surface,
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            Text(
                text = title,
                style = WalkLogTheme.typography.typography4SB,
                color = WalkLogColor.TextPrimary,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = message,
                style = WalkLogTheme.typography.typography6R,
                color = WalkLogColor.TextSecondary,
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = dismissText,
                        style = WalkLogTheme.typography.typography6SB,
                        color = WalkLogColor.TextSecondary,
                    )
                }

                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WalkLogColor.Primary,
                        contentColor = WalkLogColor.StaticBlack,
                    ),
                ) {
                    Text(
                        text = confirmText,
                        style = WalkLogTheme.typography.typography6SB,
                    )
                }
            }
        }
    }
}

@walklogPreview
@Composable
private fun PreviewWalkLogDialog() {
    BasePreview {
        WalkLogDialog(
            title = "로그인 없이 계속할까요?",
            message = "Google 로그인을 건너뛰면 데이터 백업과 기기 간 동기화를 지원하지 않습니다.\n앱을 삭제하거나 기기를 변경하면 기록이 영구적으로 삭제될 수 있어요.",
            confirmText = "건너뛰기",
            dismissText = "취소",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
