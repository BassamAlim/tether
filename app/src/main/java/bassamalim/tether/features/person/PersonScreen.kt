package bassamalim.tether.features.person

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.tether.core.enums.CadencePreset
import bassamalim.tether.core.ui.components.Avatar
import bassamalim.tether.core.ui.components.RelationshipField
import bassamalim.tether.core.ui.components.FilterPill
import bassamalim.tether.core.ui.components.LabeledTextField
import bassamalim.tether.core.ui.components.SectionLabel
import bassamalim.tether.core.ui.components.UndoSnackbar
import bassamalim.tether.core.utils.internationalDigits
import bassamalim.tether.core.ui.theme.Accent
import bassamalim.tether.core.ui.theme.AccentInk
import bassamalim.tether.core.ui.theme.Action
import bassamalim.tether.core.ui.theme.Attention
import bassamalim.tether.core.ui.theme.Danger
import bassamalim.tether.core.ui.theme.Ink
import bassamalim.tether.core.ui.theme.InkFaint
import bassamalim.tether.core.ui.theme.InkMuted
import bassamalim.tether.core.ui.theme.Pill
import bassamalim.tether.core.ui.theme.Sizes
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface0
import bassamalim.tether.core.ui.theme.Surface100
import bassamalim.tether.core.ui.theme.Surface200
import bassamalim.tether.core.ui.theme.Surface300
import bassamalim.tether.core.ui.theme.TetherType
import bassamalim.tether.R
import androidx.compose.ui.res.painterResource

private const val UNDO = "UNDO"

@Composable
fun PersonScreen(viewModel: PersonViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PersonEvent.HistoryDeleted -> {
                    // Deleting two in a row shouldn't leave the first bar offering back a row
                    // that's already been offered.
                    snackbarHostState.currentSnackbarData?.dismiss()

                    val result = snackbarHostState.showSnackbar(
                        message = "Deleted that catch-up",
                        actionLabel = UNDO,
                        withDismissAction = false
                    )

                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onUndoHistoryDelete(event.interaction)
                    }
                }
            }
        }
    }

    PersonScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = viewModel::onBack,
        onNameClick = viewModel::onNameClick,
        onNameChange = viewModel::onNameChange,
        onNameDismiss = viewModel::onNameDismiss,
        onNameSave = viewModel::onNameSave,
        onTagClick = viewModel::onTagClick,
        onTagDismiss = viewModel::onTagDismiss,
        onTagChange = viewModel::onTagChange,
        onTagOptionClick = viewModel::onTagOptionClick,
        onTagSave = viewModel::onTagSave,
        onWorkClick = viewModel::onWorkClick,
        onWorkplaceChange = viewModel::onWorkplaceChange,
        onJobTitleChange = viewModel::onJobTitleChange,
        onWorkDismiss = viewModel::onWorkDismiss,
        onWorkSave = viewModel::onWorkSave,
        onCadenceClick = viewModel::onCadenceClick,
        onCadenceDismiss = viewModel::onCadenceDismiss,
        onCadenceSelect = viewModel::onCadenceSelect,
        onLogCatchUp = viewModel::onLogCatchUp,
        onReminderClick = viewModel::onReminderClick,
        onMessage = { state.phone?.let { context.openWhatsApp(it) } },
        onPlaceClick = { context.openLink(it) },
        onHistoryClick = viewModel::onHistoryClick,
        onHistoryMenuOpen = viewModel::onHistoryMenuOpen,
        onHistoryMenuDismiss = viewModel::onHistoryMenuDismiss,
        onHistoryDelete = viewModel::onHistoryDelete,
        onAddConnection = viewModel::onAddConnection,
        onConnectionClick = viewModel::onConnectionClick,
        onConnectionEdit = viewModel::onConnectionEdit,
        onConnectionLabelChange = viewModel::onConnectionLabelChange,
        onConnectionSuggestionClick = viewModel::onConnectionSuggestionClick,
        onConnectionEditDismiss = viewModel::onConnectionEditDismiss,
        onConnectionLabelSave = viewModel::onConnectionLabelSave,
        onDisconnect = viewModel::onDisconnect,
        onMenuOpen = viewModel::onMenuOpen,
        onMenuDismiss = viewModel::onMenuDismiss,
        onDeleteClick = viewModel::onDeleteClick,
        onDeleteDismiss = viewModel::onDeleteDismiss,
        onDeleteConfirm = viewModel::onDeleteConfirm
    )
}

