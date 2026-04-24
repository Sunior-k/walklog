package com.river.walklog.feature.recap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RecapRoute(
    onBack: () -> Unit,
    viewModel: RecapViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RecapScreen(
        state = state,
        onClose = {
            viewModel.handleIntent(RecapIntent.OnClose)
            onBack()
        },
    )
}
