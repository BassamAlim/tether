package bassamalim.tether.features.connect

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.tether.core.ui.components.Avatar
import bassamalim.tether.core.ui.components.RelationshipField
import bassamalim.tether.core.ui.components.SearchField
import bassamalim.tether.core.ui.components.SectionLabel
import bassamalim.tether.core.ui.components.SelectionTick
import bassamalim.tether.core.ui.components.TagChip
import bassamalim.tether.core.ui.theme.Accent
import bassamalim.tether.core.ui.theme.Ink
import bassamalim.tether.core.ui.theme.InkFaint
import bassamalim.tether.core.ui.theme.InkMuted
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface0
import bassamalim.tether.core.ui.theme.Surface100
import bassamalim.tether.core.ui.theme.TetherType

@Composable
fun ConnectScreen(viewModel: ConnectViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ConnectScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onToggle = viewModel::onToggle,
        onNext = viewModel::onNext,
        onBack = viewModel::onBack,
        onSharedLabelChange = viewModel::onSharedLabelChange,
        onSuggestionClick = viewModel::onSuggestionClick,
        onPickClick = viewModel::onPickClick,
        onPickLabelChange = viewModel::onPickLabelChange,
        onPickSuggestionClick = viewModel::onPickSuggestionClick,
        onPickEditDismiss = viewModel::onPickEditDismiss,
        onPickLabelSave = viewModel::onPickLabelSave,
        onPickUseSharedLabel = viewModel::onPickUseSharedLabel,
        onSave = viewModel::onSave
    )
}

/**
 * Two steps on one screen: tick everyone they know, then say how. The label is written once for
 * the whole batch, because that's how these come to mind — "these six are from university" —
 * and any one of them can be given its own line instead. Labels stay optional throughout: a
 * bare link is still worth having, and the words can come later.
 */
@Composable
private fun ConnectScreen(
    state: ConnectUiState,
    onQueryChange: (String) -> Unit,
    onToggle: (Long) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSharedLabelChange: (String) -> Unit,
    onSuggestionClick: (String) -> Unit,
    onPickClick: (ConnectPick) -> Unit,
    onPickLabelChange: (String) -> Unit,
    onPickSuggestionClick: (String) -> Unit,
    onPickEditDismiss: () -> Unit,
    onPickLabelSave: () -> Unit,
    onPickUseSharedLabel: () -> Unit,
    onSave: () -> Unit
) {
    // The system gesture steps back through the screen the same way the button does, so a long
    // selection isn't lost by swiping.
    BackHandler(enabled = state.isLabelling, onBack = onBack)

    Column(
        Modifier
            .fillMaxSize()
            .background(Surface0)
    ) {
        TopBar(
            backLabel = if (state.isLabelling) "Back" else "Cancel",
            forwardLabel = if (state.isLabelling) "Save" else "Next",
            canGoForward = state.hasSelection,
            onBack = onBack,
            onForward = if (state.isLabelling) onSave else onNext
        )

        if (state.isLabelling) {
            LabelStep(
                state = state,
                onSharedLabelChange = onSharedLabelChange,
                onSuggestionClick = onSuggestionClick,
                onPickClick = onPickClick
            )
        } else {
            PickStep(state = state, onQueryChange = onQueryChange, onToggle = onToggle)
        }
    }

    state.editing?.let { editing ->
        PickLabelDialog(
            editing = editing,
            options = state.relationshipOptions,
            onLabelChange = onPickLabelChange,
            onSuggestionClick = onPickSuggestionClick,
            onDismiss = onPickEditDismiss,
            onSave = onPickLabelSave,
            onUseShared = onPickUseSharedLabel
        )
    }
}

@Composable
private fun TopBar(
    backLabel: String,
    forwardLabel: String,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.screen, start = Spacing.sm, end = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = backLabel,
            style = MaterialTheme.typography.labelMedium,
            color = InkMuted,
            modifier = Modifier
                .clickable(onClick = onBack)
                .padding(horizontal = Spacing.md, vertical = Spacing.md)
        )

        Text(
            text = "Connect",
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = forwardLabel,
            style = MaterialTheme.typography.labelLarge,
            // Dimmed rather than hidden, as on New person: you can see what's left to do.
            color = if (canGoForward) Accent else InkFaint,
            modifier = Modifier
                .clickable(enabled = canGoForward, onClick = onForward)
                .padding(horizontal = Spacing.md, vertical = Spacing.md)
        )
    }
}

