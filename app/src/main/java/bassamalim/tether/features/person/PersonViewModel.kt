package bassamalim.tether.features.person

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import bassamalim.tether.core.data.dataSources.room.entities.PersonDetail
import bassamalim.tether.core.data.dataSources.room.entities.Reminder
import bassamalim.tether.core.data.dataSources.room.relations.ConnectedPerson
import bassamalim.tether.core.domain.DueState
import bassamalim.tether.core.enums.CadencePreset
import bassamalim.tether.core.models.TrackedPerson
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.nav.Screen
import bassamalim.tether.core.utils.agoLabel
import bassamalim.tether.core.utils.cadenceLabel
import bassamalim.tether.core.utils.initials
import bassamalim.tether.core.utils.parsePlace
import bassamalim.tether.core.utils.lastTalkedStatus
import bassamalim.tether.core.utils.reminderDateLabel
import bassamalim.tether.core.utils.timeLabel
import bassamalim.tether.core.utils.workLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonViewModel @Inject constructor(
    private val domain: PersonDomain,
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId = savedStateHandle.toRoute<Screen.Person>().id

    /** Menu and dialog state is the screen's, not the database's. */
    private val localState = MutableStateFlow(LocalState())

    /**
     * The vocabulary and the screen's own state, paired: combine types only five flows, and the
     * person's four are the ones that have to stay named.
     */
    private val screenState = combine(
        domain.observeRelationshipOptions(),
        domain.observeReminder(personId),
        localState,
        ::ScreenState
    )

    private val _events = Channel<PersonEvent>()
    val events = _events.receiveAsFlow()

    init {
        // The delete can be tapped here or on the log sheet, which closes over this screen; the
        // bar is this screen's either way, so it listens for the deletion rather than for a tap.
        viewModelScope.launch {
            domain.observeDeletions(personId).collect { interaction ->
                _events.send(PersonEvent.HistoryDeleted(interaction))
            }
        }
    }

    val uiState: StateFlow<PersonUiState> = combine(
        domain.observePerson(personId),
        domain.observeDetails(personId),
        domain.observeHistory(personId),
        domain.observeConnections(personId),
        screenState
    ) { person, details, history, connections, screen ->
        person?.toUiState(
            details = details,
            history = history,
            connections = connections,
            reminder = screen.reminder,
            options = screen.options,
            local = screen.local
        )
            ?: PersonUiState(isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PersonUiState()
    )

    fun onBack() = navigator.popBackStack()

    fun onLogCatchUp() = navigator.navigate(Screen.LogInteraction(personId))

    /** The bell: set a reminder about them, or open the one already set. */
    fun onReminderClick() = navigator.navigate(Screen.Reminder(personId))

    /** History is a record you can correct: tapping an entry reopens the sheet on it. */
    fun onHistoryClick(interactionId: Long) {
        onHistoryMenuDismiss()

        navigator.navigate(Screen.LogInteraction(personId, interactionId))
    }

    fun onHistoryMenuOpen(interactionId: Long) =
        localState.update { it.copy(openHistoryMenuId = interactionId) }

    fun onHistoryMenuDismiss() = localState.update { it.copy(openHistoryMenuId = null) }

    /**
     * Delete goes through immediately rather than behind a dialog, because the undo bar is the
     * better confirmation: it asks nothing of you when you meant it.
     */
    fun onHistoryDelete(interactionId: Long) {
        onHistoryMenuDismiss()

        viewModelScope.launch {
            val interaction = domain.getInteraction(interactionId) ?: return@launch

            domain.deleteInteraction(interaction)
        }
    }

    /** The row comes back as it was, id and all, so the timeline closes over the gap. */
    fun onUndoHistoryDelete(interaction: Interaction) {
        viewModelScope.launch { domain.restoreInteraction(interaction) }
    }

    fun onNameClick() = localState.update {
        it.copy(isMenuOpen = false, isEditingName = true, nameDraft = uiState.value.name)
    }

    fun onNameDismiss() = localState.update { it.copy(isEditingName = false, nameDraft = "") }

    fun onNameChange(value: String) = localState.update { it.copy(nameDraft = value) }

    /** A typo in a name, or a name that's changed; everything else about them stays put. */
    fun onNameSave() {
        val name = localState.value.nameDraft
        if (name.isBlank()) return

        onNameDismiss()

        viewModelScope.launch { domain.setName(personId, name) }
    }

    fun onTagClick() = localState.update {
        it.copy(isPickingTag = true, tagDraft = uiState.value.tag)
    }

    fun onTagDismiss() = localState.update { it.copy(isPickingTag = false, tagDraft = "") }

    fun onTagChange(value: String) = localState.update { it.copy(tagDraft = value) }

    /** Tapping the one already chosen clears it, the way every other chip in the app does. */
    fun onTagOptionClick(option: String) = localState.update {
        it.copy(tagDraft = if (it.tagDraft.equals(option, ignoreCase = true)) "" else option)
    }

    /** Relationships change; the row that shows them should too. */
    fun onTagSave() {
        val tag = localState.value.tagDraft

        onTagDismiss()

        viewModelScope.launch { domain.setTag(personId, tag) }
    }

    fun onWorkClick() = localState.update {
        it.copy(
            isEditingWork = true,
            workplaceDraft = uiState.value.workplace,
            jobTitleDraft = uiState.value.jobTitle
        )
    }

    fun onWorkDismiss() = localState.update {
        it.copy(isEditingWork = false, workplaceDraft = "", jobTitleDraft = "")
    }

    fun onWorkplaceChange(value: String) = localState.update { it.copy(workplaceDraft = value) }

    fun onJobTitleChange(value: String) = localState.update { it.copy(jobTitleDraft = value) }

    /** Emptying both is how you say they've left; the line disappears rather than reading blank. */
    fun onWorkSave() {
        val workplace = localState.value.workplaceDraft
        val jobTitle = localState.value.jobTitleDraft

        onWorkDismiss()

        viewModelScope.launch { domain.setWork(personId, workplace, jobTitle) }
    }

    fun onCadenceClick() = localState.update { it.copy(isPickingCadence = true) }

    fun onCadenceDismiss() = localState.update { it.copy(isPickingCadence = false) }

    /**
     * Changing the cadence re-dates when they come due, counted from the last catch-up as
     * always, so tightening it can make someone overdue immediately.
     */
    fun onCadenceSelect(preset: CadencePreset) {
        localState.update { it.copy(isPickingCadence = false) }

        viewModelScope.launch { domain.setCadence(personId, preset.days) }
    }

    fun onAddConnection() = navigator.navigate(Screen.Connect(personId))

    /** A connection is a door: tapping it opens the person on the other side. */
    fun onConnectionClick(id: Long) = navigator.navigate(Screen.Person(id))

    fun onConnectionEdit(entry: ConnectionEntry) = localState.update {
        it.copy(editingConnectionId = entry.connectionId, connectionLabel = entry.label)
    }

    fun onConnectionLabelChange(value: String) =
        localState.update { it.copy(connectionLabel = value) }

    /** Tapping the suggestion you already chose clears it, the way the tag chips do. */
    fun onConnectionSuggestionClick(suggestion: String) = localState.update {
        it.copy(connectionLabel = if (it.connectionLabel == suggestion) "" else suggestion)
    }

    fun onConnectionEditDismiss() =
        localState.update { it.copy(editingConnectionId = null, connectionLabel = "") }

    fun onConnectionLabelSave() {
        val connectionId = localState.value.editingConnectionId ?: return
        val label = localState.value.connectionLabel

        onConnectionEditDismiss()

        viewModelScope.launch { domain.setConnectionLabel(connectionId, label) }
    }

    /** Removing the link doesn't touch either person; it only forgets that they know each other. */
    fun onDisconnect() {
        val connectionId = localState.value.editingConnectionId ?: return

        onConnectionEditDismiss()

        viewModelScope.launch { domain.disconnect(connectionId) }
    }

    fun onMenuOpen() = localState.update { it.copy(isMenuOpen = true) }

    fun onMenuDismiss() = localState.update { it.copy(isMenuOpen = false) }

    fun onDeleteClick() = localState.update { it.copy(isMenuOpen = false, isConfirmingDelete = true) }

    fun onDeleteDismiss() = localState.update { it.copy(isConfirmingDelete = false) }

    fun onDeleteConfirm() {
        viewModelScope.launch {
            domain.delete(personId)
            navigator.popBackStack()
        }
    }

    private data class ScreenState(
        val options: List<String>,
        val reminder: Reminder?,
        val local: LocalState
    )

    private data class LocalState(
        val openHistoryMenuId: Long? = null,
        val tagDraft: String = "",
        val nameDraft: String = "",
        val isEditingName: Boolean = false,
        val editingConnectionId: Long? = null,
        val connectionLabel: String = "",
        val isPickingTag: Boolean = false,
        val isEditingWork: Boolean = false,
        val workplaceDraft: String = "",
        val jobTitleDraft: String = "",
        val isPickingCadence: Boolean = false,
        val isMenuOpen: Boolean = false,
        val isConfirmingDelete: Boolean = false
    )

    private fun TrackedPerson.toUiState(
        details: List<PersonDetail>,
        history: List<Interaction>,
        connections: List<ConnectedPerson>,
        reminder: Reminder?,
        options: List<String>,
        local: LocalState
    ): PersonUiState {
        val today = domain.today()
        val connectionEntries = connections.map { connection ->
            ConnectionEntry(
                connectionId = connection.connectionId,
                personId = connection.person.id,
                name = connection.person.name,
                initials = initials(connection.person.name),
                label = connection.label,
                subtitle = connection.label.ifBlank { "Connected" }
            )
        }

        return PersonUiState(
            isLoading = false,
            id = person.id,
            name = person.name,
            initials = initials(person.name),
            tag = person.tag.orEmpty(),
            tagLabel = person.tag?.uppercase(),
            relationshipOptions = options,
            tagDraft = local.tagDraft,
            cadence = CadencePreset.of(person.cadenceDays),
            cadenceLabel = cadenceLabel(person.cadenceDays),
            status = lastTalkedStatus(
                lastInteractionOn = lastInteractionOn,
                daysOverdue = (dueState as? DueState.Slipping)?.daysOverdue,
                today = today
            ),
            isOverdue = dueState is DueState.Slipping,
            phone = person.phone,
            workplace = person.workplace.orEmpty(),
            jobTitle = person.jobTitle.orEmpty(),
            workLabel = workLabel(person.workplace, person.jobTitle),
            workplaceDraft = local.workplaceDraft,
            jobTitleDraft = local.jobTitleDraft,
            reminderLabel = reminder?.let {
                // What and when, in one line: "Coffee · Tomorrow, 19:00".
                listOfNotNull(
                    it.type?.label,
                    "${reminderDateLabel(it.scheduledFor.toLocalDate(), today)}, " +
                            timeLabel(it.scheduledFor.toLocalTime())
                ).joinToString(" · ")
            },
            details = details.map { DetailRow(id = it.id, label = it.label, value = it.value) },
            connections = connectionEntries,
            editingConnection = connectionEntries
                .firstOrNull { it.connectionId == local.editingConnectionId }
                ?.let { entry ->
                    // The draft is the local one, so typing isn't overwritten by the flow.
                    ConnectionEdit(
                        connectionId = entry.connectionId,
                        name = entry.name,
                        label = local.connectionLabel
                    )
                },
            history = history.map { interaction ->
                val place = parsePlace(interaction.location)
                HistoryEntry(
                    id = interaction.id,
                    isMenuOpen = interaction.id == local.openHistoryMenuId,
                    title = interaction.type?.label ?: "Caught up",
                    whoLabel = interaction.initiatedBy?.historyLabel,
                    placeLabel = place?.label,
                    placeUrl = place?.url,
                    timeLabel = agoLabel(interaction.occurredOn, today),
                    note = interaction.note.takeIf { it.isNotBlank() }
                )
            },
            isPickingTag = local.isPickingTag,
            isEditingName = local.isEditingName,
            nameDraft = local.nameDraft,
            isPickingCadence = local.isPickingCadence,
            isEditingWork = local.isEditingWork,
            isMenuOpen = local.isMenuOpen,
            isConfirmingDelete = local.isConfirmingDelete
        )
    }
}
