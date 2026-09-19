package bassamalim.tether.features.circle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.tether.R
import bassamalim.tether.core.ui.theme.Ink
import bassamalim.tether.core.ui.theme.InkFaint
import bassamalim.tether.core.ui.theme.InkMuted
import bassamalim.tether.core.ui.theme.Pill
import bassamalim.tether.core.ui.theme.RelationshipHues
import bassamalim.tether.core.ui.theme.RelationshipOther
import bassamalim.tether.core.ui.theme.Sizes
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface0
import bassamalim.tether.core.ui.theme.Surface100
import bassamalim.tether.core.ui.theme.Surface200
import bassamalim.tether.core.ui.theme.Surface300
import bassamalim.tether.core.ui.theme.TetherType
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun CircleScreen(viewModel: CircleViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CircleScreen(
        state = state,
        onLegendClick = viewModel::onLegendClick,
        onNodeClick = viewModel::onNodeClick,
        onBackgroundClick = viewModel::onBackgroundClick,
        onSelectedClick = viewModel::onSelectedClick
    )
}

@Composable
private fun CircleScreen(
    state: CircleUiState,
    onLegendClick: (HueSlot) -> Unit,
    onNodeClick: (Long) -> Unit,
    onBackgroundClick: () -> Unit,
    onSelectedClick: (Long) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(top = Spacing.xxl, start = Spacing.screen, end = Spacing.screen)) {
            Text(text = "Circle", style = MaterialTheme.typography.headlineMedium)

            Text(
                text = state.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
                modifier = Modifier.padding(top = Spacing.xs)
            )
        }

        // The legend doubles as a filter: tapping a relationship dims everyone else, which is
        // what makes five hues readable for someone who can't tell two of them apart.
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.md),
            contentPadding = PaddingValues(horizontal = Spacing.screen),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(state.legend, key = { it.slot }) { group ->
                LegendPill(
                    group = group,
                    focused = group.slot == state.focus,
                    onClick = { onLegendClick(group.slot) }
                )
            }
        }

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (state.isEmpty) {
                Text(
                    text = "Nobody here yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkFaint,
                    modifier = Modifier.padding(Spacing.xxl)
                )
            } else if (!state.isLoading) {
                Orbits(state = state, onNodeClick = onNodeClick, onBackgroundClick = onBackgroundClick)

                val selected = state.selected
                if (selected != null) {
                    SelectedCard(
                        person = selected,
                        onClick = { onSelectedClick(selected.id) },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                } else {
                    Text(
                        text = "Closer means more recent. Past the dashed ring, you're overdue.",
                        style = TetherType.Caption,
                        color = InkFaint,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(Spacing.screen)
                    )
                }
            }
        }
    }
}

/**
 * The drawing: you in the middle, a spoke to everyone, and a line between any two people who know
 * each other. Pinch to zoom and drag to pan; it opens fitted to the space it's given.
 */
