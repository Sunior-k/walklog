package com.river.walklog.feature.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.river.walklog.core.designsystem.foundation.PremiumFlatColors
import com.river.walklog.feature.history.databinding.ItemCalendarDayBinding
import com.river.walklog.feature.history.databinding.ItemCalendarEmptyBinding
import com.river.walklog.feature.history.databinding.ItemCalendarHeaderBinding
import java.time.DayOfWeek
import java.time.format.TextStyle

class CalendarAdapter(
    private val onDayClick: (CalendarItem.Day) -> Unit,
) : ListAdapter<CalendarItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private var premiumPalette: PremiumFlatColors? = null

    /** 프리미엄 모드가 바뀔 때 이미 바인딩된 날짜 셀도 즉시 새 팔레트로 다시 칠하도록 강제 재바인딩한다. */
    fun setPremiumPalette(palette: PremiumFlatColors?) {
        if (premiumPalette == palette) return
        premiumPalette = palette
        notifyItemRangeChanged(0, itemCount)
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is CalendarItem.DayLabel -> TYPE_LABEL
        is CalendarItem.Empty -> TYPE_EMPTY
        is CalendarItem.Day -> TYPE_DAY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_LABEL -> LabelViewHolder(ItemCalendarHeaderBinding.inflate(inflater, parent, false))
            TYPE_EMPTY -> EmptyViewHolder(ItemCalendarEmptyBinding.inflate(inflater, parent, false))
            TYPE_DAY -> CalendarDayViewHolder(
                ItemCalendarDayBinding.inflate(inflater, parent, false),
                onDayClick,
            )
            else -> error("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CalendarItem.DayLabel -> (holder as LabelViewHolder).bind(item)
            is CalendarItem.Empty -> Unit
            is CalendarItem.Day -> (holder as CalendarDayViewHolder).bind(item, premiumPalette)
        }
    }

    inner class LabelViewHolder(
        private val binding: ItemCalendarHeaderBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CalendarItem.DayLabel) {
            val locale = binding.root.context.resources.configuration.locales[0]
            binding.tvDayOfWeek.text = DayOfWeek.of(item.dayOfWeek).getDisplayName(TextStyle.SHORT, locale)
        }
    }

    inner class EmptyViewHolder(
        binding: ItemCalendarEmptyBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val TYPE_LABEL = 0
        private const val TYPE_EMPTY = 1
        private const val TYPE_DAY = 2

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CalendarItem>() {
            override fun areItemsTheSame(oldItem: CalendarItem, newItem: CalendarItem): Boolean =
                when {
                    oldItem is CalendarItem.DayLabel && newItem is CalendarItem.DayLabel ->
                        oldItem.dayOfWeek == newItem.dayOfWeek
                    oldItem is CalendarItem.Empty && newItem is CalendarItem.Empty ->
                        oldItem.index == newItem.index
                    oldItem is CalendarItem.Day && newItem is CalendarItem.Day ->
                        oldItem.dateEpochDay == newItem.dateEpochDay
                    else -> false
                }

            override fun areContentsTheSame(oldItem: CalendarItem, newItem: CalendarItem): Boolean =
                oldItem == newItem
        }
    }
}
