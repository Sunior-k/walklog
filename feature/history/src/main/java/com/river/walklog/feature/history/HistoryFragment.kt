package com.river.walklog.feature.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

/**
 * 월간 걸음 캘린더 화면
 *
 */
@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

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
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarTop = insets
                .getInsets(WindowInsetsCompat.Type.statusBars())
                .top
            view.updatePadding(top = initialTopPadding + statusBarTop)
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
        selectedDayContainer.isVisible = showContent && state.selectedDaySummary != null
        state.selectedDaySummary?.let { summary ->
            tvSelectedDate.text = summary.dateText
            tvSelectedSteps.text = summary.stepsText
            tvSelectedCalories.text = summary.caloriesText
            tvSelectedDistance.text = summary.distanceText
            tvSelectedTargetStatus.text = summary.targetStatusText
            tvSelectedComparison.text = summary.comparisonText
            selectedMetricsDivider.isVisible = summary.hasData
            selectedMetricsRow.isVisible = summary.hasData
            tvSelectedComparison.isVisible = summary.hasData
        }

        progressBar.isVisible = isLoading
        rvCalendar.isVisible = showContent
        emptyState.isVisible = !isLoading && isEmpty
        statsContainer.isVisible = showContent

        if (!isLoading) {
            calendarAdapter.submitList(state.items)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
