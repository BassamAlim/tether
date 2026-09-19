package bassamalim.tether.features.catchUp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.tether.core.ui.components.Avatar
import bassamalim.tether.core.ui.components.SectionLabel
import bassamalim.tether.core.ui.components.UndoSnackbar
import bassamalim.tether.core.ui.theme.Attention
import bassamalim.tether.core.ui.theme.InkFaint
import bassamalim.tether.core.ui.theme.InkMuted
import bassamalim.tether.core.ui.theme.Pill
import bassamalim.tether.core.ui.theme.Sizes
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface0
import bassamalim.tether.core.ui.theme.Surface100
import bassamalim.tether.core.ui.theme.Surface200
import bassamalim.tether.R
import androidx.compose.ui.res.painterResource

private const val UNDO = "UNDO"

@Composable
fun CatchUpScreen(viewModel: CatchUpViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CatchUpEvent.Logged -> {
                    // Tapping several checks in a row shouldn't queue stale bars behind the new one.
                    snackbarHostState.currentSnackbarData?.dismiss()

                    val result = snackbarHostState.showSnackbar(
                        message = "Logged a catch-up with ${event.personName}",
                        actionLabel = UNDO,
                        withDismissAction = false
                    )

                    if (result == SnackbarResult.ActionPerformed) viewModel.onUndo(event.interactionId)
                }
            }
        }
    }

    CatchUpScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onPersonClick = viewModel::onPersonClick,
        onReachedOut = viewModel::onReachedOut
    )
}

@Composable
private fun CatchUpScreen(
    state: CatchUpUiState,
    snackbarHostState: SnackbarHostState,
    onPersonClick: (Long) -> Unit,
    onReachedOut: (CatchUpItem) -> Unit
) {
    Scaffold(
        containerColor = Surface0,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data -> UndoSnackbar(data, actionLabel = UNDO) }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = Spacing.xxl)
        ) {
            item {
                Column(Modifier.padding(top = Spacing.xxl, start = Spacing.screen, end = Spacing.screen)) {
                    Text(text = "Catch up", style = MaterialTheme.typography.headlineMedium)

                    Text(
                        text = subtitle(state),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }

            if (state.overdue.isNotEmpty()) {
                item { Header("Overdue") }

                items(state.overdue, key = { it.id }) { item ->
                    CatchUpRow(
                        item = item,
                        onClick = { onPersonClick(item.id) },
                        onReachedOut = { onReachedOut(item) }
                    )
                }
            }

            if (state.dueThisWeek.isNotEmpty()) {
                item { Header("Due this week") }

                items(state.dueThisWeek, key = { it.id }) { item ->
                    CatchUpRow(
                        item = item,
                        onClick = { onPersonClick(item.id) },
                        onReachedOut = { onReachedOut(item) }
                    )
                }
            }

            if (state.isEmpty) {
                item {
                    Text(
                        text = "Nobody's due. Everyone you track has heard from you lately.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkFaint,
                        modifier = Modifier.padding(Spacing.xxl)
                    )
                }
            }
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

@Composable
private fun CatchUpRow(item: CatchUpItem, onClick: () -> Unit, onReachedOut: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = Spacing.md, vertical = Spacing.xxs)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(color = if (item.isOverdue) Surface100 else Color.Transparent)
            .padding(horizontal = Spacing.sm, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Avatar(initials = item.initials, highlighted = item.isOverdue)

            Column(Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium)

                Text(
                    text = item.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.isOverdue) Attention else InkFaint,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }

        // One tap says "reached out" without opening anyone.
        Box(
            modifier = Modifier
                .size(Sizes.avatar)
                .clip(Pill)
                .background(color = Surface200)
                .clickable(onClick = onReachedOut),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = "Mark ${item.name} as reached out",
                tint = InkMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun subtitle(state: CatchUpUiState): String = when (state.total) {
    0 -> "Nobody to reach out to"
    1 -> "1 person to reach out to"
    else -> "${state.total} people to reach out to"
}
