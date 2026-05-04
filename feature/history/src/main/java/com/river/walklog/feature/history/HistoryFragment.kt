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
import com.river.walklog.feature.history.databinding.FragmentHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
        tvMonthLabel.text = state.monthLabel
        btnPrevMonth.isEnabled = state.canNavigateBack
        btnNextMonth.isEnabled = state.canNavigateForward
        tvTotalSteps.text = state.totalStepsText
        tvAchievementRate.text = state.achievementRateText

        val isLoading = state.isLoading
        val isEmpty = state.isEmpty
        val showContent = !isLoading && !isEmpty

        state.selectedDaySummary?.let { summary ->
            tvSelectedDate.text = summary.dateText
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
                tvSelectedSteps.text = summary.stepsText
                tvSelectedCalories.text = summary.caloriesText
                tvSelectedDistance.text = summary.distanceText
                pbGoalProgress.progress = (summary.achievementFraction * 100).toInt()
                applyProgressBarStyle(summary.isAchieved)
                applyComparisonStyle(summary)
                applyInsightStyle(summary)
                applyTimelineStyle(summary)
            }
        }

        if (isExpanded) {
            selectedDayContainer.isVisible = showContent
            val hasSelection = state.selectedDaySummary != null
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
                "목표 달성 ✓",
            )
            summary.hasData -> Triple(
                ContextCompat.getColor(ctx, DesignR.color.walklog_primary_container),
                ContextCompat.getColor(ctx, DesignR.color.walklog_primary_dark),
                summary.targetStatusText,
            )
            else -> Triple(
                ContextCompat.getColor(ctx, DesignR.color.walklog_gray_100),
                ContextCompat.getColor(ctx, DesignR.color.walklog_gray_400),
                "기록 없음",
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
        tvSelectedComparison.text = "$prefix${summary.comparisonText}"
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
        insightContainer.isVisible = summary.hasData && summary.insightText.isNotBlank()
        if (!insightContainer.isVisible) return

        tvSelectedInsight?.text = summary.insightText
        tvSelectedMonthRank?.text = summary.monthRankText
        tvSelectedInsightSteps?.text = "${summary.stepsText}보"
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
            labelView.text = segment.label
            stepsView.text = segment.stepsText
            progressView.apply {
                progress = (segment.fraction * 100).toInt()
                progressTintList = progressTint
                progressBackgroundTintList = trackTint
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