@Composable
private fun PickStep(
    state: ConnectUiState,
    onQueryChange: (String) -> Unit,
    onToggle: (Long) -> Unit
) {
    SearchField(
        value = state.query,
        onValueChange = onQueryChange,
        placeholder = "Search your people",
        modifier = Modifier.padding(top = Spacing.md, start = Spacing.screen, end = Spacing.screen)
    )

    Text(
        text = when {
            state.hasSelection -> "${state.selectedCount} selected."
            state.personName.isEmpty() -> "Tick everyone already in Tether that they know."
            else -> "Who does ${state.personName} know? Tick everyone that applies — each link " +
                    "shows on both their pages."
        },
        style = MaterialTheme.typography.bodySmall,
        color = InkMuted,
        modifier = Modifier.padding(top = 14.dp, start = Spacing.screen, end = Spacing.screen)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = Spacing.md, bottom = Spacing.lg)
    ) {
        items(state.candidates, key = { it.id }) { candidate ->
            CandidateRow(candidate = candidate, onClick = { onToggle(candidate.id) })
        }

        if (!state.isLoading && state.candidates.isEmpty()) {
            item {
                Text(
                    text =
                        if (state.hasNobodyToConnect)
                            "There's nobody left to connect them to. Add another person first."
                        else "Nobody matches that.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkFaint,
                    modifier = Modifier.padding(Spacing.xxl)
                )
            }
        }
    }
}

@Composable
private fun CandidateRow(candidate: ConnectCandidate, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = Spacing.md, vertical = 1.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(color = if (candidate.isSelected) Surface100 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.sm, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Avatar(initials = candidate.initials)

        Text(
            text = candidate.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )

        candidate.tagLabel?.let { TagChip(label = it) }

        SelectionTick(isSelected = candidate.isSelected)
    }
}

@Composable
private fun LabelStep(
    state: ConnectUiState,
    onSharedLabelChange: (String) -> Unit,
    onSuggestionClick: (String) -> Unit,
    onPickClick: (ConnectPick) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = Spacing.xxl)
    ) {
        Text(
            text = summary(state),
            style = MaterialTheme.typography.bodySmall,
            color = InkMuted,
            modifier = Modifier.padding(
                top = Spacing.md,
                start = Spacing.screen,
                end = Spacing.screen
            )
        )

        RelationshipField(
            label = "How they know each other",
            value = state.sharedLabel,
            options = state.relationshipOptions,
            onValueChange = onSharedLabelChange,
            onOptionClick = onSuggestionClick,
            modifier = Modifier.padding(
                top = Spacing.lg,
                start = Spacing.screen,
                end = Spacing.screen
            )
        )

        SectionLabel(
            text = "Each person",
            modifier = Modifier.padding(
                start = Spacing.screen,
                end = Spacing.screen,
                top = Spacing.xl,
                bottom = Spacing.xs
            )
        )

        Text(
            text = "Everyone follows the line above. Tap someone to write their own instead.",
            style = TetherType.Caption,
            color = InkFaint,
            modifier = Modifier.padding(
                start = Spacing.screen,
                end = Spacing.screen,
                bottom = Spacing.sm
            )
        )

        state.picks.forEach { pick ->
            PickRow(pick = pick, onClick = { onPickClick(pick) })
        }
    }
}

/** "Connecting 4 people to Ahmed." */
private fun summary(state: ConnectUiState): String {
    val count = if (state.selectedCount == 1) "1 person" else "${state.selectedCount} people"

    return if (state.personName.isEmpty()) "Connecting $count."
    else "Connecting $count to ${state.personName}."
}

@Composable
private fun PickRow(pick: ConnectPick, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = Spacing.md, vertical = 1.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.sm, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Avatar(initials = pick.initials)

        Column(Modifier.weight(1f)) {
            Text(text = pick.name, style = MaterialTheme.typography.titleMedium)

            Text(
                text = pick.subtitle,
                style = MaterialTheme.typography.bodySmall,
                // A line written for this person reads as theirs; an inherited one stays quiet,
                // so a glance down the list shows which ones you've singled out.
                color = if (pick.hasOwnLabel) Ink else InkFaint,
                fontWeight = if (pick.hasOwnLabel) FontWeight(600) else null,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

@Composable
private fun PickLabelDialog(
    editing: ConnectPickEdit,
    options: List<String>,
    onLabelChange: (String) -> Unit,
    onSuggestionClick: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onUseShared: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface100,
        title = { Text(text = editing.name, style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                RelationshipField(
                    label = "How they know each other",
                    value = editing.label,
                    options = options,
                    onValueChange = onLabelChange,
                    onOptionClick = onSuggestionClick
                )

                Text(
                    text = "This line covers only this one link.",
                    style = TetherType.Caption,
                    color = InkFaint,
                    modifier = Modifier.padding(top = Spacing.md)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(text = "Save", style = MaterialTheme.typography.labelLarge, color = Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onUseShared) {
                Text(
                    text = "Use shared",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkMuted
                )
            }
        }
    )
}
