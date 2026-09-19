package bassamalim.tether.features.logInteraction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.tether.core.enums.Initiator
import bassamalim.tether.core.enums.InteractionType
import bassamalim.tether.core.ui.components.FilterPill
import bassamalim.tether.core.ui.components.LabeledTextField
import bassamalim.tether.core.ui.components.SectionLabel
import bassamalim.tether.core.ui.theme.Accent
import bassamalim.tether.core.ui.theme.AccentInk
import bassamalim.tether.core.ui.theme.AccentWash
import bassamalim.tether.core.ui.theme.Action
import bassamalim.tether.core.ui.theme.Danger
import bassamalim.tether.core.ui.theme.DangerWash
import bassamalim.tether.core.ui.theme.Ink
import bassamalim.tether.core.ui.theme.InkFaint
import bassamalim.tether.core.ui.theme.InkMuted
import bassamalim.tether.core.ui.theme.Overlay
import bassamalim.tether.core.ui.theme.Pill
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface100
import bassamalim.tether.core.ui.theme.Surface200
import bassamalim.tether.core.ui.theme.Surface300
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val dayMonth = DateTimeFormatter.ofPattern("d MMM")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogInteractionScreen(viewModel: LogInteractionViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = viewModel::onDismiss,
        sheetState = sheetState,
        containerColor = Surface100,
        scrimColor = Overlay,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { DragHandle() }
    ) {
        SheetContent(
            state = state,
            onTypeSelect = viewModel::onTypeSelect,
            onTodaySelect = viewModel::onTodaySelect,
            onYesterdaySelect = viewModel::onYesterdaySelect,
            onPickDate = viewModel::onPickDate,
            onLocationChange = viewModel::onLocationChange,
            onInitiatorSelect = viewModel::onInitiatorSelect,
            onNoteChange = viewModel::onNoteChange,
            onSave = viewModel::onSave,
            onDelete = viewModel::onDelete
        )
    }

    if (state.isPickingDate) {
        DatePicker(
            initial = state.occurredOn,
            today = state.today,
            onPicked = viewModel::onDatePicked,
            onDismiss = viewModel::onDatePickerDismiss
        )
    }
}

@Composable
private fun SheetContent(
    state: LogInteractionUiState,
    onTypeSelect: (InteractionType) -> Unit,
    onTodaySelect: () -> Unit,
    onYesterdaySelect: () -> Unit,
    onPickDate: () -> Unit,
    onLocationChange: (String) -> Unit,
    onInitiatorSelect: (Initiator) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(start = Spacing.screen, end = Spacing.screen, bottom = 28.dp)
    ) {
        Text(text = state.title, style = MaterialTheme.typography.titleMedium)

        PersonPill(name = state.personName, initials = state.initials)

        SectionLabel(text = "How", modifier = Modifier.padding(top = 22.dp))

        FlowRow(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            InteractionType.entries.forEach { type ->
                FilterPill(
                    label = type.label,
                    selected = type == state.type,
                    onClick = { onTypeSelect(type) }
                )
            }
        }

        SectionLabel(text = "When", modifier = Modifier.padding(top = Spacing.screen))

        WhenControl(
            state = state,
            onTodaySelect = onTodaySelect,
            onYesterdaySelect = onYesterdaySelect,
            onPickDate = onPickDate
        )

        LabeledTextField(
            label = "Where",
            value = state.location,
            onValueChange = onLocationChange,
            placeholder = "Blue Tokai, their place, the office",
            modifier = Modifier.padding(top = Spacing.screen)
        )

        SectionLabel(text = "Who reached out", modifier = Modifier.padding(top = Spacing.screen))

        FlowRow(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            // Pills rather than a segmented control, because unlike When this has a third
            // answer — no one did, you ran into each other — and a pill can be tapped off.
            Initiator.entries.forEach { initiator ->
                FilterPill(
                    label = initiator.label,
                    selected = initiator == state.initiatedBy,
                    onClick = { onInitiatorSelect(initiator) }
                )
            }
        }

        SectionLabel(
            text = "What you talked about",
            modifier = Modifier.padding(top = Spacing.screen)
        )

        NoteField(
            value = state.note,
            onValueChange = onNoteChange,
            modifier = Modifier.padding(top = 10.dp)
        )

        Box(
            modifier = Modifier
                .padding(top = Spacing.screen)
                .fillMaxWidth()
                .height(52.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(color = Action)
                .clickable(enabled = state.canSave, onClick = onSave),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Save",
                style = MaterialTheme.typography.labelLarge,
                color = AccentInk
            )
        }

        // Only on a catch-up that exists: it's a way to take one back out of the history, not
        // a way to abandon one you haven't written yet — Cancel does that by dismissing.
        if (state.isEditing) {
            Box(
                modifier = Modifier
                    .padding(top = Spacing.md)
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(color = DangerWash)
                    .clickable(enabled = state.canSave, onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Delete this catch-up",
                    style = MaterialTheme.typography.labelLarge,
                    color = Danger
                )
            }
        }
    }
}

