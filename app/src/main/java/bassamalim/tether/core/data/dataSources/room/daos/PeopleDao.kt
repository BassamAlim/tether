package bassamalim.tether.core.data.dataSources.room.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import bassamalim.tether.core.data.dataSources.room.entities.Person
import bassamalim.tether.core.data.dataSources.room.entities.PersonDetail
import bassamalim.tether.core.data.dataSources.room.relations.PersonWithLastInteraction
import kotlinx.coroutines.flow.Flow

@Dao
interface PeopleDao {

    @Query(
        """
        SELECT p.*, (
            SELECT MAX(i.occurredOn) FROM interactions i WHERE i.personId = p.id
        ) AS lastInteractionOn
        FROM people p
        WHERE p.archived = 0
        ORDER BY p.name COLLATE NOCASE
        """
    )
    fun observeAll(): Flow<List<PersonWithLastInteraction>>

    @Query(
        """
        SELECT p.*, (
            SELECT MAX(i.occurredOn) FROM interactions i WHERE i.personId = p.id
        ) AS lastInteractionOn
        FROM people p
        WHERE p.id = :id
        """
    )
    fun observe(id: Long): Flow<PersonWithLastInteraction?>

    /** Name matches and detail matches answer different questions, so they're queried apart. */
    @Query(
        """
        SELECT * FROM people
        WHERE archived = 0 AND name LIKE '%' || :query || '%'
        ORDER BY name COLLATE NOCASE
        """
    )
    fun searchByName(query: String): Flow<List<Person>>

    @Query(
        """
        SELECT DISTINCT p.* FROM people p
        JOIN person_details d ON d.personId = p.id
        WHERE p.archived = 0 AND d.value LIKE '%' || :query || '%'
        ORDER BY p.name COLLATE NOCASE
        """
    )
    fun searchByDetails(query: String): Flow<List<Person>>

    @Query("SELECT COUNT(*) FROM people WHERE archived = 0")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM person_details WHERE personId = :personId ORDER BY position")
    fun observeDetails(personId: Long): Flow<List<PersonDetail>>

    @Upsert
    suspend fun upsert(person: Person): Long

    @Upsert
    suspend fun upsertAll(people: List<Person>)

    @Insert
    suspend fun insertDetails(details: List<PersonDetail>)

    /** A person and their opening details land together or not at all. */
    @Transaction
    suspend fun insertWithDetails(person: Person, details: List<PersonDetail>): Long {
        val id = upsert(person)

        if (details.isNotEmpty())
            insertDetails(details.map { it.copy(personId = id) })

        return id
    }

    @Delete
    suspend fun delete(person: Person)

    /** Cascades to the person's details and history — the whole record goes. */
    @Query("DELETE FROM people WHERE id = :id")
    suspend fun deleteById(id: Long)
}
