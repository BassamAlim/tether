package bassamalim.tether.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bassamalim.tether.core.backup.ImportPreview
import bassamalim.tether.core.ui.theme.Accent
import bassamalim.tether.core.ui.theme.Ink
import bassamalim.tether.core.ui.theme.InkMuted
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface100

/**
 * The one question worth asking before a restore: is this the file you meant?
 *
 * Shared, because a backup has to be reachable from Settings and from First run — the screen a
 * reinstalled phone opens on, where there is no Settings to get to yet.
 *
 * It says what the file holds rather than only its name, because a backup's name is a date and
 * two of them look alike, and it says what a restore does, because "import" reads like it might
 * replace everything and this one never does.
 */
@Composable
fun ImportDialog(preview: ImportPreview, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface100,
        title = { Text(text = "Import this backup?", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column {
                Text(
                    text = preview.contents,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink
                )

                Text(
                    text = "Exported ${preview.exportedOn}",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                    modifier = Modifier.padding(top = Spacing.xs)
                )

                Text(
                    text = "Nothing on this phone is removed or overwritten. Anyone already in " +
                            "Tether keeps what they've got, and only what's missing is added — " +
                            "so importing the same file twice changes nothing the second time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                    modifier = Modifier.padding(top = Spacing.lg)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Import",
                    style = MaterialTheme.typography.labelLarge,
                    color = Accent
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkMuted
                )
            }
        }
    )
}
