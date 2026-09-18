package bassamalim.tether.core.data.dataSources.room.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import kotlinx.coroutines.flow.Flow

@Dao
interface InteractionsDao {

    @Query("SELECT * FROM interactions WHERE personId = :personId ORDER BY occurredOn DESC, id DESC")
    fun observeForPerson(personId: Long): Flow<List<Interaction>>

    @Query(
        """
        SELECT * FROM interactions
        WHERE note LIKE '%' || :query || '%'
        ORDER BY occurredOn DESC, id DESC
        """
    )
    fun searchNotes(query: String): Flow<List<Interaction>>

    @Insert
    suspend fun insert(interaction: Interaction): Long

    /** Only ever used to undo a one-tap log. */
    @Delete
    suspend fun delete(interaction: Interaction)

    @Query("SELECT * FROM interactions WHERE id = :id")
    suspend fun get(id: Long): Interaction?
}
