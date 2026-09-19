package bassamalim.tether.features.person

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.tether.core.ui.components.SectionLabel
import bassamalim.tether.core.ui.theme.Accent
import bassamalim.tether.core.ui.theme.AccentInk
import bassamalim.tether.core.ui.theme.Action
import bassamalim.tether.core.ui.theme.Attention
import bassamalim.tether.core.ui.theme.Danger
import bassamalim.tether.core.ui.theme.Ink
import bassamalim.tether.core.ui.theme.InkFaint
import bassamalim.tether.core.ui.theme.InkMuted
import bassamalim.tether.core.ui.theme.Pill
import bassamalim.tether.core.ui.theme.Sizes
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface0
import bassamalim.tether.core.ui.theme.Surface100
import bassamalim.tether.core.ui.theme.Surface200
import bassamalim.tether.core.ui.theme.Surface300
import bassamalim.tether.core.ui.theme.TetherType
import bassamalim.tether.R
import androidx.compose.ui.res.painterResource

@Composable
fun PersonScreen(viewModel: PersonViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    PersonScreen(
        state = state,
        onBack = viewModel::onBack,
        onLogCatchUp = viewModel::onLogCatchUp,
        onMessage = {
            state.phone?.let { phone ->
                context.startActivity(Intent(Intent.ACTION_SENDTO, "smsto:$phone".toUri()))
            }
        },
        onMenuOpen = viewModel::onMenuOpen,
        onMenuDismiss = viewModel::onMenuDismiss,
        onDeleteClick = viewModel::onDeleteClick,
        onDeleteDismiss = viewModel::onDeleteDismiss,
        onDeleteConfirm = viewModel::onDeleteConfirm
    )
}

@Composable
private fun PersonScreen(
    state: PersonUiState,
    onBack: () -> Unit,
    onLogCatchUp: () -> Unit,
    onMessage: () -> Unit,
    onMenuOpen: () -> Unit,
    onMenuDismiss: () -> Unit,
    onDeleteClick: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onDeleteConfirm: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface0),
        contentPadding = PaddingValues(bottom = Spacing.xxl)
    ) {
        item {
            TopBar(
                isMenuOpen = state.isMenuOpen,
                onBack = onBack,
                onMenuOpen = onMenuOpen,
                onMenuDismiss = onMenuDismiss,
                onDeleteClick = onDeleteClick
            )
        }

        item {
            Identity(state = state)
        }

        item {
            Actions(
                canMessage = state.canMessage,
                onLogCatchUp = onLogCatchUp,
                onMessage = onMessage
            )
        }

        if (state.details.isNotEmpty()) {
            item { Header("Details") }

            item { DetailsCard(details = state.details) }
        }

        item { Header("History") }

        if (state.history.isEmpty()) {
            item {
                Text(
                    text = "Nothing logged yet. The first catch-up you log starts the record.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint,
                    modifier = Modifier.padding(horizontal = Spacing.screen)
                )
            }
        }

        itemsIndexed(state.history, key = { _, entry -> entry.id }) { index, entry ->
            HistoryRow(entry = entry, isFirst = index == 0, isLast = index == state.history.lastIndex)
        }
    }

    if (state.isConfirmingDelete) {
        DeleteDialog(
            name = state.name,
            onDismiss = onDeleteDismiss,
            onConfirm = onDeleteConfirm
        )
    }
}

