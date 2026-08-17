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
import com.river.walklog.feature.reward.RewardRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RewardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireActivity()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            WalkLogAppTheme {
                RewardRoute(
                    onNavigateToStore = {
                        findNavController().navigate(R.id.action_reward_to_store)
                    },
                    onNavigateToPointsHistory = {
                        findNavController().navigate(R.id.action_reward_to_points_history)
                    },
                    onNavigateToBadgeCollection = {
                        findNavController().navigate(R.id.action_reward_to_badges)
                    },
                )
            }
        }
    }
}
