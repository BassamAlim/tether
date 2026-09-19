package bassamalim.tether.features.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.tether.core.ui.components.Avatar
import bassamalim.tether.core.ui.components.SectionLabel
import bassamalim.tether.core.ui.theme.Accent
import bassamalim.tether.core.ui.theme.AccentWash
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
fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SearchScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onClear = viewModel::onClear,
        onBack = viewModel::onBack,
        onPersonClick = viewModel::onPersonClick
    )
}

@Composable
private fun SearchScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onPersonClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface0),
        contentPadding = PaddingValues(bottom = Spacing.xxl)
    ) {
        item {
            SearchBar(
                query = state.query,
                onQueryChange = onQueryChange,
                onClear = onClear,
                onBack = onBack
            )
        }

        if (state.people.isNotEmpty()) {
            item { Header("People · ${state.people.size}") }

            items(state.people, key = { it.id }) { person ->
                PersonRow(
                    person = person,
                    query = state.query,
                    onClick = { onPersonClick(person.id) }
                )
            }
        }

        if (state.notes.isNotEmpty()) {
            item { Header("In your notes · ${state.notes.size}") }

            items(state.notes, key = { it.id }) { note ->
                NoteRow(
                    note = note,
                    query = state.query,
                    onClick = { onPersonClick(note.personId) }
                )
            }
        }

        if (state.isEmpty) {
            item {
                Text(
                    text = "Nothing matches \"${state.query}\": not a name, a detail, or a note.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkFaint,
                    modifier = Modifier.padding(Spacing.xxl)
                )
            }
        }

        if (!state.hasQuery) {
            item {
                Text(
                    text = "Search names, details and everything you've written down.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkFaint,
                    modifier = Modifier.padding(Spacing.xxl)
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    // You came here to type.
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.screen, start = Spacing.md, end = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(Sizes.avatar)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = "Close search",
                tint = Ink,
                modifier = Modifier.size(22.dp)
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .background(color = Surface200, shape = MaterialTheme.shapes.medium)
                .padding(start = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search people and notes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkFaint
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.merge(TextStyle(color = Ink)),
                    cursorBrush = SolidColor(Accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }

            if (query.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(Pill)
                        .background(color = Surface300)
                        .clickable(onClick = onClear),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Clear search",
                        tint = InkMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonRow(person: PersonResult, query: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = Spacing.md)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.sm, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Avatar(initials = person.initials)

        Column(Modifier.weight(1f)) {
            Text(text = person.name, style = MaterialTheme.typography.titleMedium)

            Text(
                text = highlighted(person.subtitle, query),
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
                modifier = Modifier.padding(top = 3.dp)
            )
        }

        Text(text = person.lastContactLabel, style = TetherType.Timestamp, color = InkFaint)
    }
}

@Composable
private fun NoteRow(note: NoteResult, query: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = Spacing.md, vertical = Spacing.xxs)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(color = Surface100)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.sm, vertical = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Avatar(initials = note.initials)

        Column(Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = note.personName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                Text(text = note.meta, style = TetherType.Timestamp, color = InkFaint)
            }

            Text(
                text = highlighted(note.note, query),
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
    }
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

/** Lights up the part of the line that answered the search. */
private fun highlighted(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)

    val start = text.indexOf(query, ignoreCase = true)
    if (start < 0) return AnnotatedString(text)

    return buildAnnotatedString {
        append(text.substring(0, start))

        withStyle(SpanStyle(background = AccentWash, color = Accent)) {
            append(text.substring(start, start + query.length))
        }

        append(text.substring(start + query.length))
    }
}
