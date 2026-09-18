package bassamalim.tether.features.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.tether.core.enums.RelationshipTag
import bassamalim.tether.core.models.TrackedPerson
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.nav.Screen
import bassamalim.tether.core.utils.cadenceLabel
import bassamalim.tether.core.utils.elapsedLabel
import bassamalim.tether.core.utils.initials
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val domain: PeopleDomain,
    private val navigator: Navigator
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(PeopleFilter.ALL)

    val uiState: StateFlow<PeopleUiState> = combine(
        domain.observePeople(),
        query,
        filter
    ) { people, query, filter ->
        val visible = people
            .filter { it.person.name.contains(query, ignoreCase = true) }
            .filter { it.matches(filter) }

        PeopleUiState(
            isLoading = false,
            query = query,
            filter = filter,
            totalCount = people.size,
            slippingCount = people.count { it.isSlipping },
            slipping = domain.sortSlipping(visible.filter { it.isSlipping }).map { it.toListItem() },
            inTouch = domain.sortInTouch(visible.filterNot { it.isSlipping }).map { it.toListItem() }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PeopleUiState()
    )

    fun onQueryChange(value: String) = query.update { value }

    fun onFilterSelect(value: PeopleFilter) = filter.update { value }

    fun onPersonClick(id: Long) = navigator.navigate(Screen.Person(id))

    fun onAddPersonClick() = navigator.navigate(Screen.AddPerson)

    private fun TrackedPerson.matches(filter: PeopleFilter) = when (filter) {
        PeopleFilter.ALL -> true
        PeopleFilter.SLIPPING -> isSlipping
        PeopleFilter.CLOSE -> person.tag == RelationshipTag.CLOSE
        PeopleFilter.WORK -> person.tag == RelationshipTag.WORK
    }

    private fun TrackedPerson.toListItem() = PersonListItem(
        id = person.id,
        name = person.name,
        initials = initials(person.name),
        tag = person.tag,
        cadenceLabel = cadenceLabel(person.cadenceDays),
        lastContactLabel = elapsedLabel(lastInteractionOn, domain.today()),
        isSlipping = isSlipping
    )
}
