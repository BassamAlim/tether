package bassamalim.tether.core.data.repositories

import bassamalim.tether.core.data.dataSources.room.daos.PeopleDao
import bassamalim.tether.core.data.dataSources.room.entities.Person
import bassamalim.tether.core.data.dataSources.room.entities.PersonDetail
import bassamalim.tether.core.data.dataSources.room.relations.PersonWithLastInteraction
import bassamalim.tether.core.enums.RelationshipTag
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

    suspend fun getAllDetails(): List<PersonDetail> = peopleDao.getAllDetails()

    suspend fun setTag(id: Long, tag: RelationshipTag?) = peopleDao.updateTag(id, tag)

    suspend fun setCadence(id: Long, cadenceDays: Int?) = peopleDao.updateCadence(id, cadenceDays)

    suspend fun save(person: Person): Long = peopleDao.upsert(person)

    suspend fun create(person: Person, details: List<PersonDetail> = emptyList()): Long =
        peopleDao.insertWithDetails(person, details)

    /** Contacts import copies people in once; it never syncs back. */
    suspend fun saveAll(people: List<Person>) = peopleDao.upsertAll(people)

    suspend fun delete(person: Person) = peopleDao.delete(person)

    suspend fun delete(id: Long) = peopleDao.deleteById(id)
}
