package bassamalim.tether.features.setUpImported

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import bassamalim.tether.core.data.dataSources.room.entities.Person
import bassamalim.tether.core.enums.CadencePreset
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.nav.Screen
import bassamalim.tether.core.utils.initials
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetUpImportedViewModel @Inject constructor(
    private val domain: SetUpImportedDomain,
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personIds = savedStateHandle.toRoute<Screen.SetUpImported>().personIds

    /** The queue, read once: it's a fixed list of rows that already exist. */
    private var people: List<Person> = emptyList()
    private var index = 0

    private val _uiState = MutableStateFlow(SetUpImportedUiState())
    val uiState: StateFlow<SetUpImportedUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            domain.observeRelationshipOptions().collectLatest { options ->
                _uiState.update { it.copy(relationshipOptions = options) }
            }
        }

        viewModelScope.launch {
            people = domain.getPeople(personIds)

            if (people.isEmpty()) navigator.popBackStack()
            else show(0)
        }
    }

    fun onTagChange(value: String) = _uiState.update { it.copy(tag = value) }

    /** Tapping the one already chosen clears it, the way every other chip in the app does. */
    fun onTagSelect(value: String) = _uiState.update {
        it.copy(tag = if (it.tag.equals(value, ignoreCase = true)) "" else value)
    }

    fun onCadenceSelect(value: CadencePreset) = _uiState.update { it.copy(cadence = value) }

    fun onHowYouMetChange(value: String) = _uiState.update { it.copy(howYouMet = value) }

    fun onWorkplaceChange(value: String) = _uiState.update { it.copy(workplace = value) }

    fun onJobTitleChange(value: String) = _uiState.update { it.copy(jobTitle = value) }

    /** Left as the import made them: in Tether, with no cadence and no relationship. */
    fun onSkip() = advance()

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            domain.setUp(
                personId = people[index].id,
                tag = state.tag,
                cadence = state.cadence,
                howYouMet = state.howYouMet,
                workplace = state.workplace,
                jobTitle = state.jobTitle
            )

            advance()
        }
    }

    private fun advance() {
        if (index + 1 >= people.size) navigator.popBackStack()
        else show(index + 1)
    }

    private fun show(at: Int) {
        index = at
        val person = people[at]

        // A fresh form each time: the previous person's answers are theirs, not a default.
        _uiState.value = SetUpImportedUiState(
            isLoading = false,
            // The vocabulary is the app's, not this person's, so it carries across the queue.
            relationshipOptions = _uiState.value.relationshipOptions,
            name = person.name,
            initials = initials(person.name),
            phone = person.phone.orEmpty(),
            // The address book may already have answered this one; the walk only confirms it.
            workplace = person.workplace.orEmpty(),
            jobTitle = person.jobTitle.orEmpty(),
            progress = "${at + 1} of ${people.size}",
            isLast = at == people.lastIndex
        )
    }
}