/**
 * Opens the chat in WhatsApp. First choice hands WhatsApp the raw number so it matches the
 * contact itself; if that fails, wa.me needs the number in international form. Neither works
 * without WhatsApp installed, so SMS is the last resort rather than the default.
 */
private fun Context.openWhatsApp(phone: String) {
    val attempts = listOfNotNull(
        Intent(Intent.ACTION_SENDTO, "smsto:$phone".toUri()).setPackage(WHATSAPP),
        internationalDigits(phone)?.let { Intent(Intent.ACTION_VIEW, "https://wa.me/$it".toUri()) },
        Intent(Intent.ACTION_SENDTO, "smsto:$phone".toUri())
    )

    for (intent in attempts) {
        try {
            startActivity(intent)
            return
        } catch (_: ActivityNotFoundException) {
            // Try the next way in.
        }
    }
}

private const val WHATSAPP = "com.whatsapp"

/**
 * Opens a catch-up's pasted "Where". A Google Maps link is claimed by the Maps app when it's
 * installed and falls through to the browser when it isn't; with neither, there's nowhere to go.
 */
private fun Context.openLink(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (_: ActivityNotFoundException) {
        // Nothing on the phone opens links.
    }
}

@Composable
private fun PersonScreen(
    state: PersonUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onNameClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onNameDismiss: () -> Unit,
    onNameSave: () -> Unit,
    onTagClick: () -> Unit,
    onTagDismiss: () -> Unit,
    onTagChange: (String) -> Unit,
    onTagOptionClick: (String) -> Unit,
    onTagSave: () -> Unit,
    onWorkClick: () -> Unit,
    onWorkplaceChange: (String) -> Unit,
    onJobTitleChange: (String) -> Unit,
    onWorkDismiss: () -> Unit,
    onWorkSave: () -> Unit,
    onCadenceClick: () -> Unit,
    onCadenceDismiss: () -> Unit,
    onCadenceSelect: (CadencePreset) -> Unit,
    onLogCatchUp: () -> Unit,
    onReminderClick: () -> Unit,
    onMessage: () -> Unit,
    onPlaceClick: (String) -> Unit,
    onHistoryClick: (Long) -> Unit,
    onHistoryMenuOpen: (Long) -> Unit,
    onHistoryMenuDismiss: () -> Unit,
    onHistoryDelete: (Long) -> Unit,
    onAddConnection: () -> Unit,
    onConnectionClick: (Long) -> Unit,
    onConnectionEdit: (ConnectionEntry) -> Unit,
    onConnectionLabelChange: (String) -> Unit,
    onConnectionSuggestionClick: (String) -> Unit,
    onConnectionEditDismiss: () -> Unit,
    onConnectionLabelSave: () -> Unit,
    onDisconnect: () -> Unit,
    onMenuOpen: () -> Unit,
    onMenuDismiss: () -> Unit,
    onDeleteClick: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onDeleteConfirm: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Surface0)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = Spacing.xxl)
        ) {
            item {
                TopBar(
                    isMenuOpen = state.isMenuOpen,
                    onBack = onBack,
                    onMenuOpen = onMenuOpen,
                    onMenuDismiss = onMenuDismiss,
                    onEditNameClick = onNameClick,
                    onDeleteClick = onDeleteClick
                )
            }

            item {
                Identity(
                    state = state,
                    onNameClick = onNameClick,
                    onTagClick = onTagClick,
                    onWorkClick = onWorkClick,
                    onCadenceClick = onCadenceClick
                )
            }

            item {
                Actions(
                    canMessage = state.canMessage,
                    hasReminder = state.reminderLabel != null,
                    onLogCatchUp = onLogCatchUp,
                    onReminderClick = onReminderClick,
                    onMessage = onMessage
                )
            }

            state.reminderLabel?.let { label ->
                item { Header("Reminder") }

                item { ReminderRow(label = label, onClick = onReminderClick) }
            }

            if (state.details.isNotEmpty()) {
                item { Header("Details") }

                item { DetailsCard(details = state.details) }
            }

            item { Header("Connections") }

            items(state.connections, key = { it.connectionId }) { connection ->
                ConnectionRow(
                    connection = connection,
                    onClick = { onConnectionClick(connection.personId) },
                    onEdit = { onConnectionEdit(connection) }
                )
            }

            item { AddConnectionRow(onClick = onAddConnection) }

            item { Header("History") }

            if (state.history.isEmpty()) {
                item {
                    Text(
                        text = "Nothing logged yet. The first catch-up you log starts the record.",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkFaint,
                        modifier = Modifier.padding(horizontal = Spacing.screen)
                    )
                }
            }

            itemsIndexed(state.history, key = { _, entry -> entry.id }) { index, entry ->
                HistoryRow(
                    entry = entry,
                    isFirst = index == 0,
                    isLast = index == state.history.lastIndex,
                    onClick = { onHistoryClick(entry.id) },
                    onPlaceClick = onPlaceClick,
                    onMenuOpen = { onHistoryMenuOpen(entry.id) },
                    onMenuDismiss = onHistoryMenuDismiss,
                    onDelete = { onHistoryDelete(entry.id) }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data ->
            UndoSnackbar(data, actionLabel = UNDO)
        }
    }

    if (state.isEditingName) {
        NameDialog(
            value = state.nameDraft,
            canSave = state.canSaveName,
            onValueChange = onNameChange,
            onDismiss = onNameDismiss,
            onSave = onNameSave
        )
    }

    if (state.isPickingTag) {
        RelationshipDialog(
            value = state.tagDraft,
            options = state.relationshipOptions,
            onValueChange = onTagChange,
            onOptionClick = onTagOptionClick,
            onDismiss = onTagDismiss,
            onSave = onTagSave
        )
    }

    if (state.isEditingWork) {
        WorkDialog(
            workplace = state.workplaceDraft,
            jobTitle = state.jobTitleDraft,
            onWorkplaceChange = onWorkplaceChange,
            onJobTitleChange = onJobTitleChange,
            onDismiss = onWorkDismiss,
            onSave = onWorkSave
        )
    }

    if (state.isPickingCadence) {
        CadenceDialog(
            selected = state.cadence,
            onDismiss = onCadenceDismiss,
            onSelect = onCadenceSelect
        )
    }

    state.editingConnection?.let { editing ->
        ConnectionDialog(
            editing = editing,
            options = state.relationshipOptions,
            onLabelChange = onConnectionLabelChange,
            onSuggestionClick = onConnectionSuggestionClick,
            onDismiss = onConnectionEditDismiss,
            onSave = onConnectionLabelSave,
            onRemove = onDisconnect
        )
    }

    if (state.isConfirmingDelete) {
        DeleteDialog(
            name = state.name,
            onDismiss = onDeleteDismiss,
            onConfirm = onDeleteConfirm
        )
    }
}

