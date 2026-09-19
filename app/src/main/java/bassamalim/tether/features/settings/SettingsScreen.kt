package bassamalim.tether.features.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.tether.core.enums.CadencePreset
import bassamalim.tether.core.ui.components.ImportDialog
import bassamalim.tether.core.ui.components.SectionLabel
import bassamalim.tether.core.ui.theme.Accent
import bassamalim.tether.core.ui.theme.AccentInk
import bassamalim.tether.core.ui.theme.Danger
import bassamalim.tether.core.ui.theme.DangerWash
import bassamalim.tether.core.ui.theme.Ink
import bassamalim.tether.core.ui.theme.InkFaint
import bassamalim.tether.core.ui.theme.InkMuted
import bassamalim.tether.core.ui.theme.Sizes
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface0
import bassamalim.tether.core.ui.theme.Surface100
import bassamalim.tether.core.ui.theme.Surface300
import bassamalim.tether.core.ui.theme.TetherType
import java.time.DayOfWeek
import java.time.LocalTime
import bassamalim.tether.R
import androidx.compose.ui.res.painterResource

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val notificationsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Denied only means the nudge stays silent; the setting is still the user's answer. */ }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        viewModel.onExport { json ->
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            }.isSuccess
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        // A cancelled picker isn't a failed import, so it says nothing.
        if (uri == null) return@rememberLauncherForActivityResult

        viewModel.onImportPicked(
            runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val message = when (event) {
                is SettingsEvent.BackupWritten ->
                    if (event.succeeded) "Backup saved." else "Couldn't write that backup."

                is SettingsEvent.BackupRestored -> event.summary

                SettingsEvent.BackupUnreadable -> "That doesn't look like a Tether backup."

                SettingsEvent.BackupTooNew ->
                    "That backup was written by a newer Tether than this one."
            }

            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    // Asked whenever the nudge is switched on, by the switch or by picking a time.
    val requestNotifications = {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED

        if (needsPermission) notificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    Scaffold(
        containerColor = Surface0,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        SettingsContent(
            state = state,
            modifier = Modifier.padding(innerPadding),
            onNudgeEnabledChange = { enabled ->
                viewModel.onNudgeEnabledChange(enabled)
                if (enabled) requestNotifications()
            },
            onScheduleClick = viewModel::onScheduleClick,
            onExportClick = { exportLauncher.launch(viewModel.backupFileName()) },
            // Anything, rather than application/json: a backup that came back off a desktop or
            // out of a chat app is routinely handed to the picker as text/plain or octet-stream,
            // and a file the reader can't parse is caught a moment later anyway.
            onImportClick = { importLauncher.launch(arrayOf("*/*")) }
        )
    }

    state.pendingImport?.let { preview ->
        ImportDialog(
            preview = preview,
            onDismiss = viewModel::onImportDismiss,
            onConfirm = viewModel::onImportConfirm
        )
    }

    if (state.isPickingSchedule) {
        ScheduleDialog(
            day = state.nudgeDay,
            time = state.nudgeTime,
            onDismiss = viewModel::onScheduleDismiss,
            onConfirm = { day, time ->
                viewModel.onScheduleChange(day, time)
                requestNotifications()
            }
        )
    }

}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    modifier: Modifier = Modifier,
    onNudgeEnabledChange: (Boolean) -> Unit,
    onScheduleClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.xxl)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = Spacing.xxl, start = Spacing.screen, end = Spacing.screen)
            )
        }

        item { Header("Nudges") }

        item {
            Card {
                NudgeRow(
                    schedule = state.nudgeScheduleLabel,
                    checked = state.nudgeEnabled,
                    onClick = onScheduleClick,
                    onCheckedChange = onNudgeEnabledChange
                )
            }
        }

        item { Header("Your data") }

        item {
            // Copying people in from contacts used to live here. It belongs with adding a
            // person, not with your data: it's a way in, not a setting. A backup is both ways
            // out and back, so export and import sit together.
            Card {
                ValueRow(label = "Export a backup", value = null, onClick = onExportClick)

                Divider()

                ValueRow(
                    label = "Import a backup",
                    value = null,
                    enabled = !state.isImporting,
                    onClick = onImportClick
                )
            }
        }

        item {
            Text(
                text = "Everything lives on this phone. Tether has no account and no server, so " +
                        "a backup is the only copy that survives a lost device. Importing one " +
                        "adds what's missing and leaves what's already here alone.",
                style = TetherType.Caption,
                color = InkFaint,
                modifier = Modifier.padding(top = 10.dp, start = Spacing.screen, end = Spacing.screen)
            )
        }

        item { Header("About") }

        item {
            Card {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Version", style = MaterialTheme.typography.bodyMedium)

                    Text(
                        text = state.version,
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkFaint
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
private fun Card(content: @Composable () -> Unit) {
    Column(
        Modifier
            .padding(horizontal = Spacing.screen)
            .fillMaxWidth()
            .background(color = Surface100, shape = MaterialTheme.shapes.medium)
            .padding(horizontal = Spacing.lg, vertical = Spacing.xxs)
    ) {
        content()
    }
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(Sizes.border)
            .background(Surface300)
    )
}

/**
 * Whether and when, in one row: the text opens the schedule, the switch turns it on and off. The
 * rule between them says they're two targets. Picking a time while it's off turns it on, so the
 * text is never a dead tap.
 */
@Composable
private fun NudgeRow(
    schedule: String,
    checked: Boolean,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick)
                .padding(vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
        ) {
            Text(text = "Weekly nudge", style = MaterialTheme.typography.bodyMedium)

            Text(
                text = schedule,
                style = MaterialTheme.typography.bodySmall,
                color = if (checked) InkMuted else InkFaint
            )
        }

        Box(
            Modifier
                .size(width = Sizes.border, height = Spacing.xl)
                .background(Surface300)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AccentInk,
                checkedTrackColor = Accent,
                checkedBorderColor = Accent,
                uncheckedThumbColor = InkFaint,
                uncheckedTrackColor = Surface300,
                uncheckedBorderColor = Surface300
            )
        )
    }
}

@Composable
private fun ValueRow(
    label: String,
    value: String?,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) Ink else InkFaint,
            modifier = Modifier.weight(1f)
        )

        value?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium, color = InkMuted)
        }

        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = InkFaint,
            modifier = Modifier.size(18.dp)
        )
    }
}
