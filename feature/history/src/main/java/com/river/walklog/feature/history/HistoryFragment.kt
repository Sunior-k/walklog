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
import com.river.walklog.core.designsystem.foundation.PremiumFlatColors
import com.river.walklog.core.designsystem.foundation.PremiumFlatPalette
import com.river.walklog.core.designsystem.foundation.setPremiumCardColor
import com.river.walklog.core.model.PremiumVisualMode
import com.river.walklog.core.ui.ThemeViewModel
import com.river.walklog.core.ui.withComma
import com.river.walklog.feature.history.databinding.FragmentHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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
    private val themeViewModel: ThemeViewModel by viewModels()
    private val calendarAdapter = CalendarAdapter { day ->
        viewModel.onDaySelected(day.dateEpochDay)
    }

    /** 프리미엄이 비활성이면 null — [applyChipStyle] 등 데이터 변경으로 트리거되는 렌더링에서도 참조한다. */
    private var premiumMode: PremiumVisualMode? = null
    private val premiumPalette: PremiumFlatColors?
        get() = premiumMode?.let { PremiumFlatPalette.forMode(it) }

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
        observePremiumTheme()
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

    private fun observePremiumTheme() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                combine(themeViewModel.isPremiumTheme, themeViewModel.premiumVisualMode) { active, mode ->
                    if (active) mode else null
                }.collectLatest { mode -> applyPremiumTheme(mode) }
            }
        }
    }

    private fun applyPremiumTheme(mode: PremiumVisualMode?) = with(binding) {
        val ctx = requireContext()
        val defaultBackground = ContextCompat.getColor(ctx, DesignR.color.walklog_background)
        val defaultStatsContainer = ContextCompat.getColor(ctx, DesignR.color.walklog_surface_variant)
        val defaultSelectedDayPanel = ContextCompat.getColor(ctx, DesignR.color.walklog_surface)
        val defaultInsightCard = ContextCompat.getColor(ctx, DesignR.color.walklog_surface_variant)
        val defaultInsightMetricCard = ContextCompat.getColor(ctx, DesignR.color.walklog_surface)
        val defaultEmptyIconCircle = ContextCompat.getColor(ctx, DesignR.color.walklog_gray_100)
        val defaultOnBackground = ContextCompat.getColor(ctx, DesignR.color.walklog_text_primary)
        val defaultOnBackgroundMuted = ContextCompat.getColor(ctx, DesignR.color.walklog_gray_400)
        val defaultOnCard = ContextCompat.getColor(ctx, DesignR.color.walklog_text_primary)
        val defaultCardOutline = ContextCompat.getColor(ctx, DesignR.color.walklog_gray_200)
        premiumMode = mode
        val palette = premiumPalette
        val cardColor = palette?.cardBackground

        root.setBackgroundColor(palette?.background ?: defaultBackground)
        statsContainer.setPremiumCardColor(cardColor ?: defaultStatsContainer)
        selectedDayContainer.setPremiumCardColor(cardColor ?: defaultSelectedDayPanel)
        groupSelectedInsight?.setPremiumCardColor(cardColor ?: defaultInsightCard)
        groupSelectedTimeline?.setPremiumCardColor(cardColor ?: defaultSelectedDayPanel)
        insightMetricStepsCard?.setPremiumCardColor(cardColor ?: defaultInsightMetricCard)
        insightMetricGoalCard?.setPremiumCardColor(cardColor ?: defaultInsightMetricCard)
        emptyIconCircle?.setPremiumCardColor(cardColor ?: defaultEmptyIconCircle)

        // 캘린더 그리드·월 헤더·빈 상태 텍스트는 카드로 감싸이지 않고 배경 위에 바로 놓여 있어,
        // 배경이 프리미엄 모드에 따라 밝고 어두움이 바뀌면 시스템 라이트/다크 기준의 고정 텍스트
        // 색으로는 안 보이는 경우가 생긴다 — 팔레트 색으로 직접 덮어써야 한다.
        val onBackground = palette?.onBackground ?: defaultOnBackground
        val onBackgroundMuted = palette?.onBackgroundMuted ?: defaultOnBackgroundMuted
        tvMonthLabel.setTextColor(onBackground)
        btnPrevMonth.setColorFilter(onBackgroundMuted)
        btnNextMonth.setColorFilter(onBackgroundMuted)
        tvEmptyTitle.setTextColor(onBackground)
        tvEmptyDescription.setTextColor(onBackgroundMuted)

        // 카드 위 주요 숫자/텍스트 — NIGHT는 카드 자체가 어두워지므로 밝은 색으로 뒤집혀야 한다.
        val onCard = palette?.onCard ?: defaultOnCard
        val cardOutline = palette?.cardOutline ?: defaultCardOutline
        tvTotalSteps.setTextColor(onCard)
        statsDivider.setBackgroundColor(cardOutline)
        tvSelectedSteps.setTextColor(onCard)
        tvSelectedInsight?.setTextColor(onCard)
        tvSelectedInsightSteps?.setTextColor(onCard)
        tvTimelineTitle?.setTextColor(onCard)
        tvTimelineMorningLabel.setTextColor(onCard)
        tvTimelineAfternoonLabel.setTextColor(onCard)
        tvTimelineEveningLabel.setTextColor(onCard)
        tvNoDataMessage.setTextColor(onCard)

        applyMetricCardPremiumStyle(palette)
        calendarAdapter.setPremiumPalette(palette)
    }

    /**
     * 칼로리/거리 강조 카드는 원래 옅은 파스텔 배경 + 어두운 텍스트라, NIGHT의 어두운 카드 위에서는
     * 그대로 두면 밝은 상자가 튀어 보인다. NIGHT에서는 반투명 색조 배경 + 밝은 텍스트로 뒤집는다.
     */
    private fun applyMetricCardPremiumStyle(palette: PremiumFlatColors?) = with(binding) {
        val ctx = requireContext()
        if (palette != null && premiumMode == PremiumVisualMode.NIGHT) {
            val primary = ContextCompat.getColor(ctx, DesignR.color.walklog_primary)
            val secondary = ContextCompat.getColor(ctx, DesignR.color.walklog_secondary)
            cardMetricCalories.setPremiumCardColor(withAlpha(primary, 46))
            cardMetricDistance.setPremiumCardColor(withAlpha(secondary, 70))
            ivMetricCaloriesIcon.setColorFilter(palette.onCard)
            tvMetricCaloriesLabel.setTextColor(palette.onCard)
            ivMetricDistanceIcon.setColorFilter(palette.onCard)
            tvMetricDistanceLabel.setTextColor(palette.onCard)
        } else {
            val warmColor = ContextCompat.getColor(ctx, DesignR.color.walklog_primary_dark)
            val coolColor = ContextCompat.getColor(ctx, DesignR.color.walklog_secondary)
            cardMetricCalories.setPremiumCardColor(ContextCompat.getColor(ctx, DesignR.color.walklog_primary_container))
            cardMetricDistance.setPremiumCardColor(ContextCompat.getColor(ctx, DesignR.color.walklog_secondary_container))
            ivMetricCaloriesIcon.setColorFilter(warmColor)
            tvMetricCaloriesLabel.setTextColor(warmColor)
            ivMetricDistanceIcon.setColorFilter(coolColor)
            tvMetricDistanceLabel.setTextColor(coolColor)
        }
        val valueColor = palette?.onCard ?: ContextCompat.getColor(ctx, DesignR.color.walklog_text_primary)
        tvSelectedCalories.setTextColor(valueColor)
        tvSelectedDistance.setTextColor(valueColor)
    }

    private fun applyChipStyle(summary: SelectedDaySummary) = with(binding) {
        val ctx = requireContext()
        val palette = premiumPalette
        val isNight = premiumMode == PremiumVisualMode.NIGHT

        val (bgColor, textColor, text) = when {
            summary.isAchieved -> Triple(
                if (isNight) {
                    withAlpha(ContextCompat.getColor(ctx, DesignR.color.walklog_success), 56)
                } else {
                    ContextCompat.getColor(ctx, DesignR.color.walklog_success_container)
                },
                if (isNight) {
                    palette?.onCard ?: ContextCompat.getColor(ctx, DesignR.color.walklog_success_dark)
                } else {
                    ContextCompat.getColor(ctx, DesignR.color.walklog_success_dark)
                },
                getString(R.string.chip_achieved),
            )
            summary.hasData -> Triple(
                if (isNight) {
                    withAlpha(ContextCompat.getColor(ctx, DesignR.color.walklog_primary), 56)
                } else {
                    ContextCompat.getColor(ctx, DesignR.color.walklog_primary_container)
                },
                if (isNight) {
                    palette?.onCard ?: ContextCompat.getColor(ctx, DesignR.color.walklog_primary_dark)
                } else {
                    ContextCompat.getColor(ctx, DesignR.color.walklog_primary_dark)
                },
                summary.targetStatusText(),
            )
            else -> Triple(
                palette?.cardOutline ?: ContextCompat.getColor(ctx, DesignR.color.walklog_gray_100),
                palette?.onCardMuted ?: ContextCompat.getColor(ctx, DesignR.color.walklog_gray_400),
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
            else -> (premiumPalette?.onCardMuted ?: ContextCompat.getColor(ctx, DesignR.color.walklog_gray_400)) to ""
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
        pbGoalProgress.progressBackgroundTintList = ColorStateList.valueOf(progressTrackColor())
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
        val trackTint = ColorStateList.valueOf(progressTrackColor())

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

    /** 프로그레스 트랙(미달성 구간) 색 — NIGHT의 어두운 카드 위에서는 옅은 흰색 반투명으로 보여야 한다. */
    private fun progressTrackColor(): Int {
        val palette = premiumPalette
        return if (premiumMode == PremiumVisualMode.NIGHT && palette != null) {
            withAlpha(palette.onCard, 40)
        } else {
            ContextCompat.getColor(requireContext(), DesignR.color.walklog_gray_100)
        }
    }

    private fun withAlpha(color: Int, alpha: Int): Int = (color and 0x00FFFFFF) or (alpha shl 24)

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
