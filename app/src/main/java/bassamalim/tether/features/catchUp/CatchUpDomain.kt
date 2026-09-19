package bassamalim.tether.features.catchUp

import bassamalim.tether.core.data.repositories.InteractionsRepository
import bassamalim.tether.core.domain.DueState
import bassamalim.tether.core.domain.TrackedPeople
import bassamalim.tether.core.models.TrackedPerson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * The screen that makes this a CRM instead of a contact list: who is due, and one tap to say you
 * reached out.
 */
class CatchUpDomain @Inject constructor(
    private val trackedPeople: TrackedPeople,
    private val interactionsRepository: InteractionsRepository
) {

    fun observeDue(): Flow<DuePeople> = trackedPeople.observe().map { people ->
        DuePeople(
            // Most overdue first: the person you're losing fastest is the one to call.
            overdue = people
                .filter { it.dueState is DueState.Slipping }
                .sortedByDescending { (it.dueState as DueState.Slipping).daysOverdue },
            dueThisWeek = people
                .mapNotNull { person ->
                    (person.dueState as? DueState.InTouch)
                        ?.takeIf { it.daysUntilDue <= DAYS_IN_WEEK }
                        ?.let { person to it.daysUntilDue }
                }
                .sortedBy { it.second }
                .map { it.first }
        )
    }

    /**
     * A one-tap log: no type, no note, dated today. Returns the interaction id so the tap can be
     * undone: the tap is easy to make by accident and the data is irreplaceable.
     */
    suspend fun logReachedOut(personId: Long): Long =
        interactionsRepository.log(personId = personId, occurredOn = trackedPeople.today())

    suspend fun undo(interactionId: Long) = interactionsRepository.undo(interactionId)

    private companion object {
        const val DAYS_IN_WEEK = 7L
    }
}

data class DuePeople(
    val overdue: List<TrackedPerson> = emptyList(),
    val dueThisWeek: List<TrackedPerson> = emptyList()
)
