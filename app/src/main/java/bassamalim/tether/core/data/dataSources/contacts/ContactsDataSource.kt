package bassamalim.tether.core.data.dataSources.contacts

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Data
import bassamalim.tether.core.di.IoDispatcher
import bassamalim.tether.core.models.DeviceContact
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads the device address book. One pass over the Data table rather than a query per contact,
 * because 800 contacts is a normal number and a picker that takes three seconds to open is a
 * picker you don't use.
 */
@Singleton
class ContactsDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher
) {

    suspend fun read(): List<DeviceContact> = withContext(dispatcher) {
        val contacts = LinkedHashMap<Long, DeviceContact>()

        context.contentResolver.query(
            Data.CONTENT_URI,
            arrayOf(
                Data.CONTACT_ID,
                Data.DISPLAY_NAME_PRIMARY,
                Data.MIMETYPE,
                Data.DATA1,
                Organization.TITLE
            ),
            "${Data.MIMETYPE} IN (?, ?, ?) AND ${Data.DISPLAY_NAME_PRIMARY} IS NOT NULL",
            arrayOf(Phone.CONTENT_ITEM_TYPE, Email.CONTENT_ITEM_TYPE, Organization.CONTENT_ITEM_TYPE),
            "${Data.DISPLAY_NAME_PRIMARY} COLLATE NOCASE ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(Data.CONTACT_ID)
            val nameColumn = cursor.getColumnIndexOrThrow(Data.DISPLAY_NAME_PRIMARY)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(Data.MIMETYPE)
            val valueColumn = cursor.getColumnIndexOrThrow(Data.DATA1)
            val titleColumn = cursor.getColumnIndexOrThrow(Organization.TITLE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)?.trim().orEmpty()
                if (name.isEmpty()) continue

                val mimeType = cursor.getString(mimeTypeColumn)
                val value = cursor.getString(valueColumn)?.trim().orEmpty()
                // An Organization row's DATA1 is the company, which may be missing where the
                // title isn't; every other kind is nothing without its value.
                val title = if (mimeType == Organization.CONTENT_ITEM_TYPE)
                    cursor.getString(titleColumn)?.trim().orEmpty()
                else ""
                if (value.isEmpty() && title.isEmpty()) continue

                val existing = contacts[id] ?: DeviceContact(id = id, name = name)

                contacts[id] = when (mimeType) {
                    // The first of each wins; a contact's other numbers aren't the picker's business.
                    Phone.CONTENT_ITEM_TYPE -> existing.copy(phone = existing.phone ?: value)
                    Email.CONTENT_ITEM_TYPE -> existing.copy(email = existing.email ?: value)
                    // Company and title are one entry, so they're taken as a pair.
                    Organization.CONTENT_ITEM_TYPE ->
                        if (existing.workplace != null || existing.jobTitle != null) existing
                        else existing.copy(
                            workplace = value.ifEmpty { null },
                            jobTitle = title.ifEmpty { null }
                        )
                    else -> existing
                }
            }
        }

        contacts.values.toList()
    }
}
