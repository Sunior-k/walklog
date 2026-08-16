package com.river.walklog.feature.reward

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetRewardRedemptionsUseCase
import com.river.walklog.core.model.RewardCatalogIds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BadgeCollectionViewModel @Inject constructor(
    private val getRewardRedemptionsUseCase: GetRewardRedemptionsUseCase,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(BadgeCollectionState())
    val state: StateFlow<BadgeCollectionState> = _state.asStateFlow()

    init {
        crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.BADGE_COLLECTION)
        observeOwnedBadges()
    }

    private fun observeOwnedBadges() {
        viewModelScope.launch {
            getRewardRedemptionsUseCase(RewardCatalogIds.BADGE_GOLD)
                .catch { e ->
                    crashReporter.log("badgeCollectionViewModel: $e")
                    crashReporter.recordException(e)
                }
                .collect { redemptions ->
                    _state.update { it.copy(isGoldBadgeOwned = redemptions.isNotEmpty()) }
                }
        }
    }
}
