package bassamalim.tether.core.data.repositories

import bassamalim.tether.core.data.dataSources.room.daos.PeopleDao
import bassamalim.tether.core.data.dataSources.room.entities.Person
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

    fun searchByName(query: String): Flow<List<Person>> = peopleDao.searchByName(query)

    fun searchByNotes(query: String): Flow<List<Person>> = peopleDao.searchByNotes(query)

    suspend fun save(person: Person): Long = peopleDao.upsert(person)

    /** Contacts import copies people in once; it never syncs back. */
    suspend fun saveAll(people: List<Person>) = peopleDao.upsertAll(people)

    suspend fun delete(person: Person) = peopleDao.delete(person)
}
