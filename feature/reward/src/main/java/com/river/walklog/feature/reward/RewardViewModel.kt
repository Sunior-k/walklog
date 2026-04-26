package com.river.walklog.feature.reward

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RewardViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(RewardState())
    val state: StateFlow<RewardState> = _state.asStateFlow()
}
