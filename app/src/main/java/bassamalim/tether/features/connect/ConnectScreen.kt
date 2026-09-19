package bassamalim.tether.features.connect

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.tether.core.ui.components.Avatar
import bassamalim.tether.core.ui.components.FilterPill
import bassamalim.tether.core.ui.components.LabeledTextField
import bassamalim.tether.core.ui.components.SearchField
import bassamalim.tether.core.ui.components.TagChip
import bassamalim.tether.core.ui.theme.Accent
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
        onSelect = viewModel::onSelect,
        onClearSelection = viewModel::onClearSelection,
        onLabelChange = viewModel::onLabelChange,
        onSuggestionClick = viewModel::onSuggestionClick,
        onCancel = viewModel::onCancel,
        onSave = viewModel::onSave
    )
}

/**
 * Two steps on one screen: pick the other person, then say how they know each other. The label
 * is optional — a bare link is still worth having, and you can add the words later.
 */
@Composable
private fun ConnectScreen(
    state: ConnectUiState,
    onQueryChange: (String) -> Unit,
    onSelect: (Long) -> Unit,
    onClearSelection: () -> Unit,
    onLabelChange: (String) -> Unit,
    onSuggestionClick: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Surface0)
    ) {
        TopBar(canSave = state.canSave, onCancel = onCancel, onSave = onSave)

        if (state.selected == null) {
            PickStep(state = state, onQueryChange = onQueryChange, onSelect = onSelect)
        } else {
            LabelStep(
                selected = state.selected,
                personName = state.personName,
                label = state.label,
                onClearSelection = onClearSelection,
                onLabelChange = onLabelChange,
                onSuggestionClick = onSuggestionClick
            )
        }
    }
}

@Composable
private fun TopBar(canSave: Boolean, onCancel: () -> Unit, onSave: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.screen, start = Spacing.sm, end = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Cancel",
            style = MaterialTheme.typography.labelMedium,
            color = InkMuted,
            modifier = Modifier
                .clickable(onClick = onCancel)
                .padding(horizontal = Spacing.md, vertical = Spacing.md)
        )

        Text(
            text = "Connect",
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "Save",
            style = MaterialTheme.typography.labelLarge,
            // Dimmed rather than hidden, as on New person: you can see what's left to do.
            color = if (canSave) Accent else InkFaint,
            modifier = Modifier
                .clickable(enabled = canSave, onClick = onSave)
                .padding(horizontal = Spacing.md, vertical = Spacing.md)
        )
    }
}

@Composable
private fun PickStep(
    state: ConnectUiState,
    onQueryChange: (String) -> Unit,
    onSelect: (Long) -> Unit
) {
    SearchField(
        value = state.query,
        onValueChange = onQueryChange,
        placeholder = "Search your people",
        modifier = Modifier.padding(top = Spacing.md, start = Spacing.screen, end = Spacing.screen)
    )

    Text(
        text =
            if (state.personName.isEmpty()) "Pick someone else already in Tether."
            else "Who does ${state.personName} know? Pick someone else already in Tether — " +
                    "the link shows on both their pages.",
        style = MaterialTheme.typography.bodySmall,
        color = InkMuted,
        modifier = Modifier.padding(top = 14.dp, start = Spacing.screen, end = Spacing.screen)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = Spacing.md, bottom = Spacing.lg)
    ) {
        items(state.candidates, key = { it.id }) { candidate ->
            CandidateRow(candidate = candidate, onClick = { onSelect(candidate.id) })
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
            .padding(horizontal = Spacing.md, vertical = Spacing.xxs)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.sm, vertical = Spacing.md),
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
    }
}

@Composable
private fun LabelStep(
    selected: ConnectCandidate,
    personName: String,
    label: String,
    onClearSelection: () -> Unit,
    onLabelChange: (String) -> Unit,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = Spacing.xxl)
    ) {
        Row(
            modifier = Modifier
                .padding(top = Spacing.lg, start = Spacing.screen, end = Spacing.screen)
                .fillMaxWidth()
                .background(color = Surface100, shape = MaterialTheme.shapes.medium)
                .padding(horizontal = Spacing.md, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Avatar(initials = selected.initials)

            Column(Modifier.weight(1f)) {
                Text(text = selected.name, style = MaterialTheme.typography.titleMedium)

                if (personName.isNotEmpty()) {
                    Text(
                        text = "with $personName",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkFaint,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }

            Text(
                text = "Change",
                style = MaterialTheme.typography.labelMedium,
                color = InkMuted,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClick = onClearSelection)
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
            )
        }

        LabeledTextField(
            label = "How they know each other",
            value = label,
            onValueChange = onLabelChange,
            placeholder = "Siblings, worked together, met at…",
            modifier = Modifier.padding(top = 22.dp, start = Spacing.screen, end = Spacing.screen)
        )

        FlowRow(
            modifier = Modifier
                .padding(top = 10.dp, start = Spacing.screen, end = Spacing.screen)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            CONNECTION_SUGGESTIONS.forEach { suggestion ->
                FilterPill(
                    label = suggestion,
                    selected = label == suggestion,
                    onClick = { onSuggestionClick(suggestion) }
                )
            }
        }

        Text(
            // One label, two pages: it has to make sense read from either end.
            text = "One line, shown on both their pages, so write it to read the same either way.",
            style = TetherType.Caption,
            color = InkFaint,
            modifier = Modifier.padding(top = 14.dp, start = Spacing.screen, end = Spacing.screen)
        )
    }
}
