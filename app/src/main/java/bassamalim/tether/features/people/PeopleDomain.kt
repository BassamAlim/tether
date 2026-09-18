package bassamalim.tether.features.people

import bassamalim.tether.core.domain.DueState
import bassamalim.tether.core.domain.TrackedPeople
import bassamalim.tether.core.models.TrackedPerson
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * People is sorted by who's slipping, not alphabetically. An address book tells you who you
 * know; this tells you who you're losing.
 */
class PeopleDomain @Inject constructor(
    private val trackedPeople: TrackedPeople
) {

    fun observePeople(): Flow<List<TrackedPerson>> = trackedPeople.observe()

    fun today(): LocalDate = trackedPeople.today()

    /** Most overdue first; everyone else by how recently you spoke. */
    fun sortSlipping(people: List<TrackedPerson>) = people.sortedByDescending {
        (it.dueState as? DueState.Slipping)?.daysOverdue ?: Long.MIN_VALUE
    }

    fun sortInTouch(people: List<TrackedPerson>) = people.sortedByDescending { it.lastInteractionOn }
}