@Composable
private fun TopBar(
    isMenuOpen: Boolean,
    onBack: () -> Unit,
    onMenuOpen: () -> Unit,
    onMenuDismiss: () -> Unit,
    onEditNameClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.screen, start = Spacing.md, end = Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CircleIconButton(onClick = onBack) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = "Back to people",
                tint = Ink,
                modifier = Modifier.size(22.dp)
            )
        }

        Box {
            CircleIconButton(onClick = onMenuOpen) {
                Icon(
                    painter = painterResource(R.drawable.ic_more),
                    contentDescription = "More options",
                    tint = Ink,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = isMenuOpen,
                onDismissRequest = onMenuDismiss,
                containerColor = Surface200
            ) {
                // Tapping the name does the same; the menu is where it's written down.
                DropdownMenuItem(
                    text = {
                        Text(text = "Edit name", style = MaterialTheme.typography.bodyMedium)
                    },
                    onClick = onEditNameClick
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Delete person",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Danger
                        )
                    },
                    onClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
private fun Identity(
    state: PersonUiState,
    onNameClick: () -> Unit,
    onTagClick: () -> Unit,
    onWorkClick: () -> Unit,
    onCadenceClick: () -> Unit
) {
    Column(Modifier.padding(top = Spacing.sm, start = Spacing.screen, end = Spacing.screen)) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(color = Surface200, shape = Pill),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.initials,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight(700),
                color = InkMuted
            )
        }

        Text(
            text = state.name,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(top = Spacing.lg)
                .clickable(onClickLabel = "Edit name", onClick = onNameClick)
        )

        Row(
            modifier = Modifier.padding(top = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            // Both are badges because both are editable; a bare line of text reads as a fact
            // about the person rather than something you can change.
            HeaderChip(
                text = state.tagLabel ?: "SET RELATIONSHIP",
                style = MaterialTheme.typography.labelSmall,
                color = if (state.tagLabel != null) InkMuted else InkFaint,
                onClick = onTagClick
            )

            HeaderChip(
                text = state.cadenceLabel,
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
                onClick = onCadenceClick
            )
        }

        // Where they work sits with the name rather than in Details: it's how you place
        // someone, and it's asked for by name on the way in.
        Text(
            text = state.workLabel ?: "Add where they work",
            style = MaterialTheme.typography.bodySmall,
            color = if (state.workLabel != null) InkMuted else InkFaint,
            modifier = Modifier
                .padding(top = 10.dp)
                .clickable(onClickLabel = "Edit where they work", onClick = onWorkClick)
        )

        Text(
            text = state.status,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight(600),
            color = if (state.isOverdue) Attention else InkMuted,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
private fun HeaderChip(
    text: String,
    style: TextStyle,
    color: Color,
    onClick: () -> Unit
) {
    Text(
        text = text,
        style = style,
        color = color,
        modifier = Modifier
            .clip(Pill)
            .background(color = Surface200)
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = Spacing.xs)
    )
}

@Composable
private fun Actions(
    canMessage: Boolean,
    hasReminder: Boolean,
    onLogCatchUp: () -> Unit,
    onReminderClick: () -> Unit,
    onMessage: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(top = Spacing.screen, start = Spacing.screen, end = Spacing.screen)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(color = Action)
                .clickable(onClick = onLogCatchUp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.CenterHorizontally)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = AccentInk,
                modifier = Modifier.size(18.dp)
            )

            Text(
                text = "Log a catch-up",
                style = MaterialTheme.typography.labelLarge,
                color = AccentInk
            )
        }

        SquareIconButton(enabled = true, onClick = onReminderClick) {
            Icon(
                painter = painterResource(R.drawable.ic_bell),
                // Always full Ink, like the message button when it's live: the muted inks are
                // this screen's disabled tint, so anything dimmer reads as a dead button. What's
                // set is said by the Reminder row below, not by dimming the way in.
                contentDescription = if (hasReminder) "Edit the reminder" else "Set a reminder",
                tint = Ink,
                modifier = Modifier.size(19.dp)
            )
        }

        SquareIconButton(enabled = canMessage, onClick = onMessage) {
            Icon(
                painter = painterResource(R.drawable.ic_message),
                contentDescription = "Message on WhatsApp",
                tint = if (canMessage) Ink else InkFaint,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@Composable
private fun DetailsCard(details: List<DetailRow>) {
    Column(
        Modifier
            .padding(horizontal = Spacing.screen)
            .fillMaxWidth()
            .background(color = Surface100, shape = MaterialTheme.shapes.medium)
            .padding(horizontal = Spacing.lg, vertical = Spacing.xxs)
    ) {
        details.forEachIndexed { index, detail ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                Text(
                    text = detail.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkFaint
                )

                Text(
                    text = detail.value,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight(600),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }

            if (index != details.lastIndex) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(Sizes.border)
                        .background(Surface300)
                )
            }
        }
    }
}

/**
 * Someone this person knows. Tapping the row walks the link; the label is edited from the
 * button, so a tap never means two things at once.
 */
@Composable
private fun ConnectionRow(
    connection: ConnectionEntry,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = Spacing.md, vertical = Spacing.xxs)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(start = Spacing.sm, top = Spacing.sm, bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Avatar(initials = connection.initials)

        Column(Modifier.weight(1f)) {
            Text(text = connection.name, style = MaterialTheme.typography.titleMedium)

            Text(
                text = connection.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = InkFaint,
                modifier = Modifier.padding(top = 3.dp)
            )
        }

        CircleIconButton(onClick = onEdit) {
            Icon(
                painter = painterResource(R.drawable.ic_more),
                contentDescription = "Edit connection with ${connection.name}",
                tint = InkFaint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AddConnectionRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = Spacing.md, vertical = Spacing.xxs)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(start = Spacing.sm, top = Spacing.sm, bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Box(
            modifier = Modifier
                .size(Sizes.avatar)
                .background(color = Surface100, shape = Pill),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                tint = InkFaint,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = "Add a connection",
            style = MaterialTheme.typography.bodyMedium,
            color = InkMuted
        )
    }
}

@Composable
private fun ConnectionDialog(
    editing: ConnectionEdit,
    options: List<String>,
    onLabelChange: (String) -> Unit,
    onSuggestionClick: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onRemove: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface100,
        title = { Text(text = editing.name, style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                RelationshipField(
                    label = "How they know each other",
                    value = editing.label,
                    options = options,
                    onValueChange = onLabelChange,
                    onOptionClick = onSuggestionClick
                )

                Text(
                    // Removing the link forgets nothing else: both people stay, with their
                    // details and their history.
                    text = "Removing only forgets that they know each other.",
                    style = TetherType.Caption,
                    color = InkFaint,
                    modifier = Modifier.padding(top = Spacing.md)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(text = "Save", style = MaterialTheme.typography.labelLarge, color = Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onRemove) {
                Text(text = "Remove", style = MaterialTheme.typography.labelLarge, color = Danger)
            }
        }
    )
}

/**
 * A place you can open: full Ink over the line's faint ink, and underlined, since colour alone
 * shouldn't be the only thing saying "tap me" — and lime is spent on the screen's action.
 */
private val PlaceLinkStyles = TextLinkStyles(
    style = SpanStyle(color = Ink, textDecoration = TextDecoration.Underline)
)

/** A history entry's first line: the menu's touch target, which everything else centres on. */
private val HistoryHeaderHeight = 28.dp

@Composable
private fun HistoryRow(
    entry: HistoryEntry,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    onPlaceClick: (String) -> Unit,
    onMenuOpen: () -> Unit,
    onMenuDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // The row opens the entry for correcting; everything you can do to it is also on
            // the menu, so nothing about this row depends on knowing a gesture.
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.screen)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Column(
            modifier = Modifier
                .width(Spacing.sm)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // The dot sits on the header's centre line, level with the title, the date and
            // the menu; the rail runs into it from the entry above.
            Box(
                Modifier
                    .width(Sizes.border)
                    .height((HistoryHeaderHeight - Spacing.sm) / 2)
                    .background(if (isFirst) Color.Transparent else Surface300)
            )

            Box(
                Modifier
                    .size(Spacing.sm)
                    .background(color = if (isFirst) InkMuted else Surface300, shape = Pill)
            )

            if (!isLast) {
                Box(
                    Modifier
                        .width(Sizes.border)
                        .weight(1f)
                        .background(Surface300)
                )
            }
        }

        Column(
            Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else Spacing.lg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HistoryHeaderHeight),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight(700),
                    modifier = Modifier.weight(1f)
                )

                Text(text = entry.timeLabel, style = TetherType.Timestamp, color = InkFaint)

                HistoryMenu(
                    isOpen = entry.isMenuOpen,
                    onOpen = onMenuOpen,
                    onDismiss = onMenuDismiss,
                    onEdit = onClick,
                    onDelete = onDelete
                )
            }

            // Who and where sit on their own line, in the timestamp's voice: they're
            // circumstances of the catch-up, not part of what was said. A place that came in
            // as a Maps link reads as its name and opens the map; the rest of the row still
            // opens the entry for correcting.
            if (entry.whoLabel != null || entry.placeLabel != null) {
                val meta = buildAnnotatedString {
                    entry.whoLabel?.let { append(it) }
                    entry.placeLabel?.let { place ->
                        if (entry.whoLabel != null) append(" · ")
                        val url = entry.placeUrl
                        if (url == null) {
                            append(place)
                        } else {
                            withLink(
                                LinkAnnotation.Clickable(
                                    tag = url,
                                    styles = PlaceLinkStyles,
                                    linkInteractionListener = { onPlaceClick(url) }
                                )
                            ) { append(place) }
                        }
                    }
                }

                Text(
                    text = meta,
                    style = TetherType.Timestamp,
                    color = InkFaint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = Spacing.xxs)
                )
            }

            entry.note?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }
    }
}

