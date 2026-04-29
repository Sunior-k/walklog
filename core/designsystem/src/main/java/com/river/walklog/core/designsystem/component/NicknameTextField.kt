package com.river.walklog.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme

const val NICKNAME_MAX_LENGTH = 10
private val NICKNAME_REGEX = Regex("^[가-힣ㄱ-ㅣa-zA-Z]*\$")

@Composable
fun NicknameTextField(
    nickname: String,
    onNicknameChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Done,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier) {
        OutlinedTextField(
            value = nickname,
            onValueChange = { input ->
                if (input.length <= NICKNAME_MAX_LENGTH && (input.isEmpty() || NICKNAME_REGEX.matches(input))) {
                    onNicknameChanged(input)
                }
            },
            placeholder = {
                Text(
                    text = "닉네임을 입력해주세요",
                    style = WalkLogTheme.typography.subTypography9R,
                    color = WalkLogColor.TextDisabled,
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            textStyle = WalkLogTheme.typography.subTypography9B.copy(
                color = WalkLogColor.TextPrimary,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WalkLogColor.Primary,
                unfocusedBorderColor = WalkLogColor.Gray200,
                focusedContainerColor = WalkLogColor.StaticWhite,
                unfocusedContainerColor = WalkLogColor.Gray50,
                cursorColor = WalkLogColor.Primary,
            ),
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() },
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, end = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${nickname.length} / $NICKNAME_MAX_LENGTH",
                style = WalkLogTheme.typography.subTypography12R,
                color = if (nickname.length >= NICKNAME_MAX_LENGTH) {
                    WalkLogColor.Primary
                } else {
                    WalkLogColor.TextDisabled
                },
            )
        }
    }
}
