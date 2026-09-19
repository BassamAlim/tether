package bassamalim.tether.core.data.dataSources.room.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import bassamalim.tether.core.data.dataSources.room.entities.Connection
import bassamalim.tether.core.data.dataSources.room.relations.ConnectedPerson
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionsDao {

    /**
     * Both columns are searched and the far end is picked in the CASE, so the caller never has
     * to know which side of the pair their person landed on.
     */
    @Query(
        """
        SELECT p.*, c.id AS connectionId, c.label AS label
        FROM connections c
        JOIN people p
            ON p.id = CASE WHEN c.personAId = :personId THEN c.personBId ELSE c.personAId END
        WHERE (c.personAId = :personId OR c.personBId = :personId) AND p.archived = 0
        ORDER BY p.name COLLATE NOCASE
        """
    )
    fun observeFor(personId: Long): Flow<List<ConnectedPerson>>

    /** Who this person is already connected to, so the picker can leave them out. */
    @Query(
        """
        SELECT CASE WHEN c.personAId = :personId THEN c.personBId ELSE c.personAId END AS otherId
        FROM connections c
        WHERE c.personAId = :personId OR c.personBId = :personId
        """
    )
    fun observeConnectedIds(personId: Long): Flow<List<Long>>

    /**
     * One transaction, so a batch of links either all land or none do.
     *
     * IGNORE rather than REPLACE: the unique pair index already means a second attempt is the
     * same edge, and replacing would hand it a new id for no gain.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(connections: List<Connection>)

    @Query("UPDATE connections SET label = :label WHERE id = :id")
    suspend fun updateLabel(id: Long, label: String)

    @Query("DELETE FROM connections WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM connections ORDER BY personAId, personBId")
    suspend fun getAll(): List<Connection>

    /** Every edge at once, for Circle, which draws who knows who across everyone. */
    @Query("SELECT * FROM connections")
    fun observeAll(): Flow<List<Connection>>
}
