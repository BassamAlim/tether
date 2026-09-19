package bassamalim.tether.features.person

import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import bassamalim.tether.core.data.dataSources.room.entities.PersonDetail
import bassamalim.tether.core.data.dataSources.room.entities.Reminder
import bassamalim.tether.core.data.dataSources.room.relations.ConnectedPerson
import bassamalim.tether.core.data.repositories.ConnectionsRepository
import bassamalim.tether.core.data.repositories.InteractionsRepository
import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.data.repositories.RemindersRepository
import bassamalim.tether.core.data.repositories.RelationshipTypesRepository
import bassamalim.tether.core.domain.TrackedPeople
import bassamalim.tether.core.models.TrackedPerson
import bassamalim.tether.core.reminder.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import java.time.LocalDate
import javax.inject.Inject

class PersonDomain @Inject constructor(
    private val trackedPeople: TrackedPeople,
    private val peopleRepository: PeopleRepository,
    private val interactionsRepository: InteractionsRepository,
    private val connectionsRepository: ConnectionsRepository,
    private val remindersRepository: RemindersRepository,
    private val reminderScheduler: ReminderScheduler,
    private val relationshipTypesRepository: RelationshipTypesRepository
) {

    fun observeRelationshipOptions(): Flow<List<String>> = relationshipTypesRepository.observeAll()

    fun observePerson(id: Long): Flow<TrackedPerson?> = trackedPeople.observe(id)

    fun observeDetails(id: Long): Flow<List<PersonDetail>> = peopleRepository.observeDetails(id)

    /** Newest first. Entries can be corrected or removed, but never reordered by hand. */
    fun observeHistory(id: Long): Flow<List<Interaction>> =
        interactionsRepository.observeForPerson(id)

    /** The bell: their one pending reminder, or null when it isn't set. */
    fun observeReminder(id: Long): Flow<Reminder?> = remindersRepository.observeForPerson(id)

    /** Who they know, from either end of the link. */
    fun observeConnections(id: Long): Flow<List<ConnectedPerson>> =
        connectionsRepository.observeFor(id)

    fun today(): LocalDate = trackedPeople.today()

    /** A blank name is refused here as well as on the button: every row needs something to show. */
    suspend fun setName(id: Long, name: String) {
        if (name.isBlank()) return
        peopleRepository.setName(id, name)
    }

    suspend fun setTag(id: Long, tag: String?) {
        relationshipTypesRepository.remember(tag)
        peopleRepository.setTag(id, tag)
    }

    /** Jobs change more often than names do; both halves are rewritten together. */
    suspend fun setWork(id: Long, workplace: String, jobTitle: String) =
        peopleRepository.setWork(id, workplace, jobTitle)

    suspend fun setCadence(id: Long, cadenceDays: Int?) =
        peopleRepository.setCadence(id, cadenceDays)

    suspend fun setConnectionLabel(connectionId: Long, label: String) {
        relationshipTypesRepository.remember(label)
        connectionsRepository.setLabel(connectionId, label)
    }

    suspend fun disconnect(connectionId: Long) = connectionsRepository.disconnect(connectionId)

    /** Deletions of this person's catch-ups, wherever they were tapped. */
    fun observeDeletions(personId: Long): Flow<Interaction> =
        interactionsRepository.deletions.filter { it.personId == personId }

    suspend fun getInteraction(id: Long): Interaction? = interactionsRepository.get(id)

    suspend fun deleteInteraction(interaction: Interaction) =
        interactionsRepository.delete(interaction)

    suspend fun restoreInteraction(interaction: Interaction) =
        interactionsRepository.restore(interaction)

    /** Deleting them cascades their rows away; the pending alarm has to be called off by hand. */
    suspend fun delete(id: Long) {
        reminderScheduler.cancel(id)
        peopleRepository.delete(id)
    }
}
