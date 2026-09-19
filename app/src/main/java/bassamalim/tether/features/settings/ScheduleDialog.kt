package bassamalim.tether.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import bassamalim.tether.core.ui.components.ClockDial
import bassamalim.tether.core.ui.components.FilterPill
import bassamalim.tether.core.ui.components.rememberClockState
import bassamalim.tether.core.ui.components.time
import bassamalim.tether.core.ui.theme.Accent
import bassamalim.tether.core.ui.theme.InkMuted
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface100
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

/** Which day and hour the weekly nudge lands on. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDialog(
    day: DayOfWeek,
    time: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (DayOfWeek, LocalTime) -> Unit
) {
    var selectedDay by remember { mutableStateOf(day) }
    val timeState = rememberClockState(time)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface100,
        title = { Text(text = "Nudge me on", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    DayOfWeek.entries.forEach { option ->
                        FilterPill(
                            label = option.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            selected = option == selectedDay,
                            onClick = { selectedDay = option }
                        )
                    }
                }

                ClockDial(state = timeState, modifier = Modifier.padding(top = Spacing.screen))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(selectedDay, timeState.time)
                }
            ) {
                Text(text = "Save", style = MaterialTheme.typography.labelLarge, color = Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", style = MaterialTheme.typography.labelLarge, color = InkMuted)
            }
        }
    )
}
