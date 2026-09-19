package bassamalim.tether.features.person

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import bassamalim.tether.core.data.dataSources.room.entities.PersonDetail
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

    val uiState: StateFlow<PersonUiState> = combine(
        domain.observePerson(personId),
        domain.observeDetails(personId),
        domain.observeHistory(personId),
        localState
    ) { person, details, history, local ->
        person?.toUiState(details, history, local) ?: PersonUiState(isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PersonUiState()
    )

    fun onBack() = navigator.popBackStack()

    fun onLogCatchUp() = navigator.navigate(Screen.LogInteraction(personId))

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
        val isPickingTag: Boolean = false,
        val isPickingCadence: Boolean = false,
        val isMenuOpen: Boolean = false,
        val isConfirmingDelete: Boolean = false
    )

    private fun TrackedPerson.toUiState(
        details: List<PersonDetail>,
        history: List<Interaction>,
        local: LocalState
    ): PersonUiState {
        val today = domain.today()

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
            history = history.map { interaction ->
                HistoryEntry(
                    id = interaction.id,
                    title = interaction.type?.label ?: "Caught up",
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
