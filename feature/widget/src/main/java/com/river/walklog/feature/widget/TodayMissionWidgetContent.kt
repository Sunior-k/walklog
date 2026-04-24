package com.river.walklog.feature.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.river.walklog.core.designsystem.foundation.WalkLogColor

private val WidgetPrimary = ColorProvider(
    day = WalkLogColor.Primary,
    night = WalkLogColor.PrimaryLight,
)

private val WidgetTrack = ColorProvider(
    day = WalkLogColor.PrimaryContainer,
    night = WalkLogColor.PrimaryContainerDark,
)

@Composable
internal fun TodayMissionWidgetContent(
    currentSteps: Int,
    targetSteps: Int,
    isLoading: Boolean,
) {
    val context = LocalContext.current
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)

    val progress = if (targetSteps > 0) {
        (currentSteps.toFloat() / targetSteps).coerceIn(0f, 1f)
    } else {
        0f
    }
    val remainingSteps = (targetSteps - currentSteps).coerceAtLeast(0)
    val isAchieved = currentSteps >= targetSteps

    var rootModifier = GlanceModifier
        .fillMaxSize()
        .background(GlanceTheme.colors.surface)
        .padding(horizontal = 16.dp, vertical = 12.dp)

    if (launchIntent != null) {
        rootModifier = rootModifier.clickable(actionStartActivity(launchIntent))
    }

    Column(
        modifier = rootModifier,
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        // ── 헤더 ──────────────────────────────────────────────────────────────
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            Text(
                text = "워크로그",
                style = TextStyle(
                    color = WidgetPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "오늘 미션",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 11.sp,
                ),
            )
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        if (isLoading) {
            // ── 로딩 상태 ──────────────────────────────────────────────────────
            Text(
                text = "걸음 수 불러오는 중...",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 13.sp,
                ),
            )
        } else {
            // ── 걸음 수 ──────────────────────────────────────────────────────
            Text(
                text = "%,d보".format(currentSteps),
                style = TextStyle(
                    color = if (isAchieved) WidgetPrimary else GlanceTheme.colors.onSurface,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )

            Spacer(modifier = GlanceModifier.height(2.dp))

            Text(
                text = "목표 %,d보".format(targetSteps),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 11.sp,
                ),
            )

            Spacer(modifier = GlanceModifier.height(10.dp))

            // ── 프로그레스 바 ─────────────────────────────────────────────────
            // SizeMode.Exact 이므로 LocalSize.current 로 정확한 위젯 너비를 얻을 수 있다.
            // 양쪽 패딩(16.dp × 2)을 빼서 실제 콘텐츠 너비를 계산한다.
            val barWidth = LocalSize.current.width - 32.dp

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(WidgetTrack)
                    .cornerRadius(5.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (progress > 0f) {
                    Box(
                        modifier = GlanceModifier
                            .width((barWidth * progress).coerceAtLeast(10.dp))
                            .fillMaxHeight()
                            .background(WidgetPrimary)
                            .cornerRadius(5.dp),
                        content = {},
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // ── 상태 / 보상 ──────────────────────────────────────────────────
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                Text(
                    text = if (isAchieved) {
                        "오늘 목표 달성!"
                    } else {
                        "%,d보 남았어요".format(remainingSteps)
                    },
                    style = TextStyle(
                        color = if (isAchieved) WidgetPrimary else GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                    ),
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "+20 캐시",
                    style = TextStyle(
                        color = WidgetPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}
