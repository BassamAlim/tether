package bassamalim.tether.features.importContacts

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.tether.core.ui.components.Avatar
import bassamalim.tether.core.ui.components.SearchField
import bassamalim.tether.core.ui.components.SelectionTick
import bassamalim.tether.core.ui.theme.AccentInk
import bassamalim.tether.core.ui.theme.Action
import bassamalim.tether.core.ui.theme.Ink
import bassamalim.tether.core.ui.theme.InkFaint
import bassamalim.tether.core.ui.theme.InkMuted
import bassamalim.tether.core.ui.theme.Sizes
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface0
import bassamalim.tether.core.ui.theme.Surface100
import bassamalim.tether.core.ui.theme.Surface300
import bassamalim.tether.core.ui.theme.TetherType

@Composable
fun ImportContactsScreen(viewModel: ImportContactsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        viewModel::onPermissionResult
    )

    // Asked at the moment you open the picker, not at first launch.
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) viewModel.onPermissionResult(true)
        else permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    ImportContactsScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onToggle = viewModel::onToggle,
        onCancel = viewModel::onCancel,
        onImport = viewModel::onImport,
        onRequestPermission = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) }
    )
}

@Composable
private fun ImportContactsScreen(
    state: ImportContactsUiState,
    onQueryChange: (String) -> Unit,
    onToggle: (Long) -> Unit,
    onCancel: () -> Unit,
    onImport: () -> Unit,
    onRequestPermission: () -> Unit
) {
    Scaffold(
        containerColor = Surface0,
        bottomBar = {
            if (state.hasPermission) {
                BottomBar(state = state, onImport = onImport)
            }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TopBar(selectedCount = state.selectedCount, onCancel = onCancel)

            if (!state.hasPermission) {
                PermissionNotice(
                    isDenied = state.isPermissionDenied,
                    onRequestPermission = onRequestPermission
                )
                return@Column
            }

            SearchField(
                value = state.query,
                onValueChange = onQueryChange,
                placeholder =
                    if (state.totalCount == 0) "Search your contacts"
                    else "Search ${state.totalCount} contacts",
                modifier = Modifier.padding(top = Spacing.md, start = Spacing.screen, end = Spacing.screen)
            )

            Text(
                text = "Tick only the people you actually want to keep up with. Tether copies " +
                        "them once. It never syncs, and never writes back to your contacts.",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
                modifier = Modifier.padding(top = 14.dp, start = Spacing.screen, end = Spacing.screen)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = Spacing.md, bottom = Spacing.lg)
            ) {
                items(state.contacts, key = { it.id }) { contact ->
                    ContactRow(contact = contact, onClick = { onToggle(contact.id) })
                }

                if (!state.isLoading && state.contacts.isEmpty()) {
                    item {
                        Text(
                            text = "No contacts match that.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkFaint,
                            modifier = Modifier.padding(Spacing.xxl)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(selectedCount: Int, onCancel: () -> Unit) {
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
            text = "From contacts",
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = if (selectedCount == 0) "" else "$selectedCount",
            style = MaterialTheme.typography.labelLarge,
            color = InkFaint,
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md)
        )
    }
}

@Composable
private fun PermissionNotice(isDenied: Boolean, onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text =
                if (isDenied) "Tether can't read your contacts without permission. " +
                        "It only reads them. Nothing is ever written back."
                else "Asking for access to your contacts…",
            style = MaterialTheme.typography.bodyMedium,
            color = InkMuted,
            textAlign = TextAlign.Center
        )

        if (isDenied) {
            Box(
                modifier = Modifier
                    .height(52.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(color = Action)
                    .clickable(onClick = onRequestPermission)
                    .padding(horizontal = Spacing.xl),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Allow access",
                    style = MaterialTheme.typography.labelLarge,
                    color = AccentInk
                )
            }
        }
    }
}

@Composable
private fun ContactRow(contact: ContactRow, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = Spacing.md, vertical = 1.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(color = if (contact.isSelected) Surface100 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.sm, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Avatar(initials = contact.initials)

        Column(Modifier.weight(1f)) {
            Text(text = contact.name, style = MaterialTheme.typography.titleMedium)

            if (contact.subtitle.isNotEmpty()) {
                Text(
                    text = contact.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }

        SelectionTick(isSelected = contact.isSelected)
    }
}

@Composable
private fun BottomBar(state: ImportContactsUiState, onImport: () -> Unit) {
    Column(
        Modifier
            .background(Surface0)
            .fillMaxWidth()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(Sizes.border)
                .background(Surface300)
        )

        Column(Modifier.padding(start = Spacing.screen, end = Spacing.screen, top = Spacing.lg, bottom = 28.dp)) {
            Text(
                text = "Next you'll go through them one at a time, to say what each of them " +
                        "is to you and how often you'd like to be in touch.",
                style = TetherType.Caption,
                color = InkFaint
            )

            Box(
                modifier = Modifier
                    .padding(top = Spacing.md)
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(color = if (state.canImport) Action else Surface100)
                    .clickable(enabled = state.canImport, onClick = onImport),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (state.selectedCount) {
                        0 -> "Pick who's worth keeping up with"
                        1 -> "Add 1 person"
                        else -> "Add ${state.selectedCount} people"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (state.canImport) AccentInk else Ink
                )
            }
        }
    }
}
