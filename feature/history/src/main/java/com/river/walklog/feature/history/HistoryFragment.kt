package com.river.walklog.feature.history

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.river.walklog.core.ui.withComma
import com.river.walklog.feature.history.databinding.FragmentHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import com.river.walklog.core.designsystem.R as DesignR

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val isExpanded get() = resources.configuration.screenWidthDp >= 600

    private val viewModel: HistoryViewModel by viewModels()
    private val calendarAdapter = CalendarAdapter { day ->
        viewModel.onDaySelected(day.dateEpochDay)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyStatusBarInsets()
        setupRecyclerView()
        setupClickListeners()
        observeState()
    }

    private fun applyStatusBarInsets() {
        val initialTopPadding = binding.root.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updatePadding(top = initialTopPadding + statusBarTop)
            insets
        }
    }

    private fun setupRecyclerView() {
        binding.rvCalendar.apply {
            adapter = calendarAdapter
            layoutManager = GridLayoutManager(requireContext(), 7)
            itemAnimator = null
        }
    }

    private fun setupClickListeners() {
        binding.btnPrevMonth.setOnClickListener { viewModel.onPreviousMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.onNextMonth() }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: HistoryState) = with(binding) {
        tvMonthLabel.text = state.yearMonth.formatWithPattern(R.string.history_month_format_pattern)
        btnPrevMonth.isEnabled = state.canNavigateBack
        btnNextMonth.isEnabled = state.canNavigateForward
        tvTotalSteps.text = getString(R.string.total_steps_format, state.totalSteps.withComma())
        tvAchievementRate.text = state.achievementRateText

        val isLoading = state.isLoading
        val isEmpty = state.isEmpty
        val showContent = !isLoading && !isEmpty

        val hasSelection = state.selectedDaySummary != null
        val selectedDateHeader = tvSelectedDate.parent as? View
        selectedDateHeader?.isVisible = hasSelection
        if (!hasSelection) {
            groupHasData.isVisible = false
            groupNoData.isVisible = false
        }

        state.selectedDaySummary?.let { summary ->
            tvSelectedDate.text = LocalDate.ofEpochDay(summary.dateEpochDay)
                .formatWithPattern(R.string.history_date_format_pattern)
            applyChipStyle(summary)

            groupHasData.isVisible = summary.hasData
            groupNoData.isVisible = !summary.hasData

            if (!summary.hasData) {
                if (summary.isPastDay) {
                    tvNoDataMessage.setText(R.string.no_data_past_message)
                    tvNoDataSubMessage.setText(R.string.no_data_past_sub_message)
                } else {
                    tvNoDataMessage.setText(R.string.no_data_today_message)
                    tvNoDataSubMessage.setText(R.string.no_data_today_sub_message)
                }
            }

            if (summary.hasData) {
                tvSelectedSteps.text = summary.steps.withComma()
                tvSelectedCalories.text = "${summary.calories.withComma()} kcal"
                tvSelectedDistance.text = "${summary.distanceKm.formatDistance()} km"
                pbGoalProgress.progress = (summary.achievementFraction * 100).toInt()
                applyProgressBarStyle(summary.isAchieved)
                applyComparisonStyle(summary)
                applyInsightStyle(summary)
                applyTimelineStyle(summary)
            }
        }

        if (isExpanded) {
            selectedDayContainer.isVisible = showContent
            binding.groupSelectPrompt?.isVisible = showContent && !hasSelection
        } else {
            selectedDayContainer.isVisible = showContent && state.selectedDaySummary != null
        }
        progressBar.isVisible = isLoading
        if (!showContent) rvCalendar.isVisible = false
        emptyState.isVisible = !isLoading && isEmpty
        statsContainer.isVisible = showContent

        calendarAdapter.submitList(state.items) {
            rvCalendar.isVisible = showContent
        }
    }

    private fun applyChipStyle(summary: SelectedDaySummary) = with(binding) {
        val ctx = requireContext()
        val (bgColor, textColor, text) = when {
            summary.isAchieved -> Triple(
                ContextCompat.getColor(ctx, DesignR.color.walklog_success_container),
                ContextCompat.getColor(ctx, DesignR.color.walklog_success_dark),
                getString(R.string.chip_achieved),
            )
            summary.hasData -> Triple(
                ContextCompat.getColor(ctx, DesignR.color.walklog_primary_container),
                ContextCompat.getColor(ctx, DesignR.color.walklog_primary_dark),
                summary.targetStatusText(),
            )
            else -> Triple(
                ContextCompat.getColor(ctx, DesignR.color.walklog_gray_100),
                ContextCompat.getColor(ctx, DesignR.color.walklog_gray_400),
                getString(R.string.chip_no_record),
            )
        }
        tvSelectedTargetStatus.text = text
        tvSelectedTargetStatus.setTextColor(textColor)
        tvSelectedTargetStatus.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = resources.displayMetrics.density * 12
            setColor(bgColor)
        }
    }

    private fun applyComparisonStyle(summary: SelectedDaySummary) = with(binding) {
        val ctx = requireContext()
        val show = summary.hasData && summary.comparisonSign != null
        tvSelectedComparison.isVisible = show
        if (!show) return

        val (color, prefix) = when (summary.comparisonSign) {
            1 -> ContextCompat.getColor(ctx, DesignR.color.walklog_success) to "↑ "
            -1 -> ContextCompat.getColor(ctx, DesignR.color.walklog_error) to "↓ "
            else -> ContextCompat.getColor(ctx, DesignR.color.walklog_gray_400) to ""
        }
        tvSelectedComparison.setTextColor(color)
        tvSelectedComparison.text = "$prefix${summary.comparisonText()}"
    }

    private fun applyProgressBarStyle(isAchieved: Boolean) = with(binding) {
        val ctx = requireContext()
        val fillColor = if (isAchieved) {
            ContextCompat.getColor(ctx, DesignR.color.walklog_success)
        } else {
            ContextCompat.getColor(ctx, DesignR.color.walklog_primary)
        }
        pbGoalProgress.progressTintList = ColorStateList.valueOf(fillColor)
        pbGoalProgress.progressBackgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(ctx, DesignR.color.walklog_gray_100),
        )
    }

    private fun applyInsightStyle(summary: SelectedDaySummary) = with(binding) {
        val insightContainer = groupSelectedInsight ?: return
        insightContainer.isVisible = summary.hasData
        if (!insightContainer.isVisible) return

        tvSelectedInsight?.text = summary.insightText()
        tvSelectedMonthRank?.text = summary.monthRankText()
        tvSelectedInsightSteps?.text = getString(R.string.total_steps_format, summary.steps.withComma())
        tvSelectedInsightGoal?.text = "${(summary.achievementFraction * 100).toInt()}%"
    }

    private fun applyTimelineStyle(summary: SelectedDaySummary) = with(binding) {
        val timelineContainer = groupSelectedTimeline ?: return
        val segments = summary.timelineSegments
        timelineContainer.isVisible = summary.hasData && segments.size >= 3
        if (!timelineContainer.isVisible) return

        val ctx = requireContext()
        val progressTint = ColorStateList.valueOf(ContextCompat.getColor(ctx, DesignR.color.walklog_primary))
        val trackTint = ColorStateList.valueOf(ContextCompat.getColor(ctx, DesignR.color.walklog_gray_100))

        listOf(
            Triple(tvTimelineMorningLabel, tvTimelineMorningSteps, pbTimelineMorning),
            Triple(tvTimelineAfternoonLabel, tvTimelineAfternoonSteps, pbTimelineAfternoon),
            Triple(tvTimelineEveningLabel, tvTimelineEveningSteps, pbTimelineEvening),
        ).zip(segments).forEach { (views, segment) ->
            val (labelView, stepsView, progressView) = views
            labelView.text = segment.labelText()
            stepsView.text = getString(R.string.timeline_steps_format, segment.steps.withComma())
            progressView.apply {
                progress = (segment.fraction * 100).toInt()
                progressTintList = progressTint
                progressBackgroundTintList = trackTint
            }
        }
    }

    private fun TemporalAccessor.formatWithPattern(patternResId: Int): String =
        DateTimeFormatter.ofPattern(getString(patternResId), resources.configuration.locales[0]).format(this)

    private fun Number.formatDistance(): String =
        NumberFormat.getNumberInstance(resources.configuration.locales[0]).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
        }.format(toDouble())

    private fun SelectedDaySummary.targetStatusText(): String = when {
        !hasData -> getString(R.string.target_status_no_data)
        isAchieved -> getString(R.string.target_status_achieved)
        else -> getString(R.string.target_status_remaining, remainingSteps.withComma())
    }

    private fun SelectedDaySummary.comparisonText(): String {
        if (!hasData) return getString(R.string.comparison_no_data)
        val diff = comparisonDiff ?: return getString(R.string.comparison_no_prev_data)
        return when {
            diff > 0 -> getString(R.string.comparison_positive, diff.withComma())
            diff < 0 -> getString(R.string.comparison_negative, (-diff).withComma())
            else -> getString(R.string.comparison_same)
        }
    }

    private fun SelectedDaySummary.insightText(): String {
        if (!hasData) return getString(R.string.insight_no_data)
        val diff = comparisonDiff
        return when {
            isAchieved && diff != null && diff > 0 ->
                getString(R.string.insight_achieved_with_increase, diff.withComma())
            isAchieved -> getString(R.string.insight_achieved)
            diff != null && diff > 0 -> getString(R.string.insight_increase, diff.withComma())
            diff != null && diff < 0 -> getString(R.string.insight_decrease, (-diff).withComma())
            else -> getString(R.string.insight_remaining, remainingSteps.withComma())
        }
    }

    private fun SelectedDaySummary.monthRankText(): String =
        monthRank?.let { getString(R.string.month_rank, it, activeDaysInMonth) }
            ?: getString(R.string.month_rank_none)

    private fun SelectedDayTimelineSegment.labelText(): String = getString(
        when (type) {
            TimelineSegmentType.Morning -> R.string.timeline_morning
            TimelineSegmentType.Afternoon -> R.string.timeline_afternoon
            TimelineSegmentType.Evening -> R.string.timeline_evening
        },
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
