package com.river.walklog.feature.history

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.river.walklog.feature.history.databinding.ItemCalendarDayBinding
import java.time.LocalDate
import com.river.walklog.core.designsystem.R as DesignR

class CalendarDayViewHolder(
    private val binding: ItemCalendarDayBinding,
    private val onDayClick: (CalendarItem.Day) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: CalendarItem.Day) {
        val context = binding.root.context
        binding.tvDayNumber.text = item.dayNumber.toString()

        val primary = ContextCompat.getColor(context, DesignR.color.walklog_primary)
        val primaryDark = ContextCompat.getColor(context, DesignR.color.walklog_primary_dark)
        val textPrimary = ContextCompat.getColor(context, DesignR.color.walklog_text_primary)
        val textDisabled = ContextCompat.getColor(context, DesignR.color.walklog_text_disabled)
        val gray100 = ContextCompat.getColor(context, DesignR.color.walklog_gray_100)
        val gray200 = ContextCompat.getColor(context, DesignR.color.walklog_gray_200)
        val gray300 = ContextCompat.getColor(context, DesignR.color.walklog_gray_300)
        val density = context.resources.displayMetrics.density
        val stroke2dp = (2 * density).toInt()
        val stroke3dp = (3 * density).toInt()

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
                    if (item.isSelected) primary else gray200,
                )
                binding.tvDayNumber.setTextColor(textPrimary)
            }
            isPast -> {
                circle.setColor(gray100)
                if (item.isSelected) circle.setStroke(stroke2dp, gray300)
                binding.tvDayNumber.setTextColor(textDisabled)
            }
            else -> {
                circle.setColor(Color.TRANSPARENT)
                if (item.isSelected) circle.setStroke(stroke2dp, gray200)
                binding.tvDayNumber.setTextColor(textDisabled)
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
