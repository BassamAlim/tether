package bassamalim.tether.features.firstRun

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.tether.core.ui.components.ImportDialog
import bassamalim.tether.core.ui.theme.Accent
import bassamalim.tether.core.ui.theme.AccentInk
import bassamalim.tether.core.ui.theme.Action
import bassamalim.tether.core.ui.theme.Ink
import bassamalim.tether.core.ui.theme.InkFaint
import bassamalim.tether.core.ui.theme.InkMuted
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface0
import bassamalim.tether.core.ui.theme.Surface200
import bassamalim.tether.core.ui.theme.TetherType
import bassamalim.tether.R
import androidx.compose.ui.res.painterResource

/**
 * The only screen that has to explain anything: one line on what Tether does, then the ways in.
 *
 * Restoring a backup is one of them, and has to be, because this is the screen a reinstalled
 * phone opens on — Settings, where the same import lives, is inside the tab shell the app won't
 * open until somebody is in Tether. It sits quietly under the other two: it's the rarest way in
 * and the only one that isn't a beginning.
 */
@Composable
fun FirstRunScreen(viewModel: FirstRunViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        // A cancelled picker isn't a failed import, so it says nothing.
        if (uri == null) return@rememberLauncherForActivityResult

        viewModel.onBackupPicked(
            runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
        )
    }

    LaunchedEffect(state.hasPeople) {
        if (state.hasPeople) viewModel.onPeopleExist()
    }

    // An import stacks the picker and the set-up walk over this screen, so it can be returned
    // to with twenty new people behind it. Saying nothing until the count is in beats flashing
    // "Nobody here yet" at someone who just added them.
    if (state.isLoading || state.hasPeople) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Surface0)
        )
        return
    }

    FirstRunScreen(
        state = state,
        onAddPersonClick = viewModel::onAddPersonClick,
        onImportContactsClick = viewModel::onImportContactsClick,
        onImportBackupClick = { backupLauncher.launch(arrayOf("*/*")) }
    )

    state.pendingImport?.let { preview ->
        ImportDialog(
            preview = preview,
            onDismiss = viewModel::onImportDismiss,
            onConfirm = viewModel::onImportConfirm
        )
    }
}

@Composable
private fun FirstRunScreen(
    state: FirstRunUiState,
    onAddPersonClick: () -> Unit,
    onImportContactsClick: () -> Unit,
    onImportBackupClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface0)
            .padding(horizontal = Spacing.screen)
            .padding(bottom = Spacing.xxxl)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Spacing.md),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_tether_mark),
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(76.dp)
            )

            Text(
                text = "Nobody here yet",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = Spacing.xxl)
            )

            Text(
                text = "Add the people you don't want to drift away from, and say how often " +
                        "you'd like to be in touch. Tether keeps count and tells you who's " +
                        "slipping.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.md)
            )
        }

        PrimaryButton(label = "Add your first person", onClick = onAddPersonClick)

        SecondaryButton(
            label = "Pick from contacts",
            onClick = onImportContactsClick,
            modifier = Modifier.padding(top = 10.dp)
        )

        Text(
            text = "Contacts are read on this phone and never leave it.",
            style = TetherType.Caption,
            color = InkFaint,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 14.dp)
                .fillMaxWidth()
        )

        Text(
            text = if (state.isImporting) "Importing…" else "Import a backup",
            style = MaterialTheme.typography.labelMedium,
            color = InkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = Spacing.lg)
                .fillMaxWidth()
                .clickable(enabled = !state.isImporting, onClick = onImportBackupClick)
                .padding(vertical = Spacing.sm)
        )

        state.importProblem?.let { problem ->
            Text(
                text = problem,
                style = TetherType.Caption,
                color = InkFaint,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = Spacing.sm)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(label = label, onClick = onClick, background = Action, content = AccentInk, modifier = modifier)
}

@Composable
private fun SecondaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(label = label, onClick = onClick, background = Surface200, content = Ink, modifier = modifier)
}

@Composable
private fun Button(
    label: String,
    onClick: () -> Unit,
    background: Color,
    content: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(color = background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = content)
    }
}
