package bassamalim.tether.features.setUpImported

import bassamalim.tether.core.data.dataSources.room.entities.Person
import bassamalim.tether.core.data.dataSources.room.entities.PersonDetail
import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.data.repositories.RelationshipTypesRepository
import bassamalim.tether.core.enums.CadencePreset
import javax.inject.Inject

class SetUpImportedDomain @Inject constructor(
    private val peopleRepository: PeopleRepository,
    private val relationshipTypesRepository: RelationshipTypesRepository
) {

    fun observeRelationshipOptions() = relationshipTypesRepository.observeAll()

    /** The people the import just created, in the order People would list them. */
    suspend fun getPeople(ids: List<Long>): List<Person> = peopleRepository.get(ids)

    /**
     * The fields the address book couldn't answer, plus the ones it could and this screen showed. The person already exists, so this
     * screen never creates anything — walking away leaves them exactly as the import left them.
     */
    suspend fun setUp(
        personId: Long,
        tag: String,
        cadence: CadencePreset,
        howYouMet: String,
        workplace: String,
        jobTitle: String
    ) {
        relationshipTypesRepository.remember(tag)
        peopleRepository.setTag(personId, tag)
        peopleRepository.setCadence(personId, cadence.days)
        // Written back whatever they say, so correcting what the address book had — or clearing
        // it — sticks; the fields opened holding exactly what's stored.
        peopleRepository.setWork(personId, workplace, jobTitle)

        howYouMet.trim()
            .takeIf { it.isNotEmpty() }
            ?.let {
                peopleRepository.addDetails(
                    listOf(PersonDetail(personId = personId, label = "Met", value = it))
                )
            }
    }
}