/** The entry's own options: the same two things the row and the sheet offer, written down. */
@Composable
private fun HistoryMenu(
    isOpen: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box {
        Box(
            modifier = Modifier
                .size(HistoryHeaderHeight)
                .clip(Pill)
                .clickable(onClick = onOpen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_more),
                contentDescription = "Catch-up options",
                tint = InkFaint,
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = isOpen,
            onDismissRequest = onDismiss,
            containerColor = Surface200
        ) {
            DropdownMenuItem(
                text = { Text(text = "Edit", style = MaterialTheme.typography.bodyMedium) },
                onClick = onEdit
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Danger
                    )
                },
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun NameDialog(
    value: String,
    canSave: Boolean,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface100,
        title = { Text(text = "Name", style = MaterialTheme.typography.titleMedium) },
        text = {
            LabeledTextField(
                label = "What you call them",
                value = value,
                onValueChange = onValueChange
            )
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = canSave) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (canSave) Accent else InkFaint
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", style = MaterialTheme.typography.labelLarge, color = InkMuted)
            }
        }
    )
}

@Composable
private fun WorkDialog(
    workplace: String,
    jobTitle: String,
    onWorkplaceChange: (String) -> Unit,
    onJobTitleChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface100,
        title = { Text(text = "Work", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                LabeledTextField(
                    label = "Works at",
                    value = workplace,
                    onValueChange = onWorkplaceChange,
                    placeholder = "Company, school, hospital"
                )

                LabeledTextField(
                    label = "Role",
                    value = jobTitle,
                    onValueChange = onJobTitleChange,
                    placeholder = "What they do there",
                    modifier = Modifier.padding(top = Spacing.md)
                )

                Text(
                    // Leaving is a thing that happens; emptying both is how you record it.
                    text = "Leave them empty and the line goes away.",
                    style = TetherType.Caption,
                    color = InkFaint,
                    modifier = Modifier.padding(top = Spacing.md)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
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

@Composable
private fun RelationshipDialog(
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    onOptionClick: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface100,
        title = { Text(text = "Relationship", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                RelationshipField(
                    label = "How you know them",
                    value = value,
                    options = options,
                    onValueChange = onValueChange,
                    onOptionClick = onOptionClick
                )

                Text(
                    // Clearing the field is how you say "none"; saying so beats a None pill.
                    text = "Leave it empty and they carry no relationship at all.",
                    style = TetherType.Caption,
                    color = InkFaint,
                    modifier = Modifier.padding(top = Spacing.md)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
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

@Composable
private fun CadenceDialog(
    selected: CadencePreset,
    onDismiss: () -> Unit,
    onSelect: (CadencePreset) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface100,
        title = { Text(text = "Reach out every", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    CadencePreset.entries.forEach { preset ->
                        FilterPill(
                            label = preset.label,
                            selected = preset == selected,
                            onClick = { onSelect(preset) }
                        )
                    }
                }

                Text(
                    text = "Counted from your last catch-up, so a shorter cadence can make " +
                            "someone overdue straight away.",
                    style = TetherType.Caption,
                    color = InkFaint,
                    modifier = Modifier.padding(top = Spacing.md)
                )
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
private fun DeleteDialog(name: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface100,
        title = { Text(text = "Delete $name?", style = MaterialTheme.typography.titleMedium) },
        text = {
            Text(
                // With no server, there is nowhere to restore this from.
                text = "Their details and every catch-up you logged go with them. " +
                        "This can't be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkMuted
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Delete", style = MaterialTheme.typography.labelLarge, color = Danger)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Keep", style = MaterialTheme.typography.labelLarge, color = InkMuted)
            }
        }
    )
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
private fun CircleIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(Sizes.avatar)
            .clip(Pill)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = { content() }
    )
}

/** What the bell is set to, under its own header, the way Details and Connections read. */
@Composable
private fun ReminderRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = Spacing.screen)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(color = Surface100)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_bell),
            contentDescription = null,
            tint = InkMuted,
            modifier = Modifier.size(18.dp)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Ink,
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = InkFaint,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SquareIconButton(
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(color = Surface200)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
        content = { content() }
    )
}
