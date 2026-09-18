package bassamalim.tether.features.addPerson

import bassamalim.tether.core.data.dataSources.room.entities.Person
import bassamalim.tether.core.data.dataSources.room.entities.PersonDetail
import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.data.repositories.PreferencesRepository
import bassamalim.tether.core.enums.CadencePreset
import bassamalim.tether.core.enums.RelationshipTag
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class AddPersonDomain @Inject constructor(
    private val peopleRepository: PeopleRepository,
    private val preferencesRepository: PreferencesRepository,
    private val clock: Clock
) {

    suspend fun defaultCadence(): CadencePreset =
        CadencePreset.of(preferencesRepository.observeDefaultCadenceDays().first())

    /**
     * The person is added as of today, so their first check-in comes due a cadence from now
     * rather than immediately.
     */
    suspend fun create(
        name: String,
        tag: RelationshipTag?,
        cadence: CadencePreset,
        howYouMet: String
    ): Long = peopleRepository.create(
        person = Person(
            name = name.trim(),
            tag = tag,
            cadenceDays = cadence.days,
            addedOn = LocalDate.now(clock)
        ),
        details = listOfNotNull(
            howYouMet.trim()
                .takeIf { it.isNotEmpty() }
                ?.let { PersonDetail(personId = 0, label = "Met", value = it) }
        )
    )
}
