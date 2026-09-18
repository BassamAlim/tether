package bassamalim.tether.features.addPerson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.tether.core.enums.CadencePreset
import bassamalim.tether.core.enums.RelationshipTag
import bassamalim.tether.core.nav.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
            val default = domain.defaultCadence()
            _uiState.update { it.copy(cadence = default) }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }

    fun onTagSelect(value: RelationshipTag) = _uiState.update {
        it.copy(tag = if (it.tag == value) null else value)
    }

    fun onCadenceSelect(value: CadencePreset) = _uiState.update { it.copy(cadence = value) }

    fun onHowYouMetChange(value: String) = _uiState.update { it.copy(howYouMet = value) }

    fun onCancel() = navigator.popBackStack()

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
