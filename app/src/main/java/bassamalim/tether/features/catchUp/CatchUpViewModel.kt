package bassamalim.tether.features.catchUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.tether.core.domain.DueState
import bassamalim.tether.core.models.TrackedPerson
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.nav.Screen
import bassamalim.tether.core.utils.dueReason
import bassamalim.tether.core.utils.initials
import bassamalim.tether.core.utils.overdueReason
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatchUpViewModel @Inject constructor(
    private val domain: CatchUpDomain,
    private val navigator: Navigator
) : ViewModel() {

    private val _events = Channel<CatchUpEvent>()
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<CatchUpUiState> = domain.observeDue().map { due ->
        CatchUpUiState(
            isLoading = false,
            overdue = due.overdue.map { it.toItem() },
            dueThisWeek = due.dueThisWeek.map { it.toItem() }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CatchUpUiState()
    )

    fun onPersonClick(id: Long) = navigator.navigate(Screen.Person(id))

    /** The check button: log it, drop the row, offer the way back. */
    fun onReachedOut(item: CatchUpItem) {
        viewModelScope.launch {
            val interactionId = domain.logReachedOut(item.id)
            _events.send(CatchUpEvent.Logged(item.name, interactionId))
        }
    }

    fun onUndo(interactionId: Long) {
        viewModelScope.launch { domain.undo(interactionId) }
    }

    private fun TrackedPerson.toItem() = CatchUpItem(
        id = person.id,
        name = person.name,
        initials = initials(person.name),
        reason = when (val state = dueState) {
            is DueState.Slipping -> overdueReason(state.daysOverdue, person.cadenceDays)
            is DueState.InTouch -> dueReason(state.daysUntilDue)
            DueState.NotTracked -> ""
        },
        isOverdue = dueState is DueState.Slipping
    )
}
