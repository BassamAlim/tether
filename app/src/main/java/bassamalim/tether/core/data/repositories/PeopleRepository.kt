package bassamalim.tether.core.data.repositories

import bassamalim.tether.core.data.dataSources.room.daos.PeopleDao
import bassamalim.tether.core.data.dataSources.room.entities.Person
import bassamalim.tether.core.data.dataSources.room.entities.PersonDetail
import bassamalim.tether.core.data.dataSources.room.relations.PersonWithLastInteraction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** The only way into the people table. Storage details stop here. */
@Singleton
class PeopleRepository @Inject constructor(
    private val peopleDao: PeopleDao
) {

    fun observeAll(): Flow<List<PersonWithLastInteraction>> = peopleDao.observeAll()

    fun observe(id: Long): Flow<PersonWithLastInteraction?> = peopleDao.observe(id)

    fun observeCount(): Flow<Int> = peopleDao.observeCount()

    fun observeDetails(personId: Long): Flow<List<PersonDetail>> = peopleDao.observeDetails(personId)

    fun searchByName(query: String): Flow<List<Person>> = peopleDao.searchByName(query)

    fun searchByDetails(query: String): Flow<List<Person>> = peopleDao.searchByDetails(query)

    fun searchDetails(query: String): Flow<List<PersonDetail>> = peopleDao.searchDetails(query)

    suspend fun getAll(): List<Person> = peopleDao.getAll()

    /** A named handful of people, by name order rather than by the order of [ids]. */
    suspend fun get(ids: List<Long>): List<Person> = peopleDao.get(ids)

    suspend fun getAllDetails(): List<PersonDetail> = peopleDao.getAllDetails()

    suspend fun setName(id: Long, name: String) = peopleDao.updateName(id, name.trim())

    suspend fun setTag(id: Long, tag: String?) = peopleDao.updateTag(id, tag?.trim()?.ifEmpty { null })

    /** Both at once, since they're edited together; blank means unsaid. */
    suspend fun setWork(id: Long, workplace: String?, jobTitle: String?) = peopleDao.updateWork(
        id = id,
        workplace = workplace?.trim()?.ifEmpty { null },
        jobTitle = jobTitle?.trim()?.ifEmpty { null }
    )

    suspend fun setCadence(id: Long, cadenceDays: Int?) = peopleDao.updateCadence(id, cadenceDays)

    suspend fun save(person: Person): Long = peopleDao.upsert(person)

    suspend fun create(person: Person, details: List<PersonDetail> = emptyList()): Long =
        peopleDao.insertWithDetails(person, details)

    /** Contacts import copies people in once; it never syncs back. Returns the new rows' ids. */
    suspend fun saveAll(people: List<Person>): List<Long> = peopleDao.upsertAll(people)

    suspend fun addDetails(details: List<PersonDetail>) = peopleDao.insertDetails(details)

    suspend fun delete(person: Person) = peopleDao.delete(person)

    suspend fun delete(id: Long) = peopleDao.deleteById(id)
}
