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

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val message = when (event) {
                is SettingsEvent.BackupWritten ->
                    if (event.succeeded) "Backup saved." else "Couldn't write that backup."
                SettingsEvent.EverythingDeleted -> "Everything deleted."
            }

            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
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

                val needsPermission = enabled &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED

                if (needsPermission) {
                    notificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onScheduleClick = viewModel::onScheduleClick,
            onNudgeOnlyWhenOverdueChange = viewModel::onNudgeOnlyWhenOverdueChange,
            onCadenceClick = viewModel::onCadenceClick,
            onExportClick = { exportLauncher.launch(viewModel.backupFileName()) },
            onImportClick = viewModel::onImportClick,
            onWipeClick = viewModel::onWipeClick
        )
    }

    if (state.isPickingSchedule) {
        ScheduleDialog(
            day = state.nudgeDay,
            time = state.nudgeTime,
            onDismiss = viewModel::onScheduleDismiss,
            onConfirm = viewModel::onScheduleChange
        )
    }

    if (state.isPickingCadence) {
        CadenceDialog(
            selected = state.defaultCadence,
            onDismiss = viewModel::onCadenceDismiss,
            onSelect = viewModel::onCadenceChange
        )
    }

    if (state.isConfirmingWipe) {
        WipeDialog(onDismiss = viewModel::onWipeDismiss, onConfirm = viewModel::onWipeConfirm)
    }
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    modifier: Modifier = Modifier,
    onNudgeEnabledChange: (Boolean) -> Unit,
    onScheduleClick: () -> Unit,
    onNudgeOnlyWhenOverdueChange: (Boolean) -> Unit,
    onCadenceClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onWipeClick: () -> Unit
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
                ToggleRow(
                    label = "Weekly nudge",
                    checked = state.nudgeEnabled,
                    onCheckedChange = onNudgeEnabledChange
                )

                Divider()

                ValueRow(
                    label = "Nudge me on",
                    value = state.nudgeScheduleLabel,
                    enabled = state.nudgeEnabled,
                    onClick = onScheduleClick
                )

                Divider()

                ToggleRow(
                    label = "Only when someone is overdue",
                    checked = state.nudgeOnlyWhenOverdue,
                    onCheckedChange = onNudgeOnlyWhenOverdueChange
                )
            }
        }

        item { Header("Defaults") }

        item {
            Card {
                ValueRow(
                    label = "Cadence for new people",
                    value = state.defaultCadenceLabel,
                    onClick = onCadenceClick
                )
            }
        }

        item { Header("Your data") }

        item {
            Card {
                ValueRow(label = "Export a backup", value = null, onClick = onExportClick)

                Divider()

                ValueRow(label = "Import from contacts", value = null, onClick = onImportClick)
            }
        }

        item {
            Text(
                text = "Everything lives on this phone. Tether has no account and no server, so " +
                        "a backup is the only copy that survives a lost device.",
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

        item {
            Box(
                modifier = Modifier
                    .padding(top = Spacing.screen, start = Spacing.screen, end = Spacing.screen)
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(color = DangerWash, shape = MaterialTheme.shapes.medium)
                    .clickable(onClick = onWipeClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Delete everything",
                    style = MaterialTheme.typography.labelLarge,
                    color = Danger
                )
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

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
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

@Composable
private fun CadenceDialog(
    selected: CadencePreset,
    onDismiss: () -> Unit,
    onSelect: (CadencePreset) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface100,
        title = {
            Text(text = "Cadence for new people", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column {
                // "Never" is missing on purpose: a default of never would add people the app
                // then says nothing about.
                CadencePreset.entries.filter { it.days != null }.forEach { preset ->
                    Text(
                        text = preset.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (preset == selected) Accent else Ink,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(preset) }
                            .padding(vertical = Spacing.md)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", style = MaterialTheme.typography.labelLarge, color = InkMuted)
            }
        }
    )
}

@Composable
private fun WipeDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface100,
        title = { Text(text = "Delete everything?", style = MaterialTheme.typography.titleMedium) },
        text = {
            Text(
                text = "Every person, detail and catch-up you've logged. There's no server and " +
                        "no account, so unless you've exported a backup, this is the only copy.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkMuted
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Delete everything",
                    style = MaterialTheme.typography.labelLarge,
                    color = Danger
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Keep it", style = MaterialTheme.typography.labelLarge, color = InkMuted)
            }
        }
    )
}
