package bassamalim.tether.core.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bassamalim.tether.core.ui.theme.Accent
import bassamalim.tether.core.ui.theme.AccentInk
import bassamalim.tether.core.ui.theme.AccentWash
import bassamalim.tether.core.ui.theme.Ink
import bassamalim.tether.core.ui.theme.InkMuted
import bassamalim.tether.core.ui.theme.Surface100
import bassamalim.tether.core.ui.theme.Surface200
import bassamalim.tether.core.ui.theme.Surface300
import java.time.LocalTime

/** A 12-hour clock state seeded from [initial]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberClockState(initial: LocalTime): TimePickerState = rememberTimePickerState(
    initialHour = initial.hour,
    initialMinute = initial.minute,
    is24Hour = false
)

/** The picked time, whatever half of the day the dial is on. */
@OptIn(ExperimentalMaterial3Api::class)
val TimePickerState.time: LocalTime get() = LocalTime.of(hour, minute)

/**
 * The clock-face time picker, in Tether's colours. Shared by the nudge schedule and reminders
 * so the two read alike.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockDial(state: TimePickerState, modifier: Modifier = Modifier) {
    TimePicker(
        state = state,
        modifier = modifier,
        colors = TimePickerDefaults.colors(
            clockDialColor = Surface200,
            clockDialSelectedContentColor = AccentInk,
            clockDialUnselectedContentColor = Ink,
            selectorColor = Accent,
            containerColor = Surface100,
            periodSelectorBorderColor = Surface300,
            periodSelectorSelectedContainerColor = AccentWash,
            periodSelectorUnselectedContainerColor = Surface100,
            periodSelectorSelectedContentColor = Accent,
            periodSelectorUnselectedContentColor = InkMuted,
            timeSelectorSelectedContainerColor = AccentWash,
            timeSelectorSelectedContentColor = Accent,
            timeSelectorUnselectedContainerColor = Surface200,
            timeSelectorUnselectedContentColor = Ink
        )
    )
}
