package bassamalim.tether.features.addPerson

import bassamalim.tether.core.data.dataSources.room.entities.Person
import bassamalim.tether.core.data.dataSources.room.entities.PersonDetail
import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.data.repositories.RelationshipTypesRepository
import bassamalim.tether.core.enums.CadencePreset
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class AddPersonDomain @Inject constructor(
    private val peopleRepository: PeopleRepository,
    private val relationshipTypesRepository: RelationshipTypesRepository,
    private val clock: Clock
) {

    fun observeRelationshipOptions() = relationshipTypesRepository.observeAll()

    /**
     * The person is added as of today, so their first check-in comes due a cadence from now
     * rather than immediately.
     */
    suspend fun create(
        name: String,
        tag: String,
        cadence: CadencePreset,
        howYouMet: String,
        workplace: String,
        jobTitle: String
    ): Long {
        // A relationship typed here is one you'll reach for again, so it joins the vocabulary.
        relationshipTypesRepository.remember(tag)

        return peopleRepository.create(
            person = Person(
                name = name.trim(),
                tag = tag.trim().ifEmpty { null },
                cadenceDays = cadence.days,
                workplace = workplace.trim().ifEmpty { null },
                jobTitle = jobTitle.trim().ifEmpty { null },
                addedOn = LocalDate.now(clock)
            ),
            details = listOfNotNull(
                howYouMet.trim()
                    .takeIf { it.isNotEmpty() }
                    ?.let { PersonDetail(personId = 0, label = "Met", value = it) }
            )
        )
    }
}
