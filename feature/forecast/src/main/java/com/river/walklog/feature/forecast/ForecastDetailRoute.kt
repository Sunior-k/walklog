package com.river.walklog.feature.forecast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ForecastDetailRoute(
    onDismiss: () -> Unit,
    onClickStartWalking: () -> Unit,
    viewModel: ForecastDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ForecastDetailBottomSheet(
        state = state,
        onDismiss = {
            viewModel.handleIntent(ForecastDetailIntent.OnDismiss)
            onDismiss()
        },
        onClickStartWalking = {
            viewModel.handleIntent(ForecastDetailIntent.OnClickStartWalking)
            onClickStartWalking()
        },
    )
}
