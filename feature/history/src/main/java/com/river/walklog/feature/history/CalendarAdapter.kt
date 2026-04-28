package com.river.walklog.feature.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.river.walklog.feature.history.databinding.ItemCalendarDayBinding
import com.river.walklog.feature.history.databinding.ItemCalendarEmptyBinding
import com.river.walklog.feature.history.databinding.ItemCalendarHeaderBinding

class CalendarAdapter(
    private val onDayClick: (CalendarItem.Day) -> Unit,
) : ListAdapter<CalendarItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

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
            is CalendarItem.Day -> (holder as CalendarDayViewHolder).bind(item)
        }
    }

    inner class LabelViewHolder(
        private val binding: ItemCalendarHeaderBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CalendarItem.DayLabel) {
            binding.tvDayOfWeek.text = item.label
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
                        oldItem.label == newItem.label
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
