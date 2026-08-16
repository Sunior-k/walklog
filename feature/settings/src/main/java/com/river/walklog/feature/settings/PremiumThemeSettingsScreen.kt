package com.river.walklog.feature.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.river.walklog.core.designsystem.component.PremiumBackgroundEffect
import com.river.walklog.core.designsystem.component.premiumPressScale
import com.river.walklog.core.designsystem.component.preview.BasePreview
import com.river.walklog.core.designsystem.component.preview.walklogPreview
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.core.model.PremiumVisualMode
import com.river.walklog.core.designsystem.R as DesignR

@Composable
fun PremiumThemeSettingsRoute(
    onBack: () -> Unit,
    viewModel: PremiumThemeSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PremiumThemeSettingsScreen(
        state = state,
        onBack = onBack,
        onSelectMode = viewModel::selectMode,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PremiumThemeSettingsScreen(
    state: PremiumThemeSettingsState,
    onBack: () -> Unit,
    onSelectMode: (PremiumVisualMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.premium_theme_settings_title),
                        style = WalkLogTheme.typography.typography5SB,
                        color = WalkLogTheme.colors.onSurface,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = ImageVector.vectorResource(DesignR.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.premium_theme_settings_back_cd),
                            tint = WalkLogTheme.colors.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WalkLogTheme.colors.background),
            )
        },
        containerColor = WalkLogTheme.colors.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.premium_theme_settings_description),
                    style = WalkLogTheme.typography.subTypography12R,
                    color = WalkLogTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            items(PremiumThemeModeOptions, key = { it.mode }) { option ->
                PremiumThemeModeCard(
                    option = option,
                    isSelected = state.selectedMode == option.mode,
                    onClick = { onSelectMode(option.mode) },
                )
            }
        }
    }
}

private data class PremiumThemeModeOption(
    val mode: PremiumVisualMode,
    val titleRes: Int,
    val descriptionRes: Int,
)

private val PremiumThemeModeOptions = listOf(
    PremiumThemeModeOption(
        mode = PremiumVisualMode.NIGHT,
        titleRes = R.string.premium_theme_mode_night_title,
        descriptionRes = R.string.premium_theme_mode_night_description,
    ),
    PremiumThemeModeOption(
        mode = PremiumVisualMode.DAY_CLEAR,
        titleRes = R.string.premium_theme_mode_day_clear_title,
        descriptionRes = R.string.premium_theme_mode_day_clear_description,
    ),
    PremiumThemeModeOption(
        mode = PremiumVisualMode.DAY_WET,
        titleRes = R.string.premium_theme_mode_day_wet_title,
        descriptionRes = R.string.premium_theme_mode_day_wet_description,
    ),
)

@Composable
private fun PremiumThemeModeCard(
    option: PremiumThemeModeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .premiumPressScale(enabled = true, interactionSource = interactionSource)
            .clip(RoundedCornerShape(24.dp))
            .background(option.mode.previewBackgroundColor())
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, WalkLogTheme.colors.primary, RoundedCornerShape(24.dp))
                } else {
                    Modifier
                },
            )
            .selectable(
                selected = isSelected,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                role = Role.RadioButton,
            ),
    ) {
        PremiumBackgroundEffect(mode = option.mode, modifier = Modifier.matchParentSize())

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, WalkLogColor.StaticBlack.copy(alpha = 0.55f)),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(option.titleRes),
                style = WalkLogTheme.typography.typography6SB,
                color = WalkLogColor.StaticWhite,
            )
            Text(
                text = stringResource(option.descriptionRes),
                style = WalkLogTheme.typography.subTypography13R,
                color = WalkLogColor.StaticWhite.copy(alpha = 0.85f),
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(WalkLogTheme.colors.primary),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(14.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.08f, size.height * 0.55f)
                        lineTo(size.width * 0.4f, size.height * 0.85f)
                        lineTo(size.width * 0.95f, size.height * 0.15f)
                    }
                    drawPath(
                        path = path,
                        color = WalkLogColor.StaticBlack,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }
            }
        }
    }
}

/** 카드가 현재 활성화된 앱 테마와 무관하게 각 모드 고유의 배경색으로 미리보기되도록 한다. */
private fun PremiumVisualMode.previewBackgroundColor(): Color = when (this) {
    PremiumVisualMode.NIGHT -> WalkLogColor.PremiumBackground
    PremiumVisualMode.DAY_CLEAR -> WalkLogColor.PremiumDayClearBackground
    PremiumVisualMode.DAY_WET -> WalkLogColor.PremiumDayWetBackground
}

@walklogPreview
@Composable
private fun PremiumThemeSettingsScreenPreview() {
    BasePreview {
        PremiumThemeSettingsScreen(state = PremiumThemeSettingsState(), onBack = {}, onSelectMode = {})
    }
}
