package bassamalim.tether.features.logInteraction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
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

    private val personId = savedStateHandle.toRoute<Screen.LogInteraction>().personId

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

    fun onNoteChange(value: String) = _uiState.update { it.copy(note = value) }

    fun onDismiss() = navigator.popBackStack()

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            domain.log(
                personId = personId,
                type = state.type,
                occurredOn = state.occurredOn,
                note = state.note
            )

            navigator.popBackStack()
        }
    }
}
