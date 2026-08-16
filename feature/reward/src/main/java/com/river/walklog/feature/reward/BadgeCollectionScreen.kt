package com.river.walklog.feature.reward

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.river.walklog.core.designsystem.component.BadgeColor
import com.river.walklog.core.designsystem.component.BadgeSize
import com.river.walklog.core.designsystem.component.BadgeVariant
import com.river.walklog.core.designsystem.component.BaseBadge
import com.river.walklog.core.designsystem.component.WalkLogLinearProgressBar
import com.river.walklog.core.designsystem.component.preview.BasePreview
import com.river.walklog.core.designsystem.component.preview.walklogPreview
import com.river.walklog.core.designsystem.foundation.WalkLogColor
import com.river.walklog.core.designsystem.foundation.WalkLogTheme
import com.river.walklog.core.designsystem.R as DesignR

@Composable
fun BadgeCollectionRoute(
    onBack: () -> Unit,
    viewModel: BadgeCollectionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BadgeCollectionScreen(state = state, onBack = onBack)
}

/**
 * Xbox/PlayStation 트로피 케이스 + Duolingo 업적 배지 패턴을 참고한 "달성 갤러리" 구성.
 * 실제 뱃지는 아직 1종(골드 워커)뿐이지만, 상단에 보유 현황 진행률을 보여주고 나머지 칸을
 * "???" 미스터리 타일로 채워서 컬렉션이 계속 채워질 거라는 기대감을 준다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BadgeCollectionScreen(
    state: BadgeCollectionState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tiles = buildBadgeTiles(isGoldBadgeOwned = state.isGoldBadgeOwned)
    val ownedCount = tiles.count { it.isOwned }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.reward_feature2_title),
                        style = WalkLogTheme.typography.typography5SB,
                        color = WalkLogTheme.colors.onSurface,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = ImageVector.vectorResource(DesignR.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back_button_cd),
                            tint = WalkLogTheme.colors.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WalkLogTheme.colors.background),
            )
        },
        containerColor = WalkLogTheme.colors.background,
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            BadgeProgressHeader(ownedCount = ownedCount, totalCount = tiles.size)

            tiles.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    row.forEach { tile -> BadgeTile(spec = tile, modifier = Modifier.weight(1f)) }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private data class BadgeTileSpec(
    val emoji: String,
    val title: String,
    val statusText: String,
    val isOwned: Boolean,
    val isMystery: Boolean,
)

@Composable
private fun buildBadgeTiles(isGoldBadgeOwned: Boolean): List<BadgeTileSpec> = listOf(
    BadgeTileSpec(
        emoji = "🥇",
        title = stringResource(R.string.badge_collection_gold_badge_title),
        statusText = stringResource(
            if (isGoldBadgeOwned) R.string.badge_collection_owned else R.string.badge_collection_locked,
        ),
        isOwned = isGoldBadgeOwned,
        isMystery = false,
    ),
    BadgeTileSpec(
        emoji = "?",
        title = stringResource(R.string.badge_collection_mystery_title),
        statusText = stringResource(R.string.badge_collection_mystery_subtitle),
        isOwned = false,
        isMystery = true,
    ),
    BadgeTileSpec(
        emoji = "?",
        title = stringResource(R.string.badge_collection_mystery_title),
        statusText = stringResource(R.string.badge_collection_mystery_subtitle),
        isOwned = false,
        isMystery = true,
    ),
    BadgeTileSpec(
        emoji = "?",
        title = stringResource(R.string.badge_collection_mystery_title),
        statusText = stringResource(R.string.badge_collection_mystery_subtitle),
        isOwned = false,
        isMystery = true,
    ),
)

@Composable
private fun BadgeProgressHeader(ownedCount: Int, totalCount: Int, modifier: Modifier = Modifier) {
    val progress = if (totalCount == 0) 0f else ownedCount.toFloat() / totalCount

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(WalkLogTheme.colors.surfaceVariant)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "🏆", style = WalkLogTheme.typography.typography6SB)
            Text(
                text = stringResource(R.string.badge_collection_progress_format, ownedCount, totalCount),
                style = WalkLogTheme.typography.typography5SB,
                color = WalkLogTheme.colors.onSurface,
            )
        }
        Text(
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
            text = stringResource(R.string.badge_collection_progress_subtitle),
            style = WalkLogTheme.typography.subTypography12R,
            color = WalkLogTheme.colors.onSurfaceVariant,
        )
        WalkLogLinearProgressBar(
            progress = progress,
            modifier = Modifier.clip(RoundedCornerShape(50)),
            color = WalkLogColor.Primary,
            trackColor = WalkLogTheme.colors.background,
            height = 8.dp,
        )
    }
}

@Composable
private fun BadgeTile(spec: BadgeTileSpec, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(WalkLogTheme.colors.surfaceVariant)
            .padding(vertical = 20.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BadgeMedallion(spec = spec)
        Text(
            modifier = Modifier.padding(top = 12.dp),
            text = spec.title,
            style = WalkLogTheme.typography.subTypography11SB,
            color = if (spec.isMystery) WalkLogColor.TextDisabled else WalkLogTheme.colors.onSurface,
        )
        if (spec.isOwned) {
            Box(modifier = Modifier.padding(top = 8.dp)) {
                BaseBadge(
                    text = spec.statusText,
                    size = BadgeSize.Small,
                    variant = BadgeVariant.Fill,
                    color = BadgeColor.Yellow,
                )
            }
        } else {
            Text(
                modifier = Modifier.padding(top = 6.dp, start = 4.dp, end = 4.dp),
                text = spec.statusText,
                style = WalkLogTheme.typography.subTypography13R,
                color = WalkLogColor.TextDisabled,
            )
        }
    }
}

@Composable
private fun BadgeMedallion(spec: BadgeTileSpec, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(76.dp), contentAlignment = Alignment.Center) {
        if (spec.isOwned) {
            OwnedBadgeMedallion(spec = spec)
        } else {
            UnownedBadgeMedallion(spec = spec)
        }
    }
}

// spec.isOwned == true 인 경우에만 애니메이션(글로우/시머)을 구독한다. 미보유/미스터리/잠금
// 타일까지 애니메이션 스케줄러를 등록하면 매 프레임 불필요한 리컴포지션/리드로우가 발생한다.
@Composable
private fun BoxScope.OwnedBadgeMedallion(spec: BadgeTileSpec) {
    val infiniteTransition = rememberInfiniteTransition(label = "BadgeMedallion")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "MedallionGlow",
    )
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
        ),
        label = "MedallionShimmer",
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val glowRadius = size.minDimension / 2f * glowScale
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    WalkLogColor.Primary.copy(alpha = 0.35f * glowScale),
                    WalkLogColor.Primary.copy(alpha = 0.05f),
                    Color.Transparent,
                ),
                radius = glowRadius,
            ),
            radius = glowRadius,
        )
    }
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(WalkLogColor.PrimaryLight, WalkLogColor.Primary, WalkLogColor.PrimaryDark),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bandWidth = size.width * 0.4f
            val sweepX = size.width * shimmerProgress
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        WalkLogColor.StaticWhite.copy(alpha = 0.55f),
                        Color.Transparent,
                    ),
                    start = Offset(sweepX - bandWidth, 0f),
                    end = Offset(sweepX + bandWidth, size.height),
                ),
            )
        }
        Text(text = spec.emoji, style = WalkLogTheme.typography.typography3B)
    }
}

@Composable
private fun BoxScope.UnownedBadgeMedallion(spec: BadgeTileSpec) {
    when {
        spec.isMystery -> {
            Canvas(modifier = Modifier.size(60.dp)) {
                drawCircle(
                    color = WalkLogColor.Gray300,
                    radius = size.minDimension / 2f,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, pathEffect = dashPathEffect()),
                )
            }
            Text(
                text = spec.emoji,
                style = WalkLogTheme.typography.typography3B,
                color = WalkLogColor.TextDisabled,
            )
        }

        else -> {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(WalkLogTheme.colors.background),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = spec.emoji,
                    style = WalkLogTheme.typography.typography3B,
                    color = WalkLogColor.TextDisabled.copy(alpha = 0.5f),
                )
            }
            LockGlyph(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp),
            )
        }
    }
}

@Composable
private fun LockGlyph(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(WalkLogTheme.colors.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            val bodyTop = size.height * 0.42f
            drawRoundRect(
                color = WalkLogColor.TextDisabled,
                topLeft = Offset(0f, bodyTop),
                size = Size(size.width, size.height - bodyTop),
                cornerRadius = CornerRadius(2.dp.toPx()),
            )
            drawArc(
                color = WalkLogColor.TextDisabled,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(size.width * 0.18f, 0f),
                size = Size(size.width * 0.64f, bodyTop * 1.4f),
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

private fun dashPathEffect() = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))

@walklogPreview
@Composable
private fun BadgeCollectionScreenOwnedPreview() {
    BasePreview {
        BadgeCollectionScreen(state = BadgeCollectionState(isGoldBadgeOwned = true), onBack = {})
    }
}

@walklogPreview
@Composable
private fun BadgeCollectionScreenLockedPreview() {
    BasePreview {
        BadgeCollectionScreen(state = BadgeCollectionState(isGoldBadgeOwned = false), onBack = {})
    }
}
