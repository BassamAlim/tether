package bassamalim.tether.core.data.repositories

import bassamalim.tether.core.data.dataSources.contacts.ContactsDataSource
import bassamalim.tether.core.models.DeviceContact
import javax.inject.Inject
import javax.inject.Singleton

/** Read-only, by design: Tether copies contacts in once and never writes back. */
@Singleton
class ContactsRepository @Inject constructor(
    private val contactsDataSource: ContactsDataSource
) {

    suspend fun read(): List<DeviceContact> = contactsDataSource.read()
}