@Composable
private fun DragHandle() {
    Box(Modifier.fillMaxWidth().padding(top = Spacing.md, bottom = 18.dp)) {
        Box(
            Modifier
                .align(Alignment.Center)
                .width(36.dp)
                .height(4.dp)
                .background(color = Surface300, shape = Pill)
        )
    }
}

@Composable
private fun PersonPill(name: String, initials: String) {
    Row(
        modifier = Modifier
            .padding(top = 14.dp)
            .background(color = Surface200, shape = Pill)
            .padding(start = 5.dp, end = 14.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(color = AccentWash, shape = Pill),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.labelSmall,
                color = Accent
            )
        }

        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = Ink
        )
    }
}

@Composable
private fun WhenControl(
    state: LogInteractionUiState,
    onTodaySelect: () -> Unit,
    onYesterdaySelect: () -> Unit,
    onPickDate: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
            .background(color = Surface200, shape = MaterialTheme.shapes.medium)
            .padding(Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Segment(
            label = "Today",
            selected = state.whenOption == WhenOption.TODAY,
            onClick = onTodaySelect,
            modifier = Modifier.weight(1f)
        )

        Segment(
            label = "Yesterday",
            selected = state.whenOption == WhenOption.YESTERDAY,
            onClick = onYesterdaySelect,
            modifier = Modifier.weight(1f)
        )

        Segment(
            // Once a date is chosen the segment shows it, so the sheet never hides what it'll save.
            label =
                if (state.whenOption == WhenOption.PICKED) state.occurredOn.format(dayMonth)
                else "Pick a date",
            selected = state.whenOption == WhenOption.PICKED,
            onClick = onPickDate,
            modifier = Modifier.weight(1f)
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
private fun NoteField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .background(color = Surface200, shape = MaterialTheme.shapes.medium)
            .padding(horizontal = 14.dp, vertical = Spacing.md)
    ) {
        if (value.isEmpty()) {
            Text(
                text = "What you'd want to remember in a year",
                style = MaterialTheme.typography.bodySmall,
                color = InkFaint
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodySmall.merge(TextStyle(color = Ink)),
            cursorBrush = SolidColor(Accent),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePicker(
    initial: LocalDate,
    today: LocalDate,
    onPicked: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val todayMillis = today.toEpochDay() * MILLIS_PER_DAY

    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initial.toEpochDay() * MILLIS_PER_DAY,
        selectableDates = object : SelectableDates {
            // You can log a catch-up you forgot, not one you haven't had.
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMillis
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = androidx.compose.material3.DatePickerDefaults.colors(containerColor = Surface100),
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let {
                        onPicked(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                }
            ) {
                Text(text = "Use this date", style = MaterialTheme.typography.labelLarge, color = Accent)
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

private const val MILLIS_PER_DAY = 86_400_000L