@Composable
private fun Orbits(
    state: CircleUiState,
    onNodeClick: (Long) -> Unit,
    onBackgroundClick: () -> Unit
) {
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        val density = LocalDensity.current
        val unit = with(density) { Sizes.orbitNode.toPx() }
        val margin = with(density) { Spacing.xxl.toPx() }
        val cx = constraints.maxWidth / 2f
        val cy = constraints.maxHeight / 2f

        // Everyone in view at first, never blown up past life size.
        val fit = ((min(cx, cy) - margin) / ((state.extent + 0.5f) * unit)).coerceIn(0.1f, 1f)
        var scale by remember(fit) { mutableFloatStateOf(fit) }
        var pan by remember(fit) { mutableStateOf(Offset.Zero) }

        val selectedId = state.selected?.id
        val slotById = remember(state.nodes) { state.nodes.associate { it.id to it.slot } }

        fun alphaOf(node: CircleNode) = when {
            state.focus != null && node.slot != state.focus -> 0.15f
            node.isUntracked -> 0.55f
            else -> 1f
        }

        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures { onBackgroundClick() } }
                .pointerInput(fit) {
                    detectTransformGestures { _, panBy, zoom, _ ->
                        scale = (scale * zoom).coerceIn(fit * 0.8f, max(fit, 1f) * 3f)
                        pan += panBy
                    }
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = pan.x
                    translationY = pan.y
                }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val hairline = Sizes.border.toPx()
                fun at(x: Float, y: Float) = Offset(cx + x * unit, cy + y * unit)

                drawCircle(
                    color = Surface300,
                    radius = state.dueRadius * unit,
                    center = at(0f, 0f),
                    style = Stroke(
                        width = hairline,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(Spacing.xs.toPx(), Spacing.xs.toPx())
                        )
                    )
                )

                state.nodes.forEach { node ->
                    val lit = node.id == selectedId
                    drawLine(
                        color = if (lit) InkMuted else Surface200,
                        start = at(0f, 0f),
                        end = at(node.x, node.y),
                        strokeWidth = hairline,
                        alpha = if (lit) 1f else alphaOf(node)
                    )
                }

                state.links.forEach { link ->
                    val lit = selectedId != null && (link.aId == selectedId || link.bId == selectedId)
                    val focused = state.focus == null ||
                        slotById[link.aId] == state.focus || slotById[link.bId] == state.focus
                    drawLine(
                        color = if (lit) Ink else InkFaint,
                        start = at(link.ax, link.ay),
                        end = at(link.bx, link.by),
                        strokeWidth = hairline,
                        alpha = when {
                            lit -> 1f
                            selectedId != null || !focused -> 0.12f
                            else -> 0.35f
                        }
                    )
                }

                state.nodes.firstOrNull { it.id == selectedId }?.let { node ->
                    drawCircle(
                        color = Ink,
                        radius = unit / 2 + Spacing.xs.toPx(),
                        center = at(node.x, node.y),
                        style = Stroke(width = Spacing.xxs.toPx())
                    )
                }
            }

            state.nodes.forEach { node ->
                val alpha = if (node.id == selectedId) 1f else alphaOf(node)

                PersonDot(
                    node = node,
                    onClick = { onNodeClick(node.id) },
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (cx + node.x * unit - unit / 2).roundToInt(),
                                (cy + node.y * unit - unit / 2).roundToInt()
                            )
                        }
                        .alpha(alpha)
                )
            }

            You(
                Modifier.offset {
                    val half = Sizes.orbitNode.toPx() / 2
                    IntOffset((cx - half).roundToInt(), (cy - half).roundToInt())
                }
            )
        }
    }
}

@Composable
private fun You(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(Sizes.orbitNode)
            .background(color = Ink, shape = Pill),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "You", style = MaterialTheme.typography.labelSmall, color = Surface0)
    }
}

/**
 * Filled in the relationship's hue, hollow when the relationship is unsaid, with the name written
 * inside: a long one wraps to two lines, then shrinks to fit before it ever ellipsizes.
 */
@Composable
private fun PersonDot(node: CircleNode, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val hue = hueOf(node.slot)

    Box(
        modifier = modifier
            .size(Sizes.orbitNode)
            .clip(Pill)
            .background(hue ?: Surface0)
            .then(if (hue == null) Modifier.border(Sizes.border, InkFaint, Pill) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            text = node.name,
            style = TetherType.Timestamp.copy(
                color = if (hue == null) InkMuted else Surface0,
                textAlign = TextAlign.Center
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 8.sp,
                maxFontSize = TetherType.Timestamp.fontSize
            ),
            modifier = Modifier.padding(horizontal = Spacing.xs)
        )
    }
}

@Composable
private fun LegendPill(group: HueGroup, focused: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(Sizes.field)
            .clip(Pill)
            .background(if (focused) Surface300 else Surface200)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        HueDot(group.slot)

        Text(
            text = "${group.label} · ${group.count}",
            style = MaterialTheme.typography.labelMedium,
            color = if (focused) Ink else InkMuted
        )
    }
}

@Composable
private fun HueDot(slot: HueSlot) {
    val hue = hueOf(slot)
    Box(
        Modifier
            .size(Spacing.sm)
            .clip(Pill)
            .background(hue ?: Surface0)
            .then(if (hue == null) Modifier.border(Sizes.border, InkFaint, Pill) else Modifier)
    )
}

@Composable
private fun SelectedCard(person: SelectedPerson, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(Spacing.md)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(Surface100)
            .clickable(onClick = onClick)
            .padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = person.name, style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.padding(top = Spacing.xxs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                HueDot(person.slot)
                Text(text = person.summary, style = MaterialTheme.typography.bodySmall, color = InkMuted)
            }

            Text(
                text = person.status,
                style = MaterialTheme.typography.bodySmall,
                color = InkFaint,
                modifier = Modifier.padding(top = Spacing.xxs)
            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = "Open",
            tint = InkFaint
        )
    }
}

private fun hueOf(slot: HueSlot): Color? = when (slot) {
    HueSlot.ONE -> RelationshipHues[0]
    HueSlot.TWO -> RelationshipHues[1]
    HueSlot.THREE -> RelationshipHues[2]
    HueSlot.FOUR -> RelationshipHues[3]
    HueSlot.FIVE -> RelationshipHues[4]
    HueSlot.OTHER -> RelationshipOther
    HueSlot.NONE -> null
}
