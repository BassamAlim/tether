package bassamalim.tether.core.data.dataSources.room.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import kotlinx.coroutines.flow.Flow

@Dao
interface InteractionsDao {

    @Query("SELECT * FROM interactions WHERE personId = :personId ORDER BY occurredOn DESC, id DESC")
    fun observeForPerson(personId: Long): Flow<List<Interaction>>

    /** What you wrote and where you were: both are things you'd search for years later. */
    @Query(
        """
        SELECT * FROM interactions
        WHERE note LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%'
        ORDER BY occurredOn DESC, id DESC
        """
    )
    fun search(query: String): Flow<List<Interaction>>

    @Query("SELECT * FROM interactions ORDER BY personId, occurredOn")
    suspend fun getAll(): List<Interaction>

    @Insert
    suspend fun insert(interaction: Interaction): Long

    /** Corrections: the row keeps its id, so the timeline doesn't gain an entry. */
    @Update
    suspend fun update(interaction: Interaction)

    /** Only ever used to undo a one-tap log. */
    @Delete
    suspend fun delete(interaction: Interaction)

    @Query("SELECT * FROM interactions WHERE id = :id")
    suspend fun get(id: Long): Interaction?
}
