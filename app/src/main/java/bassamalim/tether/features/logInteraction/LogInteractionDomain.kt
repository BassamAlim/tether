package bassamalim.tether.features.logInteraction

import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import bassamalim.tether.core.data.repositories.InteractionsRepository
import bassamalim.tether.core.domain.TrackedPeople
import bassamalim.tether.core.enums.Initiator
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
        location: String,
        initiatedBy: Initiator?,
        note: String
    ): Long = interactionsRepository.log(
        personId = personId,
        occurredOn = occurredOn,
        type = type,
        location = location.trim(),
        initiatedBy = initiatedBy,
        note = note.trim()
    )

    suspend fun getInteraction(id: Long): Interaction? = interactionsRepository.get(id)

    /**
     * Deleting announces itself through the repository, so Person detail raises the undo bar
     * once this sheet has closed over it.
     */
    suspend fun delete(interaction: Interaction) = interactionsRepository.delete(interaction)

    /**
     * A correction rewrites the row it came from, so the history gains nothing and loses
     * nothing. Moving the date moves the clock with it, the same way logging sets it.
     */
    suspend fun update(
        interaction: Interaction,
        type: InteractionType?,
        occurredOn: LocalDate,
        location: String,
        initiatedBy: Initiator?,
        note: String
    ) = interactionsRepository.update(
        interaction.copy(
            type = type,
            occurredOn = occurredOn,
            location = location.trim(),
            initiatedBy = initiatedBy,
            note = note.trim()
        )
    )
}
