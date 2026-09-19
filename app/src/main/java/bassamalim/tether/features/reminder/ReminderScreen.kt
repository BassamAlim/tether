package bassamalim.tether.features.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.tether.core.enums.InteractionType
import bassamalim.tether.core.ui.components.ClockDial
import bassamalim.tether.core.ui.components.FilterPill
import bassamalim.tether.core.ui.components.SectionLabel
import bassamalim.tether.core.ui.components.rememberClockState
import bassamalim.tether.core.ui.components.time
import bassamalim.tether.core.ui.theme.Accent
import bassamalim.tether.core.ui.theme.AccentInk
import bassamalim.tether.core.ui.theme.AccentWash
import bassamalim.tether.core.ui.theme.Danger
import bassamalim.tether.core.ui.theme.DangerWash
import bassamalim.tether.core.ui.theme.Ink
import bassamalim.tether.core.ui.theme.InkFaint
import bassamalim.tether.core.ui.theme.InkMuted
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface0
import bassamalim.tether.core.ui.theme.Surface100
import bassamalim.tether.core.ui.theme.Surface200
import bassamalim.tether.core.ui.theme.TetherType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

@Composable
fun ReminderScreen(viewModel: ReminderViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ReminderScreen(
        state = state,
        onCancel = viewModel::onCancel,
        onSave = viewModel::onSave,
        onTypeSelect = viewModel::onTypeSelect,
        onTomorrowSelect = viewModel::onTomorrowSelect,
        onNextWeekSelect = viewModel::onNextWeekSelect,
        onPickDate = viewModel::onPickDate,
        onPickTime = viewModel::onPickTime,
        onRemove = viewModel::onRemove
    )

    if (state.isPickingDate) {
        DayPicker(
            initial = state.date,
            today = state.today,
            onPicked = viewModel::onDatePicked,
            onDismiss = viewModel::onDatePickerDismiss
        )
    }

    if (state.isPickingTime) {
        TimePickerDialog(
            initial = state.time,
            onPicked = viewModel::onTimePicked,
            onDismiss = viewModel::onTimePickerDismiss
        )
    }
}

/**
 * One reminder about one person: what you mean to do, and when to be told. The cadence is the
 * standing arrangement; this is the thing you thought of just now and don't want to lose.
 *
 * There is no board for it — the design predates reminders — so it follows New person's shape:
 * Cancel, title, lime Save, then the fields.
 */
@Composable
private fun ReminderScreen(
    state: ReminderUiState,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onTypeSelect: (InteractionType) -> Unit,
    onTomorrowSelect: () -> Unit,
    onNextWeekSelect: () -> Unit,
    onPickDate: () -> Unit,
    onPickTime: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Surface0)
    ) {
        TopBar(
            title = if (state.isEditing) "Edit reminder" else "Remind me",
            canSave = state.canSave,
            onCancel = onCancel,
            onSave = onSave
        )

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = Spacing.screen, end = Spacing.screen, bottom = 28.dp)
        ) {
            Text(
                text =
                    if (state.personName.isEmpty()) "Tether will tell you when."
                    else "Tether will tell you when, about ${state.personName}.",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
                modifier = Modifier.padding(top = Spacing.md)
            )

            SectionLabel(text = "What for", modifier = Modifier.padding(top = Spacing.screen))

            FlowRow(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                // Optional, as on the log sheet: "remind me about Maya" is a whole thought.
                InteractionType.entries.forEach { type ->
                    FilterPill(
                        label = type.label,
                        selected = type == state.type,
                        onClick = { onTypeSelect(type) }
                    )
                }
            }

            SectionLabel(text = "When", modifier = Modifier.padding(top = Spacing.screen))

            Row(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .background(color = Surface200, shape = MaterialTheme.shapes.medium)
                    .padding(Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Segment(
                    label = "Tomorrow",
                    selected = state.whenOption == WhenOption.TOMORROW,
                    onClick = onTomorrowSelect,
                    modifier = Modifier.weight(1f)
                )

                Segment(
                    label = "Next week",
                    selected = state.whenOption == WhenOption.NEXT_WEEK,
                    onClick = onNextWeekSelect,
                    modifier = Modifier.weight(1f)
                )

                Segment(
                    // Once a day is chosen the segment shows it, so nothing is hidden.
                    label =
                        if (state.whenOption == WhenOption.PICKED) state.dateLabel
                        else "Pick a day",
                    selected = state.whenOption == WhenOption.PICKED,
                    onClick = onPickDate,
                    modifier = Modifier.weight(1f)
                )
            }

            ValueRow(
                label = "At",
                value = state.timeLabel,
                onClick = onPickTime,
                modifier = Modifier.padding(top = Spacing.md)
            )

            if (!state.isInFuture) {
                Note(
                    text = "That moment has passed. Pick a later one.",
                    color = Danger,
                    modifier = Modifier.padding(top = Spacing.md)
                )
            }

            if (!state.canNotify) {
                Note(
                    text = "Notifications are off for Tether, so this reminder won't reach " +
                            "you until you turn them on.",
                    color = InkFaint,
                    modifier = Modifier.padding(top = Spacing.md)
                )
            }

            if (state.isEditing) {
                Box(
                    modifier = Modifier
                        .padding(top = Spacing.screen)
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(color = DangerWash)
                        .clickable(enabled = !state.isSaving, onClick = onRemove),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Remove reminder",
                        style = MaterialTheme.typography.labelLarge,
                        color = Danger
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    canSave: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
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
            text = title,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "Save",
            style = MaterialTheme.typography.labelLarge,
            // Dimmed rather than hidden, as on New person: you can see what's left to do.
            color = if (canSave) Accent else InkFaint,
            modifier = Modifier
                .clickable(enabled = canSave, onClick = onSave)
                .padding(horizontal = Spacing.md, vertical = Spacing.md)
        )
    }
}

@Composable
private fun Segment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(color = if (selected) AccentWash else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight(700) else FontWeight(600),
            color = if (selected) Accent else InkMuted
        )
    }
}

@Composable
private fun ValueRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(color = Surface200)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Ink,
            modifier = Modifier.weight(1f)
        )

        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = Accent)
    }
}

@Composable
private fun Note(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(text = text, style = TetherType.Caption, color = color, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayPicker(
    initial: LocalDate,
    today: LocalDate,
    onPicked: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val todayMillis = today.toEpochDay() * MILLIS_PER_DAY

    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initial.toEpochDay() * MILLIS_PER_DAY,
        selectableDates = object : SelectableDates {
            // The mirror image of the log sheet: you can be reminded ahead, not behind.
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayMillis
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = DatePickerDefaults.colors(containerColor = Surface100),
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let {
                        onPicked(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                }
            ) {
                Text(
                    text = "Use this day",
                    style = MaterialTheme.typography.labelLarge,
                    color = Accent
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", style = MaterialTheme.typography.labelLarge, color = InkMuted)
            }
        }
    ) {
        DatePicker(state = pickerState)
    }
}

/** The same clock the nudge schedule uses, so the two read alike. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initial: LocalTime,
    onPicked: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timeState = rememberClockState(initial)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface100,
        title = { Text(text = "Remind me at", style = MaterialTheme.typography.titleMedium) },
        text = { ClockDial(state = timeState) },
        confirmButton = {
            TextButton(onClick = { onPicked(timeState.time) }) {
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

private const val MILLIS_PER_DAY = 86_400_000L
