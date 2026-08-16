package com.river.walklog.feature.reward

import androidx.lifecycle.ViewModel
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class RewardViewModel @Inject constructor(
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(RewardState())
    val state: StateFlow<RewardState> = _state.asStateFlow()

    init {
        crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.REWARD)
    }

    fun onStoreCardClicked() {
        _state.update { it.copy(navigationDestination = RewardDest.Store) }
    }

    fun onPointsHistoryCardClicked() {
        _state.update { it.copy(navigationDestination = RewardDest.PointsHistory) }
    }

    fun onBadgeCollectionCardClicked() {
        _state.update { it.copy(navigationDestination = RewardDest.BadgeCollection) }
    }

    fun clearNavigationDestination() {
        _state.update { it.copy(navigationDestination = null) }
    }
}
