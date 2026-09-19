package bassamalim.tether.features.person

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import bassamalim.tether.core.data.dataSources.room.entities.PersonDetail
import bassamalim.tether.core.data.dataSources.room.relations.ConnectedPerson
import bassamalim.tether.core.domain.DueState
import bassamalim.tether.core.enums.CadencePreset
import bassamalim.tether.core.enums.RelationshipTag
import bassamalim.tether.core.models.TrackedPerson
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.nav.Screen
import bassamalim.tether.core.utils.agoLabel
import bassamalim.tether.core.utils.cadenceLabel
import bassamalim.tether.core.utils.initials
import bassamalim.tether.core.utils.lastTalkedStatus
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
        localState
    ) { person, details, history, connections, local ->
        person?.toUiState(details, history, connections, local) ?: PersonUiState(isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PersonUiState()
    )

    fun onBack() = navigator.popBackStack()

    fun onLogCatchUp() = navigator.navigate(Screen.LogInteraction(personId))

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

    fun onTagClick() = localState.update { it.copy(isPickingTag = true) }

    fun onTagDismiss() = localState.update { it.copy(isPickingTag = false) }

    /** Relationships change; the row that shows them should too. */
    fun onTagSelect(tag: RelationshipTag?) {
        localState.update { it.copy(isPickingTag = false) }

        viewModelScope.launch { domain.setTag(personId, tag) }
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

    private data class LocalState(
        val openHistoryMenuId: Long? = null,
        val editingConnectionId: Long? = null,
        val connectionLabel: String = "",
        val isPickingTag: Boolean = false,
        val isPickingCadence: Boolean = false,
        val isMenuOpen: Boolean = false,
        val isConfirmingDelete: Boolean = false
    )

    private fun TrackedPerson.toUiState(
        details: List<PersonDetail>,
        history: List<Interaction>,
        connections: List<ConnectedPerson>,
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
            tag = person.tag,
            tagLabel = person.tag?.chipLabel,
            cadence = CadencePreset.of(person.cadenceDays),
            cadenceLabel = cadenceLabel(person.cadenceDays),
            status = lastTalkedStatus(
                lastInteractionOn = lastInteractionOn,
                daysOverdue = (dueState as? DueState.Slipping)?.daysOverdue,
                today = today
            ),
            isOverdue = dueState is DueState.Slipping,
            phone = person.phone,
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
                HistoryEntry(
                    id = interaction.id,
                    isMenuOpen = interaction.id == local.openHistoryMenuId,
                    title = interaction.type?.label ?: "Caught up",
                    meta = listOfNotNull(
                        interaction.initiatedBy?.historyLabel,
                        interaction.location.takeIf { it.isNotBlank() }
                    ).joinToString(" · ").takeIf { it.isNotEmpty() },
                    timeLabel = agoLabel(interaction.occurredOn, today),
                    note = interaction.note.takeIf { it.isNotBlank() }
                )
            },
            isPickingTag = local.isPickingTag,
            isPickingCadence = local.isPickingCadence,
            isMenuOpen = local.isMenuOpen,
            isConfirmingDelete = local.isConfirmingDelete
        )
    }
}
