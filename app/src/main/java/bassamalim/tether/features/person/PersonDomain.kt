package bassamalim.tether.features.person

import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import bassamalim.tether.core.data.dataSources.room.entities.PersonDetail
import bassamalim.tether.core.data.repositories.InteractionsRepository
import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.domain.TrackedPeople
import bassamalim.tether.core.models.TrackedPerson
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class PersonDomain @Inject constructor(
    private val trackedPeople: TrackedPeople,
    private val peopleRepository: PeopleRepository,
    private val interactionsRepository: InteractionsRepository
) {

    fun observePerson(id: Long): Flow<TrackedPerson?> = trackedPeople.observe(id)

    fun observeDetails(id: Long): Flow<List<PersonDetail>> = peopleRepository.observeDetails(id)

    /** Newest first, and never edited — you log, you don't curate. */
    fun observeHistory(id: Long): Flow<List<Interaction>> =
        interactionsRepository.observeForPerson(id)

    fun today(): LocalDate = trackedPeople.today()

    suspend fun delete(id: Long) = peopleRepository.delete(id)
}
