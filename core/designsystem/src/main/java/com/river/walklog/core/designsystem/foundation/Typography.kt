package com.river.walklog.core.designsystem.foundation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.river.walklog.core.designsystem.R

/**
 * 워크로그 Pretendard 폰트 패밀리
 */
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
    Font(R.font.pretendard_extrabold, FontWeight.ExtraBold),
    Font(R.font.pretendard_black, FontWeight.Black),
)

private val Base = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.sp,
)

/**
 * 워크로그 Typography
 */
internal val Typography = WalkLogTypography(
    // Typography 1 - 매우 큰 제목 (30/40)
    typography1R = Base.copy(fontSize = 30.sp, lineHeight = 40.sp),
    typography1M = Base.copy(fontSize = 30.sp, lineHeight = 40.sp, fontWeight = FontWeight.Medium),
    typography1SB = Base.copy(fontSize = 30.sp, lineHeight = 40.sp, fontWeight = FontWeight.SemiBold),
    typography1B = Base.copy(fontSize = 30.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),

    // Sub Typography 1-3
    subTypography1R = Base.copy(fontSize = 29.sp, lineHeight = 38.sp),
    subTypography1M = Base.copy(fontSize = 29.sp, lineHeight = 38.sp, fontWeight = FontWeight.Medium),
    subTypography1SB = Base.copy(fontSize = 29.sp, lineHeight = 38.sp, fontWeight = FontWeight.SemiBold),
    subTypography1B = Base.copy(fontSize = 29.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold),

    subTypography2R = Base.copy(fontSize = 28.sp, lineHeight = 37.sp),
    subTypography2M = Base.copy(fontSize = 28.sp, lineHeight = 37.sp, fontWeight = FontWeight.Medium),
    subTypography2SB = Base.copy(fontSize = 28.sp, lineHeight = 37.sp, fontWeight = FontWeight.SemiBold),
    subTypography2B = Base.copy(fontSize = 28.sp, lineHeight = 37.sp, fontWeight = FontWeight.Bold),

    subTypography3R = Base.copy(fontSize = 27.sp, lineHeight = 36.sp),
    subTypography3M = Base.copy(fontSize = 27.sp, lineHeight = 36.sp, fontWeight = FontWeight.Medium),
    subTypography3SB = Base.copy(fontSize = 27.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
    subTypography3B = Base.copy(fontSize = 27.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),

    // Typography 2 - 큰 제목 (26/35)
    typography2R = Base.copy(fontSize = 26.sp, lineHeight = 35.sp),
    typography2M = Base.copy(fontSize = 26.sp, lineHeight = 35.sp, fontWeight = FontWeight.Medium),
    typography2SB = Base.copy(fontSize = 26.sp, lineHeight = 35.sp, fontWeight = FontWeight.SemiBold),
    typography2B = Base.copy(fontSize = 26.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold),

    // Sub Typography 4-5
    subTypography4R = Base.copy(fontSize = 25.sp, lineHeight = 34.sp),
    subTypography4M = Base.copy(fontSize = 25.sp, lineHeight = 34.sp, fontWeight = FontWeight.Medium),
    subTypography4SB = Base.copy(fontSize = 25.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold),
    subTypography4B = Base.copy(fontSize = 25.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),

    subTypography5R = Base.copy(fontSize = 24.sp, lineHeight = 33.sp),
    subTypography5M = Base.copy(fontSize = 24.sp, lineHeight = 33.sp, fontWeight = FontWeight.Medium),
    subTypography5SB = Base.copy(fontSize = 24.sp, lineHeight = 33.sp, fontWeight = FontWeight.SemiBold),
    subTypography5B = Base.copy(fontSize = 24.sp, lineHeight = 33.sp, fontWeight = FontWeight.Bold),

    // Sub Typography 6 - 조금 큰 제목 (23/32)
    subTypography6R = Base.copy(fontSize = 23.sp, lineHeight = 32.sp),
    subTypography6M = Base.copy(fontSize = 23.sp, lineHeight = 32.sp, fontWeight = FontWeight.Medium),
    subTypography6SB = Base.copy(fontSize = 23.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    subTypography6B = Base.copy(fontSize = 23.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),

    // Typography 3 - 일반 제목 (22/31)
    typography3R = Base.copy(fontSize = 22.sp, lineHeight = 31.sp),
    typography3M = Base.copy(fontSize = 22.sp, lineHeight = 31.sp, fontWeight = FontWeight.Medium),
    typography3SB = Base.copy(fontSize = 22.sp, lineHeight = 31.sp, fontWeight = FontWeight.SemiBold),
    typography3B = Base.copy(fontSize = 22.sp, lineHeight = 31.sp, fontWeight = FontWeight.Bold),

    // Sub Typography 7
    subTypography7R = Base.copy(fontSize = 21.sp, lineHeight = 30.sp),
    subTypography7M = Base.copy(fontSize = 21.sp, lineHeight = 30.sp, fontWeight = FontWeight.Medium),
    subTypography7SB = Base.copy(fontSize = 21.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold),
    subTypography7B = Base.copy(fontSize = 21.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold),

    // Typography 4 - 작은 제목 (20/29)
    typography4R = Base.copy(fontSize = 20.sp, lineHeight = 29.sp),
    typography4M = Base.copy(fontSize = 20.sp, lineHeight = 29.sp, fontWeight = FontWeight.Medium),
    typography4SB = Base.copy(fontSize = 20.sp, lineHeight = 29.sp, fontWeight = FontWeight.SemiBold),
    typography4B = Base.copy(fontSize = 20.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold),

    // Sub Typography 8-9 - 조금 큰 본문
    subTypography8R = Base.copy(fontSize = 19.sp, lineHeight = 28.sp),
    subTypography8M = Base.copy(fontSize = 19.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
    subTypography8SB = Base.copy(fontSize = 19.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    subTypography8B = Base.copy(fontSize = 19.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),

    subTypography9R = Base.copy(fontSize = 18.sp, lineHeight = 27.sp),
    subTypography9M = Base.copy(fontSize = 18.sp, lineHeight = 27.sp, fontWeight = FontWeight.Medium),
    subTypography9SB = Base.copy(fontSize = 18.sp, lineHeight = 27.sp, fontWeight = FontWeight.SemiBold),
    subTypography9B = Base.copy(fontSize = 18.sp, lineHeight = 27.sp, fontWeight = FontWeight.Bold),

    // Typography 5 - 일반 본문 (17/25.5)
    typography5R = Base.copy(fontSize = 17.sp, lineHeight = 25.5.sp),
    typography5M = Base.copy(fontSize = 17.sp, lineHeight = 25.5.sp, fontWeight = FontWeight.Medium),
    typography5SB = Base.copy(fontSize = 17.sp, lineHeight = 25.5.sp, fontWeight = FontWeight.SemiBold),
    typography5B = Base.copy(fontSize = 17.sp, lineHeight = 25.5.sp, fontWeight = FontWeight.Bold),

    // Sub Typography 10
    subTypography10R = Base.copy(fontSize = 16.sp, lineHeight = 24.sp),
    subTypography10M = Base.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    subTypography10SB = Base.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    subTypography10B = Base.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold),

    // Typography 6 - 작은 본문 (15/22.5)
    typography6R = Base.copy(fontSize = 15.sp, lineHeight = 22.5.sp),
    typography6M = Base.copy(fontSize = 15.sp, lineHeight = 22.5.sp, fontWeight = FontWeight.Medium),
    typography6SB = Base.copy(fontSize = 15.sp, lineHeight = 22.5.sp, fontWeight = FontWeight.SemiBold),
    typography6B = Base.copy(fontSize = 15.sp, lineHeight = 22.5.sp, fontWeight = FontWeight.Bold),

    // Sub Typography 11
    subTypography11R = Base.copy(fontSize = 14.sp, lineHeight = 21.sp),
    subTypography11M = Base.copy(fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Medium),
    subTypography11SB = Base.copy(fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold),
    subTypography11B = Base.copy(fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold),

    // Typography 7 - 안 읽어도 됨 (13/19.5)
    typography7R = Base.copy(fontSize = 13.sp, lineHeight = 19.5.sp),
    typography7M = Base.copy(fontSize = 13.sp, lineHeight = 19.5.sp, fontWeight = FontWeight.Medium),
    typography7SB = Base.copy(fontSize = 13.sp, lineHeight = 19.5.sp, fontWeight = FontWeight.SemiBold),
    typography7B = Base.copy(fontSize = 13.sp, lineHeight = 19.5.sp, fontWeight = FontWeight.Bold),

    // Sub Typography 12-13 - 아예 안읽어도 됨
    subTypography12R = Base.copy(fontSize = 12.sp, lineHeight = 18.sp),
    subTypography12M = Base.copy(fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
    subTypography12SB = Base.copy(fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
    subTypography12B = Base.copy(fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold),

    subTypography13R = Base.copy(fontSize = 11.sp, lineHeight = 16.5.sp),
    subTypography13M = Base.copy(fontSize = 11.sp, lineHeight = 16.5.sp, fontWeight = FontWeight.Medium),
    subTypography13SB = Base.copy(fontSize = 11.sp, lineHeight = 16.5.sp, fontWeight = FontWeight.SemiBold),
    subTypography13B = Base.copy(fontSize = 11.sp, lineHeight = 16.5.sp, fontWeight = FontWeight.Bold),
)

internal fun WalkLogTypography.scale(factor: Float): WalkLogTypography {
    fun TextStyle.s(): TextStyle {
        val scaled = (fontSize.value * factor).sp
        val scaledLine = (lineHeight.value * factor).sp
        return copy(fontSize = scaled, lineHeight = scaledLine)
    }
    return copy(
        typography1R = typography1R.s(), typography1M = typography1M.s(),
        typography1SB = typography1SB.s(), typography1B = typography1B.s(),
        subTypography1R = subTypography1R.s(), subTypography1M = subTypography1M.s(),
        subTypography1SB = subTypography1SB.s(), subTypography1B = subTypography1B.s(),
        subTypography2R = subTypography2R.s(), subTypography2M = subTypography2M.s(),
        subTypography2SB = subTypography2SB.s(), subTypography2B = subTypography2B.s(),
        subTypography3R = subTypography3R.s(), subTypography3M = subTypography3M.s(),
        subTypography3SB = subTypography3SB.s(), subTypography3B = subTypography3B.s(),
        typography2R = typography2R.s(), typography2M = typography2M.s(),
        typography2SB = typography2SB.s(), typography2B = typography2B.s(),
        subTypography4R = subTypography4R.s(), subTypography4M = subTypography4M.s(),
        subTypography4SB = subTypography4SB.s(), subTypography4B = subTypography4B.s(),
        subTypography5R = subTypography5R.s(), subTypography5M = subTypography5M.s(),
        subTypography5SB = subTypography5SB.s(), subTypography5B = subTypography5B.s(),
        subTypography6R = subTypography6R.s(), subTypography6M = subTypography6M.s(),
        subTypography6SB = subTypography6SB.s(), subTypography6B = subTypography6B.s(),
        typography3R = typography3R.s(), typography3M = typography3M.s(),
        typography3SB = typography3SB.s(), typography3B = typography3B.s(),
        subTypography7R = subTypography7R.s(), subTypography7M = subTypography7M.s(),
        subTypography7SB = subTypography7SB.s(), subTypography7B = subTypography7B.s(),
        typography4R = typography4R.s(), typography4M = typography4M.s(),
        typography4SB = typography4SB.s(), typography4B = typography4B.s(),
        subTypography8R = subTypography8R.s(), subTypography8M = subTypography8M.s(),
        subTypography8SB = subTypography8SB.s(), subTypography8B = subTypography8B.s(),
        subTypography9R = subTypography9R.s(), subTypography9M = subTypography9M.s(),
        subTypography9SB = subTypography9SB.s(), subTypography9B = subTypography9B.s(),
        typography5R = typography5R.s(), typography5M = typography5M.s(),
        typography5SB = typography5SB.s(), typography5B = typography5B.s(),
        subTypography10R = subTypography10R.s(), subTypography10M = subTypography10M.s(),
        subTypography10SB = subTypography10SB.s(), subTypography10B = subTypography10B.s(),
        typography6R = typography6R.s(), typography6M = typography6M.s(),
        typography6SB = typography6SB.s(), typography6B = typography6B.s(),
        subTypography11R = subTypography11R.s(), subTypography11M = subTypography11M.s(),
        subTypography11SB = subTypography11SB.s(), subTypography11B = subTypography11B.s(),
        typography7R = typography7R.s(), typography7M = typography7M.s(),
        typography7SB = typography7SB.s(), typography7B = typography7B.s(),
        subTypography12R = subTypography12R.s(), subTypography12M = subTypography12M.s(),
        subTypography12SB = subTypography12SB.s(), subTypography12B = subTypography12B.s(),
        subTypography13R = subTypography13R.s(), subTypography13M = subTypography13M.s(),
        subTypography13SB = subTypography13SB.s(), subTypography13B = subTypography13B.s(),
    )
}

/**
 * 워크로그 Typography 토큰
 *
 * 계층 구조:
 * - Typography 1-7: 주요 텍스트 계층 (매우 큰 제목 ~ 작은 라벨)
 * - Sub Typography 1-13: 보조 텍스트 계층
 *
 * Weight 변형:
 * - R (Regular): 일반
 * - M (Medium): 중간
 * - SB (SemiBold): 준굵게
 * - B (Bold): 굵게
 */
@Immutable
data class WalkLogTypography(
    // Typography 1 - 매우 큰 제목 (30/40)
    val typography1R: TextStyle,
    val typography1M: TextStyle,
    val typography1SB: TextStyle,
    val typography1B: TextStyle,

    // Sub Typography 1 (29/38)
    val subTypography1R: TextStyle,
    val subTypography1M: TextStyle,
    val subTypography1SB: TextStyle,
    val subTypography1B: TextStyle,

    // Sub Typography 2 (28/37)
    val subTypography2R: TextStyle,
    val subTypography2M: TextStyle,
    val subTypography2SB: TextStyle,
    val subTypography2B: TextStyle,

    // Sub Typography 3 (27/36)
    val subTypography3R: TextStyle,
    val subTypography3M: TextStyle,
    val subTypography3SB: TextStyle,
    val subTypography3B: TextStyle,

    // Typography 2 - 큰 제목 (26/35)
    val typography2R: TextStyle,
    val typography2M: TextStyle,
    val typography2SB: TextStyle,
    val typography2B: TextStyle,

    // Sub Typography 4 (25/34)
    val subTypography4R: TextStyle,
    val subTypography4M: TextStyle,
    val subTypography4SB: TextStyle,
    val subTypography4B: TextStyle,

    // Sub Typography 5 - 조금 큰 제목 (24/33)
    val subTypography5R: TextStyle,
    val subTypography5M: TextStyle,
    val subTypography5SB: TextStyle,
    val subTypography5B: TextStyle,

    // Sub Typography 6 (23/32)
    val subTypography6R: TextStyle,
    val subTypography6M: TextStyle,
    val subTypography6SB: TextStyle,
    val subTypography6B: TextStyle,

    // Typography 3 - 일반 제목 (22/31)
    val typography3R: TextStyle,
    val typography3M: TextStyle,
    val typography3SB: TextStyle,
    val typography3B: TextStyle,

    // Sub Typography 7 (21/30)
    val subTypography7R: TextStyle,
    val subTypography7M: TextStyle,
    val subTypography7SB: TextStyle,
    val subTypography7B: TextStyle,

    // Typography 4 - 작은 제목 (20/29)
    val typography4R: TextStyle,
    val typography4M: TextStyle,
    val typography4SB: TextStyle,
    val typography4B: TextStyle,

    // Sub Typography 8 - 조금 큰 본문 (19/28)
    val subTypography8R: TextStyle,
    val subTypography8M: TextStyle,
    val subTypography8SB: TextStyle,
    val subTypography8B: TextStyle,

    // Sub Typography 9 (18/27)
    val subTypography9R: TextStyle,
    val subTypography9M: TextStyle,
    val subTypography9SB: TextStyle,
    val subTypography9B: TextStyle,

    // Typography 5 - 일반 본문 (17/25.5)
    val typography5R: TextStyle,
    val typography5M: TextStyle,
    val typography5SB: TextStyle,
    val typography5B: TextStyle,

    // Sub Typography 10 (16/24)
    val subTypography10R: TextStyle,
    val subTypography10M: TextStyle,
    val subTypography10SB: TextStyle,
    val subTypography10B: TextStyle,

    // Typography 6 - 작은 본문 (15/22.5)
    val typography6R: TextStyle,
    val typography6M: TextStyle,
    val typography6SB: TextStyle,
    val typography6B: TextStyle,

    // Sub Typography 11 (14/21)
    val subTypography11R: TextStyle,
    val subTypography11M: TextStyle,
    val subTypography11SB: TextStyle,
    val subTypography11B: TextStyle,

    // Typography 7 - 안 읽어도 됨 (13/19.5)
    val typography7R: TextStyle,
    val typography7M: TextStyle,
    val typography7SB: TextStyle,
    val typography7B: TextStyle,

    // Sub Typography 12 (12/18)
    val subTypography12R: TextStyle,
    val subTypography12M: TextStyle,
    val subTypography12SB: TextStyle,
    val subTypography12B: TextStyle,

    // Sub Typography 13 - 아예 안읽어도 됨 (11/16.5)
    val subTypography13R: TextStyle,
    val subTypography13M: TextStyle,
    val subTypography13SB: TextStyle,
    val subTypography13B: TextStyle,
)
