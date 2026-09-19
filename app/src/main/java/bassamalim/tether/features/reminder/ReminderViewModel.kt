package bassamalim.tether.features.reminder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import bassamalim.tether.core.enums.InteractionType
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.nav.Screen
import bassamalim.tether.core.utils.initials
import bassamalim.tether.core.utils.reminderDateLabel
import bassamalim.tether.core.utils.timeLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val domain: ReminderDomain,
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId = savedStateHandle.toRoute<Screen.Reminder>().personId

    private val _uiState = MutableStateFlow(
        // Tomorrow evening: near enough to still mean it, far enough not to be now.
        ReminderUiState(today = domain.today()).withDate(domain.today().plusDays(1))
    )
    val uiState: StateFlow<ReminderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val person = domain.observePerson(personId).first() ?: return@launch

            _uiState.update {
                it.copy(
                    personName = person.person.name,
                    initials = initials(person.person.name),
                    canNotify = domain.canNotify()
                )
            }
        }

        viewModelScope.launch {
            val existing = domain.current(personId) ?: return@launch

            _uiState.update {
                it.copy(type = existing.type, isEditing = true)
                    .withDate(existing.scheduledFor.toLocalDate())
                    .withTime(existing.scheduledFor.toLocalTime())
            }
        }
    }

    fun onTypeSelect(value: InteractionType) = _uiState.update {
        it.copy(type = if (it.type == value) null else value)
    }

    fun onTomorrowSelect() = _uiState.update { it.withDate(it.today.plusDays(1)) }

    fun onNextWeekSelect() = _uiState.update { it.withDate(it.today.plusWeeks(1)) }

    fun onPickDate() = _uiState.update { it.copy(isPickingDate = true) }

    fun onDatePicked(date: LocalDate) = _uiState.update {
        it.copy(isPickingDate = false).withDate(date)
    }

    fun onDatePickerDismiss() = _uiState.update { it.copy(isPickingDate = false) }

    fun onPickTime() = _uiState.update { it.copy(isPickingTime = true) }

    fun onTimePicked(time: LocalTime) = _uiState.update {
        it.copy(isPickingTime = false).withTime(time)
    }

    fun onTimePickerDismiss() = _uiState.update { it.copy(isPickingTime = false) }

    fun onCancel() = navigator.popBackStack()

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            domain.set(
                personId = personId,
                type = state.type,
                at = state.date.atTime(state.time)
            )

            navigator.popBackStack()
        }
    }

    /** Clearing the bell: the alarm goes with the row. */
    fun onRemove() {
        if (!_uiState.value.isEditing) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            domain.clear(personId)
            navigator.popBackStack()
        }
    }

    private fun ReminderUiState.withDate(value: LocalDate) = copy(
        date = value,
        dateLabel = reminderDateLabel(value, today)
    ).revalidated()

    private fun ReminderUiState.withTime(value: LocalTime) = copy(
        time = value,
        timeLabel = timeLabel(value)
    ).revalidated()

    /** "Tomorrow at seven" stops being in the future at seven, even with the screen open. */
    private fun ReminderUiState.revalidated() =
        copy(isInFuture = date.atTime(time).isAfter(domain.now()))
}