@Composable
private fun TopBar(
    isMenuOpen: Boolean,
    onBack: () -> Unit,
    onMenuOpen: () -> Unit,
    onMenuDismiss: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.screen, start = Spacing.md, end = Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CircleIconButton(onClick = onBack) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = "Back to people",
                tint = Ink,
                modifier = Modifier.size(22.dp)
            )
        }

        Box {
            CircleIconButton(onClick = onMenuOpen) {
                Icon(
                    painter = painterResource(R.drawable.ic_more),
                    contentDescription = "More options",
                    tint = Ink,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = isMenuOpen,
                onDismissRequest = onMenuDismiss,
                containerColor = Surface200
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Delete person",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Danger
                        )
                    },
                    onClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
private fun Identity(state: PersonUiState) {
    Column(Modifier.padding(top = Spacing.sm, start = Spacing.screen, end = Spacing.screen)) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(color = Surface200, shape = Pill),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.initials,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight(700),
                color = InkMuted
            )
        }

        Text(
            text = state.name,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = Spacing.lg)
        )

        Row(
            modifier = Modifier.padding(top = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            state.tagLabel?.let { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = InkMuted,
                    modifier = Modifier
                        .background(color = Surface200, shape = Pill)
                        .padding(horizontal = 9.dp, vertical = Spacing.xs)
                )
            }

            Text(
                text = state.cadenceLabel,
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted
            )
        }

        Text(
            text = state.status,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight(600),
            color = if (state.isOverdue) Attention else InkMuted,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
private fun Actions(canMessage: Boolean, onLogCatchUp: () -> Unit, onMessage: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(top = Spacing.screen, start = Spacing.screen, end = Spacing.screen)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .background(color = Action, shape = MaterialTheme.shapes.medium)
                .clickable(onClick = onLogCatchUp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.CenterHorizontally)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = AccentInk,
                modifier = Modifier.size(18.dp)
            )

            Text(
                text = "Log a catch-up",
                style = MaterialTheme.typography.labelLarge,
                color = AccentInk
            )
        }

        // TODO: per-person reminders aren't built yet — the weekly nudge is all there is.
        SquareIconButton(enabled = false, onClick = {}) {
            Icon(
                painter = painterResource(R.drawable.ic_bell),
                contentDescription = "Set a reminder",
                tint = InkFaint,
                modifier = Modifier.size(19.dp)
            )
        }

        SquareIconButton(enabled = canMessage, onClick = onMessage) {
            Icon(
                painter = painterResource(R.drawable.ic_message),
                contentDescription = "Send a message",
                tint = if (canMessage) Ink else InkFaint,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@Composable
private fun DetailsCard(details: List<DetailRow>) {
    Column(
        Modifier
            .padding(horizontal = Spacing.screen)
            .fillMaxWidth()
            .background(color = Surface100, shape = MaterialTheme.shapes.medium)
            .padding(horizontal = Spacing.lg, vertical = Spacing.xxs)
    ) {
        details.forEachIndexed { index, detail ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                Text(
                    text = detail.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint
                )

                Text(
                    text = detail.value,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight(600),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }

            if (index != details.lastIndex) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(Sizes.border)
                        .background(Surface300)
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry, isFirst: Boolean, isLast: Boolean) {
    Row(
        modifier = Modifier
            .padding(horizontal = Spacing.screen)
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Column(
            modifier = Modifier
                .width(Spacing.sm)
                .fillMaxHeight()
                .padding(top = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(Spacing.sm)
                    .background(color = if (isFirst) InkMuted else Surface300, shape = Pill)
            )

            if (!isLast) {
                Box(
                    Modifier
                        .width(Sizes.border)
                        .weight(1f)
                        .background(Surface300)
                )
            }
        }

        Column(
            Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else Spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight(700),
                    modifier = Modifier.weight(1f)
                )

                Text(text = entry.timeLabel, style = TetherType.Timestamp, color = InkFaint)
            }

            entry.note?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }
    }
}

@Composable
private fun DeleteDialog(name: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface100,
        title = { Text(text = "Delete $name?", style = MaterialTheme.typography.titleMedium) },
        text = {
            Text(
                // With no server, there is nowhere to restore this from.
                text = "Their details and every catch-up you logged go with them. " +
                        "This can't be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkMuted
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Delete", style = MaterialTheme.typography.labelLarge, color = Danger)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Keep", style = MaterialTheme.typography.labelLarge, color = InkMuted)
            }
        }
    )
}

@Composable
private fun Header(text: String) {
    SectionLabel(
        text = text,
        modifier = Modifier.padding(
            start = Spacing.screen,
            end = Spacing.screen,
            top = Spacing.xl,
            bottom = Spacing.sm
        )
    )
}

@Composable
private fun CircleIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(Sizes.avatar)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = { content() }
    )
}

@Composable
private fun SquareIconButton(
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(color = Surface200, shape = MaterialTheme.shapes.medium)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
        content = { content() }
    )
}
