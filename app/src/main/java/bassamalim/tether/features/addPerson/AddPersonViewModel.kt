package bassamalim.tether.features.addPerson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.tether.core.enums.CadencePreset
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.nav.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPersonViewModel @Inject constructor(
    private val domain: AddPersonDomain,
    private val navigator: Navigator
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPersonUiState())
    val uiState: StateFlow<AddPersonUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            domain.observeRelationshipOptions().collectLatest { options ->
                _uiState.update { it.copy(relationshipOptions = options) }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }

    fun onTagChange(value: String) = _uiState.update { it.copy(tag = value) }

    /** Tapping the one already chosen clears it, the way every other chip in the app does. */
    fun onTagSelect(value: String) = _uiState.update {
        it.copy(tag = if (it.tag.equals(value, ignoreCase = true)) "" else value)
    }

    fun onCadenceSelect(value: CadencePreset) = _uiState.update { it.copy(cadence = value) }

    fun onHowYouMetChange(value: String) = _uiState.update { it.copy(howYouMet = value) }

    fun onCancel() = navigator.popBackStack()

    /**
     * The other way in. The picker replaces this form rather than stacking over it: it's a
     * different route to the same place, not a step inside this one, so backing out of it lands
     * wherever New person was opened from.
     */
    fun onFromContactsClick() = navigator.navigate(Screen.ImportContacts) {
        popUpTo(Screen.AddPerson) { inclusive = true }
    }

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            domain.create(
                name = state.name,
                tag = state.tag,
                cadence = state.cadence,
                howYouMet = state.howYouMet
            )

            navigator.popBackStack()
        }
    }
}
