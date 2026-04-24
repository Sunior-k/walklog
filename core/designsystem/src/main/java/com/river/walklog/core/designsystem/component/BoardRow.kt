package com.river.walklog.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.river.walklog.core.designsystem.component.preview.BasePreview
import com.river.walklog.core.designsystem.component.preview.walklogPreview
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme

/**
 * 워크로그 BoardRow 컴포넌트
 *
 * 주로 Q&A와 같은 정보를 표현할 때 사용.
 *
 * @param title 헤더 영역에 표시될 제목
 * @param modifier Modifier
 * @param initialOpened 컴포넌트가 처음 렌더링될 때 열려 있을지 여부 (기본값: false)
 * @param isOpened 외부에서 제어하는 열림 상태 (null이면 내부 상태 사용)
 * @param onOpen 콘텐츠 영역이 열릴 때 호출되는 함수
 * @param onClose 콘텐츠 영역이 닫힐 때 호출되는 함수
 * @param prefix 헤더 영역의 title 앞에 표시할 요소 (주로 BoardRow.Prefix)
 * @param showArrow 화살표 아이콘 표시 여부 (기본값: true)
 * @param content 콘텐츠 영역에 표시될 내용
 */

@Composable
fun BoardRow(
    title: String,
    modifier: Modifier = Modifier,
    initialOpened: Boolean = false,
    isOpened: Boolean? = null,
    onOpen: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    prefix: (@Composable () -> Unit)? = null,
    showArrow: Boolean = true,
    content: @Composable () -> Unit,
) {
    var internalOpened by remember { mutableStateOf(initialOpened) }
    val opened = isOpened ?: internalOpened

    val stateDescription = if (opened) "열림" else "닫힘"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(WalkLogTheme.colors.surface),
    ) {
        // 헤더 영역
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    role = Role.Button,
                    onClickLabel = if (opened) "접기" else "펼치기",
                ) {
                    if (isOpened == null) {
                        internalOpened = !internalOpened
                    }
                    if (opened) {
                        onClose?.invoke()
                    } else {
                        onOpen?.invoke()
                    }
                }
                .semantics {
                    this.stateDescription = stateDescription
                    this.contentDescription = "$title, $stateDescription"
                }
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Prefix
            prefix?.invoke()

            // Title
            Text(
                text = title,
                style = WalkLogTheme.typography.typography5R,
                color = WalkLogTheme.colors.onSurface,
                modifier = Modifier.weight(1f),
            )

            // Icon
            if (showArrow) {
                BoardRow.ArrowIcon(isOpened = opened)
            }
        }

        // 콘텐츠 영역
        AnimatedVisibility(
            visible = opened,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
            ) {
                content()
            }
        }

        // 구분선
        HorizontalDivider(
            color = WalkLogTheme.colors.outlineVariant,
            thickness = 1.dp,
        )
    }
}

/**
 * BoardRow의 하위 컴포넌트들을 그룹화하는 object
 */
object BoardRow {
    /**
     * BoardRow Prefix 컴포넌트
     *
     * 헤더 영역의 title 앞에 표시되는 접두사.
     *
     * @param text 접두사 텍스트 (예: "Q", "A")
     * @param modifier Modifier
     * @param typography Typography 스타일 (기본값: SubTypography8)
     * @param fontWeight 폰트 굵기 (기본값: Regular)
     * @param color 텍스트 색상 (기본값: Secondary)
     */
    @Composable
    fun Prefix(
        text: String,
        modifier: Modifier = Modifier,
        typography: TypographyToken = TypographyToken.SubTypography8,
        fontWeight: FontWeight = FontWeight.Normal,
        color: Color = WalkLogColor.Secondary,
    ) {
        val textStyle = typography.getTextStyle()

        Text(
            text = text,
            style = textStyle.copy(fontWeight = fontWeight),
            color = color,
            modifier = modifier,
        )
    }

    /**
     * BoardRow ArrowIcon 컴포넌트
     *
     * 헤더 영역의 title 뒤에 표시되는 화살표 아이콘.
     * 열림/닫힘 상태에 따라 회전.
     *
     * @param isOpened 열림 상태 (true면 180도 회전)
     * @param modifier Modifier
     * @param color 아이콘 색상 (기본값: Gray400)
     */
    @Composable
    fun ArrowIcon(
        isOpened: Boolean = false,
        modifier: Modifier = Modifier,
        color: Color = WalkLogColor.Gray400,
    ) {
        val rotation by animateFloatAsState(
            targetValue = if (isOpened) 180f else 0f,
            label = "arrow_rotation",
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = if (isOpened) "접기" else "펼치기",
            tint = color,
            modifier = modifier.rotate(rotation),
        )
    }

