package bassamalim.tether.features.importContacts

import bassamalim.tether.core.data.dataSources.room.entities.Person
import bassamalim.tether.core.data.repositories.ContactsRepository
import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.models.DeviceContact
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class ImportContactsDomain @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val peopleRepository: PeopleRepository,
    private val clock: Clock
) {

    suspend fun readContacts(): List<DeviceContact> = contactsRepository.read()

    /**
     * Imported people land with a name, a number and nothing else: an address book knows how to
     * reach someone, not what they are to you. The relationship, the cadence and how you met are
     * asked for straight after, one person at a time, and whoever you walk away from stays
     * untracked rather than half-guessed.
     *
     * Returns the new rows' ids, which is the queue that walk works through.
     */
    suspend fun import(contacts: List<DeviceContact>): List<Long> {
        val today = LocalDate.now(clock)

        return peopleRepository.saveAll(
            contacts.map { contact ->
                Person(
                    name = contact.name,
                    cadenceDays = null,
                    phone = contact.phone,
                    addedOn = today
                )
            }
        )
    }
}
