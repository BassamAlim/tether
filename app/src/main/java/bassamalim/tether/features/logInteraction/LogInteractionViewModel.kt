package bassamalim.tether.features.logInteraction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import bassamalim.tether.core.enums.Initiator
import bassamalim.tether.core.enums.InteractionType
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.nav.Screen
import bassamalim.tether.core.utils.initials
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class LogInteractionViewModel @Inject constructor(
    private val domain: LogInteractionDomain,
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.LogInteraction>()
    private val personId = route.personId

    /** The row being corrected, once it's loaded; null while logging something new. */
    private var editing: Interaction? = null

    private val _uiState = MutableStateFlow(
        // Today is the only default worth preselecting; guessing how you spoke would write
        // worse data than leaving it blank.
        LogInteractionUiState(occurredOn = domain.today(), today = domain.today())
    )
    val uiState: StateFlow<LogInteractionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val person = domain.observePerson(personId).first() ?: return@launch

            _uiState.update {
                it.copy(
                    personName = person.person.name,
                    initials = initials(person.person.name)
                )
            }
        }

        // Correcting one: the sheet opens on what was written, so editing is a change to it
        // rather than a retyping of it.
        if (route.interactionId != 0L) {
            viewModelScope.launch {
                val interaction = domain.getInteraction(route.interactionId) ?: return@launch
                editing = interaction

                _uiState.update {
                    it.copy(
                        type = interaction.type,
                        occurredOn = interaction.occurredOn,
                        location = interaction.location,
                        initiatedBy = interaction.initiatedBy,
                        note = interaction.note,
                        isEditing = true
                    )
                }
            }
        }
    }

    fun onTypeSelect(value: InteractionType) = _uiState.update {
        it.copy(type = if (it.type == value) null else value)
    }

    fun onTodaySelect() = _uiState.update { it.copy(occurredOn = it.today) }

    fun onYesterdaySelect() = _uiState.update { it.copy(occurredOn = it.today.minusDays(1)) }

    fun onPickDate() = _uiState.update { it.copy(isPickingDate = true) }

    fun onDatePicked(date: LocalDate) = _uiState.update {
        it.copy(occurredOn = date, isPickingDate = false)
    }

    fun onDatePickerDismiss() = _uiState.update { it.copy(isPickingDate = false) }

    fun onInitiatorSelect(value: Initiator) = _uiState.update {
        it.copy(initiatedBy = if (it.initiatedBy == value) null else value)
    }

    fun onLocationChange(value: String) = _uiState.update { it.copy(location = value) }

    fun onNoteChange(value: String) = _uiState.update { it.copy(note = value) }

    fun onDismiss() = navigator.popBackStack()

    /** Only ever offered for a catch-up that's already in the history. */
    fun onDelete() {
        val edited = editing ?: return
        if (!_uiState.value.canSave) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            domain.delete(edited)
            navigator.popBackStack()
        }
    }

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val edited = editing

            if (edited == null) {
                domain.log(
                    personId = personId,
                    type = state.type,
                    occurredOn = state.occurredOn,
                    location = state.location,
                    initiatedBy = state.initiatedBy,
                    note = state.note
                )
            }
            else {
                domain.update(
                    interaction = edited,
                    type = state.type,
                    occurredOn = state.occurredOn,
                    location = state.location,
                    initiatedBy = state.initiatedBy,
                    note = state.note
                )
            }

            navigator.popBackStack()
        }
    }
}
