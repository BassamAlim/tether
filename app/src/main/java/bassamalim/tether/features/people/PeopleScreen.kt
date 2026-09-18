package bassamalim.tether.features.people

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.tether.core.ui.components.Avatar
import bassamalim.tether.core.ui.components.FilterPill
import bassamalim.tether.core.ui.components.SearchFieldButton
import bassamalim.tether.core.ui.components.SectionLabel
import bassamalim.tether.core.ui.components.TagChip
import bassamalim.tether.core.ui.theme.Attention
import bassamalim.tether.core.ui.theme.InkFaint
import bassamalim.tether.core.ui.theme.InkMuted
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface100
import bassamalim.tether.core.ui.theme.TetherType

@Composable
fun PeopleScreen(viewModel: PeopleViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PeopleScreen(
        state = state,
        onSearchClick = viewModel::onSearchClick,
        onFilterSelect = viewModel::onFilterSelect,
        onPersonClick = viewModel::onPersonClick
    )
}

@Composable
private fun PeopleScreen(
    state: PeopleUiState,
    onSearchClick: () -> Unit,
    onFilterSelect: (PeopleFilter) -> Unit,
    onPersonClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.xxl)
    ) {
        item {
            Column(Modifier.padding(horizontal = Spacing.screen)) {
                Text(text = "People", style = MaterialTheme.typography.headlineMedium)

                Text(
                    text = subtitle(state),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                    modifier = Modifier.padding(top = Spacing.xs)
                )

                SearchFieldButton(
                    placeholder = "Search people",
                    onClick = onSearchClick,
                    modifier = Modifier.padding(top = Spacing.lg)
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screen, vertical = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                PeopleFilter.entries.forEach { filter ->
                    FilterPill(
                        label = filter.label,
                        selected = filter == state.filter,
                        onClick = { onFilterSelect(filter) }
                    )
                }
            }
        }

        if (state.slipping.isNotEmpty()) {
            item {
                SectionLabel(
                    text = "Slipping",
                    modifier = Modifier.padding(
                        start = Spacing.screen,
                        end = Spacing.screen,
                        top = Spacing.sm,
                        bottom = Spacing.sm
                    )
                )
            }

            items(state.slipping, key = { it.id }) { person ->
                PersonRow(person = person, onClick = { onPersonClick(person.id) })
            }
        }

        if (state.inTouch.isNotEmpty()) {
            item {
                SectionLabel(
                    text = "In touch",
                    modifier = Modifier.padding(
                        start = Spacing.screen,
                        end = Spacing.screen,
                        top = Spacing.screen,
                        bottom = Spacing.sm
                    )
                )
            }

            items(state.inTouch, key = { it.id }) { person ->
                PersonRow(person = person, onClick = { onPersonClick(person.id) })
            }
        }

        if (state.isEmpty) {
            item {
                Text(
                    text = "Nobody here yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkFaint,
                    modifier = Modifier.padding(Spacing.xxl)
                )
            }
        }
    }
}

@Composable
private fun PersonRow(person: PersonListItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = Spacing.md, vertical = Spacing.xxs)
            .fillMaxWidth()
            .background(
                color = if (person.isSlipping) Surface100 else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.sm, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Avatar(initials = person.initials, highlighted = person.isSlipping)

        Column(Modifier.weight(1f)) {
            Text(text = person.name, style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.padding(top = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                person.tag?.let { TagChip(label = it.chipLabel) }

                Text(
                    text = person.cadenceLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint
                )
            }
        }

        Text(
            text = person.lastContactLabel,
            style = TetherType.Timestamp,
            color = if (person.isSlipping) Attention else InkFaint
        )
    }
}

private fun subtitle(state: PeopleUiState): String {
    val people = if (state.totalCount == 1) "1 person" else "${state.totalCount} people"
    return "$people · ${state.slippingCount} slipping"
}
