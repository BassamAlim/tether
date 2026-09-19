package bassamalim.tether.features.connect

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import bassamalim.tether.core.data.dataSources.room.entities.Person
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.nav.Screen
import bassamalim.tether.core.utils.initials
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
class ConnectViewModel @Inject constructor(
    private val domain: ConnectDomain,
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId = savedStateHandle.toRoute<Screen.Connect>().personId

    private val localState = MutableStateFlow(LocalState())

    val uiState: StateFlow<ConnectUiState> = combine(
        domain.observeName(personId),
        domain.observeCandidates(personId),
        localState
    ) { name, candidates, local ->
        val matching = candidates
            .filter { local.query.isBlank() || it.name.contains(local.query, ignoreCase = true) }
            .map { it.toCandidate() }

        ConnectUiState(
            isLoading = false,
            personName = name.orEmpty(),
            query = local.query,
            candidates = matching,
            // Read back from the live list, so a selected person who is deleted mid-flow drops
            // out rather than lingering as a stale copy.
            selected = candidates.firstOrNull { it.id == local.selectedId }?.toCandidate(),
            label = local.label
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ConnectUiState()
    )

    fun onQueryChange(value: String) = localState.update { it.copy(query = value) }

    fun onSelect(id: Long) = localState.update { it.copy(selectedId = id) }

    /** Backing out of the label step returns to the list rather than leaving the screen. */
    fun onClearSelection() = localState.update { it.copy(selectedId = null) }

    fun onLabelChange(value: String) = localState.update { it.copy(label = value) }

    /** Tapping the suggestion you already chose clears it, the way the tag chips do. */
    fun onSuggestionClick(suggestion: String) = localState.update {
        it.copy(label = if (it.label == suggestion) "" else suggestion)
    }

    fun onCancel() = navigator.popBackStack()

    fun onSave() {
        val state = uiState.value
        val other = state.selected ?: return

        viewModelScope.launch {
            domain.connect(personId = personId, otherId = other.id, label = state.label)
            navigator.popBackStack()
        }
    }

    private data class LocalState(
        val query: String = "",
        val selectedId: Long? = null,
        val label: String = ""
    )

    private fun Person.toCandidate() = ConnectCandidate(
        id = id,
        name = name,
        initials = initials(name),
        tagLabel = tag?.chipLabel
    )
}
