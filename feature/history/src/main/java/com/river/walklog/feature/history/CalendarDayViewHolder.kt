package com.river.walklog.feature.history

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.river.walklog.core.designsystem.foundation.PremiumFlatColors
import com.river.walklog.feature.history.databinding.ItemCalendarDayBinding
import java.time.LocalDate
import com.river.walklog.core.designsystem.R as DesignR

class CalendarDayViewHolder(
    private val binding: ItemCalendarDayBinding,
    private val onDayClick: (CalendarItem.Day) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: CalendarItem.Day, palette: PremiumFlatColors?) {
        val context = binding.root.context
        binding.tvDayNumber.text = item.dayNumber.toString()

        val primary = ContextCompat.getColor(context, DesignR.color.walklog_primary)
        val primaryDark = ContextCompat.getColor(context, DesignR.color.walklog_primary_dark)
        val textPrimary = ContextCompat.getColor(context, DesignR.color.walklog_text_primary)
        val textDisabled = ContextCompat.getColor(context, DesignR.color.walklog_text_disabled)
        val gray200 = ContextCompat.getColor(context, DesignR.color.walklog_gray_200)
        val gray300 = ContextCompat.getColor(context, DesignR.color.walklog_gray_300)
        val gray100 = ContextCompat.getColor(context, DesignR.color.walklog_gray_100)
        val density = context.resources.displayMetrics.density
        val stroke2dp = (2 * density).toInt()
        val stroke3dp = (3 * density).toInt()

        // 캘린더 그리드는 카드로 감싸이지 않고 배경 위에 바로 떠 있다. 프리미엄이 켜져 있으면
        // 시스템 라이트/다크 리소스 대신 현재 프리미엄 모드에 맞는 고정 색을 써야, NIGHT처럼
        // 배경이 어두워져도 숫자가 계속 보인다.
        val onRootText = palette?.onBackground ?: textPrimary
        val onRootMutedText = palette?.onBackgroundMuted ?: textDisabled
        val onRootMutedStroke = palette?.onBackgroundMuted ?: gray200
        // 지난 날짜(기록 없음) 원은 카드와 같은 톤을 쓴다 — NIGHT는 어두운 카드이므로 그 위
        // 텍스트/테두리도 팔레트의 "카드 위" 색을 따라 밝게 뒤집혀야 한다.
        val pastFill = palette?.cardBackground ?: gray100
        val pastText = palette?.onCardMuted ?: textDisabled
        val pastStroke = palette?.cardOutline ?: gray300

        val isPast = item.dateEpochDay < LocalDate.now().toEpochDay()
        val circle = GradientDrawable().apply { shape = GradientDrawable.OVAL }

        when {
            item.isAchieved -> {
                circle.setColor(primary)
                if (item.isSelected) circle.setStroke(stroke3dp, primaryDark)
                binding.tvDayNumber.setTextColor(textPrimary)
            }
            item.hasData -> {
                val fraction = (item.steps.toFloat() / item.targetSteps).coerceIn(0f, 0.99f)
                val alpha = (80 + (fraction * 155).toInt()).coerceIn(80, 230)
                circle.setColor(withAlpha(primary, alpha))
                if (item.isSelected) circle.setStroke(stroke3dp, primary)
                binding.tvDayNumber.setTextColor(textPrimary)
            }
            item.isToday -> {
                circle.setColor(Color.TRANSPARENT)
                circle.setStroke(
                    if (item.isSelected) stroke3dp else stroke2dp,
                    if (item.isSelected) primary else onRootMutedStroke,
                )
                binding.tvDayNumber.setTextColor(onRootText)
            }
            isPast -> {
                circle.setColor(pastFill)
                if (item.isSelected) circle.setStroke(stroke2dp, pastStroke)
                binding.tvDayNumber.setTextColor(pastText)
            }
            else -> {
                circle.setColor(Color.TRANSPARENT)
                if (item.isSelected) circle.setStroke(stroke2dp, onRootMutedStroke)
                binding.tvDayNumber.setTextColor(onRootMutedText)
            }
        }

        val rippleColor = ColorStateList.valueOf(Color.argb(50, 0, 0, 0))
        val mask = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.WHITE)
        }
        binding.viewCircleBg.background = RippleDrawable(rippleColor, circle, mask)

        binding.root.setOnClickListener { onDayClick(item) }
        binding.root.contentDescription = "${item.dayNumber}일, ${item.steps}보"
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        (color and 0x00FFFFFF) or (alpha shl 24)
}
