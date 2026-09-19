package bassamalim.tether.features.importContacts

import bassamalim.tether.core.data.dataSources.room.entities.Person
import bassamalim.tether.core.data.repositories.ContactsRepository
import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.data.repositories.PreferencesRepository
import bassamalim.tether.core.models.DeviceContact
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class ImportContactsDomain @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val peopleRepository: PeopleRepository,
    private val preferencesRepository: PreferencesRepository,
    private val clock: Clock
) {

    suspend fun readContacts(): List<DeviceContact> = contactsRepository.read()

    suspend fun defaultCadenceDays(): Int? = preferencesRepository.observeDefaultCadenceDays().first()

    /**
     * Imported people start on the default cadence so the flow stays two taps; tuning happens
     * per person afterwards. That default may be "Never", in which case they are simply added.
     */
    suspend fun import(contacts: List<DeviceContact>) {
        val cadenceDays = defaultCadenceDays()
        val today = LocalDate.now(clock)

        peopleRepository.saveAll(
            contacts.map { contact ->
                Person(
                    name = contact.name,
                    cadenceDays = cadenceDays,
                    phone = contact.phone,
                    addedOn = today
                )
            }
        )
    }
}
