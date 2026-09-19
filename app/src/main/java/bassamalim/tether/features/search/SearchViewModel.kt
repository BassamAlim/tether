package bassamalim.tether.features.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.nav.Screen
import bassamalim.tether.core.utils.cadenceLabel
import bassamalim.tether.core.utils.elapsedLabel
import bassamalim.tether.core.utils.initials
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val domain: SearchDomain,
    private val navigator: Navigator
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<SearchUiState> = combine(
        query,
        query.flatMapLatest { domain.observeResults(it) }
    ) { query, results ->
        val today = domain.today()

        SearchUiState(
            query = query,
            people = results.people.map { match ->
                PersonResult(
                    id = match.person.person.id,
                    name = match.person.person.name,
                    initials = initials(match.person.person.name),
                    subtitle = match.detail
                        ?.let { "${it.label} ${it.value}" }
                        ?: cadenceLabel(match.person.person.cadenceDays),
                    lastContactLabel = elapsedLabel(match.person.lastInteractionOn, today)
                )
            },
            notes = results.notes.map { match ->
                NoteResult(
                    id = match.interaction.id,
                    personId = match.person.person.id,
                    personName = match.person.person.name,
                    initials = initials(match.person.person.name),
                    meta = listOfNotNull(
                        match.interaction.type?.label,
                        match.interaction.location.takeIf { it.isNotBlank() },
                        elapsedLabel(match.interaction.occurredOn, today)
                    ).joinToString(" · "),
                    note = match.interaction.note
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUiState()
    )

    fun onQueryChange(value: String) = query.update { value }

    fun onClear() = query.update { "" }

    fun onPersonClick(id: Long) = navigator.navigate(Screen.Person(id))

    fun onBack() = navigator.popBackStack()
}
