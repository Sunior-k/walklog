package com.river.walklog.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.river.walklog.reactbridge.RewardStoreViewHost
import dagger.hilt.android.AndroidEntryPoint

/** RN 루트 View는 [RewardStoreViewHost]가 앱 생애주기 동안 하나만 만들어 재사용한다. */
@AndroidEntryPoint
class RewardStoreFragment : Fragment() {

    private lateinit var container: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FrameLayout(requireContext()).also { this.container = it }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        RewardStoreViewHost.attachTo(requireActivity(), container)
    }

    override fun onDestroyView() {
        RewardStoreViewHost.detachFrom(container)
        super.onDestroyView()
    }
}
