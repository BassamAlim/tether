package bassamalim.tether.features.logInteraction

import bassamalim.tether.core.data.repositories.InteractionsRepository
import bassamalim.tether.core.domain.TrackedPeople
import bassamalim.tether.core.enums.InteractionType
import bassamalim.tether.core.models.TrackedPerson
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class LogInteractionDomain @Inject constructor(
    private val trackedPeople: TrackedPeople,
    private val interactionsRepository: InteractionsRepository
) {

    fun observePerson(id: Long): Flow<TrackedPerson?> = trackedPeople.observe(id)

    fun today(): LocalDate = trackedPeople.today()

    /**
     * Saving resets the clock from the interaction's own date, not from now: backfilling last
     * Tuesday's coffee shouldn't buy an extra week.
     */
    suspend fun log(
        personId: Long,
        type: InteractionType?,
        occurredOn: LocalDate,
        note: String
    ): Long = interactionsRepository.log(
        personId = personId,
        occurredOn = occurredOn,
        type = type,
        note = note.trim()
    )
}
