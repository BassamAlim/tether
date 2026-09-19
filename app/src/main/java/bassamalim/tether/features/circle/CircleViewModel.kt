package bassamalim.tether.features.circle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.tether.core.domain.DueState
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.nav.Screen
import bassamalim.tether.core.utils.cadenceLabel
import bassamalim.tether.core.utils.lastTalkedStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CircleViewModel @Inject constructor(
    private val domain: CircleDomain,
    private val navigator: Navigator
) : ViewModel() {

    private val focus = MutableStateFlow<HueSlot?>(null)
    private val selectedId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<CircleUiState> = combine(
        domain.observeGraph(),
        focus,
        selectedId
    ) { graph, focused, selected ->
        val byId = graph.people.associateBy { it.tracked.person.id }
        val slipping = graph.people.count { it.tracked.isSlipping }
        val headcount = if (graph.people.size == 1) "1 person" else "${graph.people.size} people"

        CircleUiState(
            isLoading = false,
            subtitle = "$headcount · $slipping slipping",
            nodes = graph.people.map { it.toNode() },
            links = graph.links.mapNotNull { (a, b) ->
                val from = byId[a] ?: return@mapNotNull null
                val to = byId[b] ?: return@mapNotNull null
                CircleLink(a, b, from.x, from.y, to.x, to.y)
            },
            legend = graph.groups,
            // A group that emptied out (its last person retagged) can't stay focused.
            focus = focused?.takeIf { slot -> graph.groups.any { it.slot == slot } },
            // Nor can someone deleted from under the card.
            selected = selected?.let(byId::get)?.toSelected(),
            dueRadius = graph.dueRadius,
            extent = graph.extent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CircleUiState()
    )

    /** Tapping the focused entry again lets go of it. */
    fun onLegendClick(slot: HueSlot) = focus.update { if (it == slot) null else slot }

    fun onNodeClick(id: Long) = selectedId.update { if (it == id) null else id }

    fun onBackgroundClick() = selectedId.update { null }

    fun onSelectedClick(id: Long) = navigator.navigate(Screen.Person(id))

    private fun PlacedPerson.toNode() = CircleNode(
        id = tracked.person.id,
        x = x,
        y = y,
        name = tracked.person.name.trim(),
        slot = slot,
        isUntracked = tracked.dueState is DueState.NotTracked
    )

    private fun PlacedPerson.toSelected() = SelectedPerson(
        id = tracked.person.id,
        name = tracked.person.name,
        summary = listOfNotNull(
            tracked.person.tag?.trim()?.takeIf(String::isNotEmpty),
            cadenceLabel(tracked.person.cadenceDays).let { if (it == "Never") "Untracked" else it }
        ).joinToString(" · "),
        status = lastTalkedStatus(
            lastInteractionOn = tracked.lastInteractionOn,
            daysOverdue = (tracked.dueState as? DueState.Slipping)?.daysOverdue,
            today = domain.today()
        ),
        slot = slot
    )
}
