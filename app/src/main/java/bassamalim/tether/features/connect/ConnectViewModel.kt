package bassamalim.tether.features.connect

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
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
        domain.observeRelationshipOptions(),
        localState
    ) { name, candidates, options, local ->
        // Picks are read back out of the live list, in its order, so someone deleted mid-flow
        // drops out rather than lingering as a stale copy, and the two steps agree on order.
        val picks = candidates
            .filter { it.id in local.selectedIds }
            .map { person ->
                val ownLabel = local.ownLabels[person.id]
                val label = ownLabel ?: local.sharedLabel

                ConnectPick(
                    id = person.id,
                    name = person.name,
                    initials = initials(person.name),
                    label = label,
                    subtitle = label.ifBlank { "No label" },
                    hasOwnLabel = ownLabel != null
                )
            }

        ConnectUiState(
            isLoading = false,
            personName = name.orEmpty(),
            query = local.query,
            relationshipOptions = options,
            candidates = candidates
                .filter { local.query.isBlank() || it.name.contains(local.query, true) }
                .map { person ->
                    ConnectCandidate(
                        id = person.id,
                        name = person.name,
                        initials = initials(person.name),
                        tagLabel = person.tag?.uppercase(),
                        isSelected = person.id in local.selectedIds
                    )
                },
            // Everyone unpicking themselves drops you back to the list rather than stranding
            // you on a label step with nobody to label.
            isLabelling = local.isLabelling && picks.isNotEmpty(),
            sharedLabel = local.sharedLabel,
            picks = picks,
            editing = picks.firstOrNull { it.id == local.editingId }?.let { pick ->
                // The draft is the local one, so typing isn't overwritten by the flow.
                ConnectPickEdit(id = pick.id, name = pick.name, label = local.editingLabel)
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ConnectUiState()
    )

    fun onQueryChange(value: String) = localState.update { it.copy(query = value) }

    /** Unpicking someone forgets the line you wrote for them; re-picking starts clean. */
    fun onToggle(id: Long) = localState.update { local ->
        if (id in local.selectedIds) local.copy(
            selectedIds = local.selectedIds - id,
            ownLabels = local.ownLabels - id
        )
        else local.copy(selectedIds = local.selectedIds + id)
    }

    fun onNext() {
        if (localState.value.selectedIds.isEmpty()) return

        localState.update { it.copy(isLabelling = true) }
    }

    /** Back steps within the screen before it leaves it, so a long selection isn't lost. */
    fun onBack() {
        if (uiState.value.isLabelling) localState.update { it.copy(isLabelling = false) }
        else navigator.popBackStack()
    }

    fun onSharedLabelChange(value: String) = localState.update { it.copy(sharedLabel = value) }

    /** Tapping the suggestion you already chose clears it, the way the tag chips do. */
    fun onSuggestionClick(suggestion: String) = localState.update {
        it.copy(sharedLabel = if (it.sharedLabel == suggestion) "" else suggestion)
    }

    fun onPickClick(pick: ConnectPick) = localState.update {
        it.copy(editingId = pick.id, editingLabel = pick.label)
    }

    fun onPickLabelChange(value: String) = localState.update { it.copy(editingLabel = value) }

    fun onPickSuggestionClick(suggestion: String) = localState.update {
        it.copy(editingLabel = if (it.editingLabel == suggestion) "" else suggestion)
    }

    fun onPickEditDismiss() =
        localState.update { it.copy(editingId = null, editingLabel = "") }

    fun onPickLabelSave() = localState.update {
        val id = it.editingId ?: return@update it

        it.copy(ownLabels = it.ownLabels + (id to it.editingLabel), editingId = null, editingLabel = "")
    }

    /** Handing someone back to the shared line, rather than blanking their own to mean the same. */
    fun onPickUseSharedLabel() = localState.update {
        val id = it.editingId ?: return@update it

        it.copy(ownLabels = it.ownLabels - id, editingId = null, editingLabel = "")
    }

    fun onSave() {
        val picks = uiState.value.picks
        if (picks.isEmpty()) return

        viewModelScope.launch {
            domain.connectAll(personId, picks.associate { it.id to it.label })
            navigator.popBackStack()
        }
    }

    private data class LocalState(
        val query: String = "",
        val selectedIds: Set<Long> = emptySet(),
        val isLabelling: Boolean = false,
        val sharedLabel: String = "",
        /** Only the people given a line of their own; everyone else follows [sharedLabel]. */
        val ownLabels: Map<Long, String> = emptyMap(),
        val editingId: Long? = null,
        val editingLabel: String = ""
    )
}
