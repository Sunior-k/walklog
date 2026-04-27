package com.river.walklog.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.feature.report.WeeklyReportDetailRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeeklyReportDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireActivity()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            WalkLogTheme {
                WeeklyReportDetailRoute(
                    weekStartEpochDay = requireArguments().getLong(ARG_WEEK_START_EPOCH_DAY),
                    onBack = { findNavController().popBackStack() },
                )
            }
        }
    }

    companion object {
        private const val ARG_WEEK_START_EPOCH_DAY = "weekStartEpochDay"

        fun createArgs(weekStartEpochDay: Long): Bundle = Bundle().apply {
            putLong(ARG_WEEK_START_EPOCH_DAY, weekStartEpochDay)
        }
    }
}
