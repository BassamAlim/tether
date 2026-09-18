package bassamalim.tether.core.data.dataSources.room.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import bassamalim.tether.core.data.dataSources.room.entities.Person
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

    /** Name matches and note matches answer different questions, so they're queried apart. */
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
        SELECT * FROM people
        WHERE archived = 0 AND notes LIKE '%' || :query || '%'
        ORDER BY name COLLATE NOCASE
        """
    )
    fun searchByNotes(query: String): Flow<List<Person>>

    @Query("SELECT COUNT(*) FROM people WHERE archived = 0")
    fun observeCount(): Flow<Int>

    @Upsert
    suspend fun upsert(person: Person): Long

    @Upsert
    suspend fun upsertAll(people: List<Person>)

    @Delete
    suspend fun delete(person: Person)
}
