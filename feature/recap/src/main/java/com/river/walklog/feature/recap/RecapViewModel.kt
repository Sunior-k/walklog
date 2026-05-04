package com.river.walklog.feature.recap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.river.walklog.core.analytics.CrashKeys
import com.river.walklog.core.analytics.CrashReporter
import com.river.walklog.core.domain.usecase.GetMonthlyRecapUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class RecapViewModel @Inject constructor(
    private val getMonthlyRecap: GetMonthlyRecapUseCase,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(RecapState())
    val state: StateFlow<RecapState> = _state.asStateFlow()

    init {
        crashReporter.setKey(CrashKeys.SCREEN, CrashKeys.Screens.RECAP)
        val recapMonth = YearMonth.now().minusMonths(1)
        loadRecap(recapMonth.year, recapMonth.monthValue)
    }

    fun loadRecap(year: Int, month: Int) {
        getMonthlyRecap(year, month)
            .onEach { recap -> _state.update { it.copy(isLoading = false, recap = recap) } }
            .catch { e ->
                crashReporter.recordException(e)
                _state.update { it.copy(isLoading = false, isError = true) }
            }
            .launchIn(viewModelScope)
    }

    fun handleIntent(intent: RecapIntent) {
        when (intent) {
            RecapIntent.OnClose -> Unit
        }
    }
}
