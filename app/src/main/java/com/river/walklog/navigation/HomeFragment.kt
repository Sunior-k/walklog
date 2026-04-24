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
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.feature.home.HomeRoute
import dagger.hilt.android.AndroidEntryPoint

/**
 * [HomeRoute]를 Fragment로 감싼 래퍼.
 *
 * - ComposeView + ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed 패턴:
 *   Fragment의 view lifecycle에 맞춰 Compose 컴포지션이 올바르게 해제.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireActivity()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            WalkLogTheme {
                HomeRoute(
                    onNavigateToWeeklyReport = {
                        findNavController().navigate(R.id.action_home_to_weeklyReport)
                    },
                    onNavigateToMission = {
                        findNavController().navigate(R.id.action_home_to_missionDetail)
                    },
                    onNavigateToRecap = {
                        findNavController().navigate(R.id.action_home_to_recap)
                    },
                )
            }
        }
    }
}