    /**
     * BoardRow Text 컴포넌트
     *
     * 콘텐츠 영역에 표시되는 간단한 텍스트.
     * Post.Paragraph를 확장한 컴포넌트.
     *
     * @param text 표시할 텍스트
     * @param modifier Modifier
     * @param typography Typography 스타일 (기본값: Typography6)
     * @param paddingBottom 아래 여백 (기본값: 0dp)
     */
    @Composable
    fun Text(
        text: String,
        modifier: Modifier = Modifier,
        typography: TypographyToken = TypographyToken.Typography6,
        paddingBottom: androidx.compose.ui.unit.Dp = 0.dp,
    ) {
        val textStyle = typography.getTextStyle()

        Text(
            text = text,
            style = textStyle,
            color = WalkLogTheme.colors.onSurfaceVariant,
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = paddingBottom),
        )
    }
}

@walklogPreview
@Composable
private fun PreviewBoardRowClosed() {
    BasePreview {
        BoardRow(
            title = "매도 환전이 무엇인가요?",
            prefix = { BoardRow.Prefix(text = "Q") },
        ) {
            BoardRow.Text(
                text = "주식 거래가 실시간이 아니기 때문에, 가격이 변할 것에 대비하는 금액을 말해요.",
            )
        }
    }
}

@walklogPreview
@Composable
private fun PreviewBoardRowOpened() {
    BasePreview {
        BoardRow(
            title = "매도 환전이 무엇인가요?",
            initialOpened = true,
            prefix = { BoardRow.Prefix(text = "Q") },
        ) {
            BoardRow.Text(
                text = "주식 거래가 실시간이 아니기 때문에, 가격이 변할 것에 대비하는 금액을 말해요.",
            )
        }
    }
}

@walklogPreview
@Composable
private fun PreviewBoardRowWithPostContent() {
    BasePreview {
        BoardRow(
            title = "질문을 적어주세요.",
            initialOpened = true,
            prefix = { BoardRow.Prefix(text = "Q") },
        ) {
            Post.Paragraph(
                text = "주식 거래가 실시간이 아니기 때문에 가격이 변할 것에 대비하는 금액을 말해요.",
                typography = TypographyToken.Typography6,
                paddingBottom = 16.dp,
            )
            Post.Ul(
                typography = TypographyToken.Typography6,
                paddingBottom = 0.dp,
            ) {
                Post.Li {
                    Text("대시를 붙이고 띄어쓰면 불렛을 쓸 수 있어요.")
                    Post.Ul(
                        typography = TypographyToken.Typography6,
                        paddingBottom = 0.dp,
                    ) {
                        Post.Li {
                            Text("들여쓰려면 대시 앞에 〉를 입력해요.")
                        }
                    }
                }
            }
        }
    }
}

@walklogPreview
@Composable
private fun PreviewBoardRowList() {
    BasePreview {
        Column {
            BoardRow(
                title = "첫 번째 질문입니다.",
                prefix = { BoardRow.Prefix(text = "Q") },
            ) {
                BoardRow.Text(text = "첫 번째 답변입니다.")
            }

            BoardRow(
                title = "두 번째 질문입니다.",
                initialOpened = true,
                prefix = { BoardRow.Prefix(text = "Q") },
            ) {
                BoardRow.Text(text = "두 번째 답변입니다. 이 항목은 처음부터 열려 있습니다.")
            }

            BoardRow(
                title = "세 번째 질문입니다.",
                prefix = { BoardRow.Prefix(text = "Q") },
            ) {
                BoardRow.Text(text = "세 번째 답변입니다.")
            }
        }
    }
}

@walklogPreview
@Composable
private fun PreviewBoardRowControlled() {
    var opened by remember { mutableStateOf(false) }

    BasePreview {
        Column {
            BoardRow(
                title = "외부에서 제어되는 BoardRow",
                isOpened = opened,
                onOpen = { opened = true },
                onClose = { opened = false },
                prefix = { BoardRow.Prefix(text = "Q") },
            ) {
                BoardRow.Text(text = "이 BoardRow는 외부 상태로 제어됩니다.")
            }
        }
    }
}

@walklogPreview
@Composable
private fun PreviewBoardRowCustomPrefix() {
    BasePreview {
        Column {
            BoardRow(
                title = "Q 접두사 사용",
                initialOpened = true,
                prefix = {
                    BoardRow.Prefix(
                        text = "Q",
                        color = WalkLogColor.Secondary,
                        fontWeight = FontWeight.Bold,
                    )
                },
            ) {
                BoardRow.Text(text = "질문에 대한 답변입니다.")
            }

            BoardRow(
                title = "A 접두사 사용",
                prefix = {
                    BoardRow.Prefix(
                        text = "A",
                        color = WalkLogColor.Success,
                        fontWeight = FontWeight.Bold,
                    )
                },
            ) {
                BoardRow.Text(text = "추가 정보입니다.")
            }
        }
    }
}

@walklogPreview
@Composable
private fun PreviewBoardRowNoArrow() {
    BasePreview {
        BoardRow(
            title = "화살표 아이콘 없음",
            showArrow = false,
            prefix = { BoardRow.Prefix(text = "Q") },
        ) {
            BoardRow.Text(text = "화살표 없이 사용할 수도 있습니다.")
        }
    }
}
