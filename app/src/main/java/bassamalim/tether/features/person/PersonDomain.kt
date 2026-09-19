package bassamalim.tether.features.person

import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import bassamalim.tether.core.data.dataSources.room.entities.PersonDetail
import bassamalim.tether.core.data.dataSources.room.relations.ConnectedPerson
import bassamalim.tether.core.data.repositories.ConnectionsRepository
import bassamalim.tether.core.data.repositories.InteractionsRepository
import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.domain.TrackedPeople
import bassamalim.tether.core.enums.RelationshipTag
import bassamalim.tether.core.models.TrackedPerson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import java.time.LocalDate
import javax.inject.Inject

class PersonDomain @Inject constructor(
    private val trackedPeople: TrackedPeople,
    private val peopleRepository: PeopleRepository,
    private val interactionsRepository: InteractionsRepository,
    private val connectionsRepository: ConnectionsRepository
) {

    fun observePerson(id: Long): Flow<TrackedPerson?> = trackedPeople.observe(id)

    fun observeDetails(id: Long): Flow<List<PersonDetail>> = peopleRepository.observeDetails(id)

    /** Newest first. Entries can be corrected or removed, but never reordered by hand. */
    fun observeHistory(id: Long): Flow<List<Interaction>> =
        interactionsRepository.observeForPerson(id)

    /** Who they know, from either end of the link. */
    fun observeConnections(id: Long): Flow<List<ConnectedPerson>> =
        connectionsRepository.observeFor(id)

    fun today(): LocalDate = trackedPeople.today()

    suspend fun setTag(id: Long, tag: RelationshipTag?) = peopleRepository.setTag(id, tag)

    suspend fun setCadence(id: Long, cadenceDays: Int?) =
        peopleRepository.setCadence(id, cadenceDays)

    suspend fun setConnectionLabel(connectionId: Long, label: String) =
        connectionsRepository.setLabel(connectionId, label)

    suspend fun disconnect(connectionId: Long) = connectionsRepository.disconnect(connectionId)

    /** Deletions of this person's catch-ups, wherever they were tapped. */
    fun observeDeletions(personId: Long): Flow<Interaction> =
        interactionsRepository.deletions.filter { it.personId == personId }

    suspend fun getInteraction(id: Long): Interaction? = interactionsRepository.get(id)

    suspend fun deleteInteraction(interaction: Interaction) =
        interactionsRepository.delete(interaction)

    suspend fun restoreInteraction(interaction: Interaction) =
        interactionsRepository.restore(interaction)

    suspend fun delete(id: Long) = peopleRepository.delete(id)
}
