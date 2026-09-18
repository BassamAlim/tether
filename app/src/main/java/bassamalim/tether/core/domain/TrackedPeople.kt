package bassamalim.tether.core.domain

import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.models.TrackedPerson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * People with their cadence resolved against today. Every screen that cares who's slipping reads
 * this, so the rule lives in exactly one place.
 */
@Singleton
class TrackedPeople @Inject constructor(
    private val peopleRepository: PeopleRepository,
    private val clock: Clock
) {

    fun observe(): Flow<List<TrackedPerson>> = peopleRepository.observeAll().map { rows ->
        val today = today()

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
}
