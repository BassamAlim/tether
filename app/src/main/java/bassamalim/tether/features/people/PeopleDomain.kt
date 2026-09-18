package bassamalim.tether.features.people

import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.domain.DueState
import bassamalim.tether.core.domain.dueState
import bassamalim.tether.core.models.TrackedPerson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/**
 * People is sorted by who's slipping, not alphabetically. An address book tells you who you
 * know; this tells you who you're losing.
 */
class PeopleDomain @Inject constructor(
    private val peopleRepository: PeopleRepository,
    private val clock: Clock
) {

    fun observePeople(): Flow<List<TrackedPerson>> = peopleRepository.observeAll().map { rows ->
        val today = LocalDate.now(clock)

        rows.map { row ->
            TrackedPerson(
                person = row.person,
                lastInteractionOn = row.lastInteractionOn,
                dueState = dueState(
                    cadenceDays = row.person.cadenceDays,
                    lastInteractionOn = row.lastInteractionOn,
                    addedOn = row.person.addedOn,
                    today = today
                )
            )
        }
    }

    fun today(): LocalDate = LocalDate.now(clock)

    /** Most overdue first; everyone else by how recently you spoke. */
    fun sortSlipping(people: List<TrackedPerson>) = people.sortedByDescending {
        (it.dueState as? DueState.Slipping)?.daysOverdue ?: Long.MIN_VALUE
    }

    fun sortInTouch(people: List<TrackedPerson>) = people.sortedByDescending { it.lastInteractionOn }
}
