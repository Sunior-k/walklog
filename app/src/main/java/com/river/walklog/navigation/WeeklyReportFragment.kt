package com.river.walklog.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.river.walklog.R
import com.river.walklog.feature.report.WeeklyReportArchiveRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeeklyReportFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireActivity()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            WalkLogAppTheme {
                WeeklyReportArchiveRoute(
                    onBack = { findNavController().popBackStack() },
                    onClickReport = { weekStartEpochDay ->
                        findNavController().navigate(
                            R.id.action_weeklyReport_to_weeklyReportDetail,
                            WeeklyReportDetailFragment.createArgs(weekStartEpochDay),
                        )
                    },
                )
            }
        }
    }
}
