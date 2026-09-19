package bassamalim.tether.core.data.dataSources.room.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import bassamalim.tether.core.data.dataSources.room.entities.RelationshipType
import kotlinx.coroutines.flow.Flow

@Dao
interface RelationshipTypesDao {

    @Query("SELECT * FROM relationship_types ORDER BY label COLLATE NOCASE")
    fun observeAll(): Flow<List<RelationshipType>>

    /** Typing one you already have is not a correction, so it's ignored rather than rewritten. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(type: RelationshipType)

    @Query("SELECT * FROM relationship_types ORDER BY label COLLATE NOCASE")
    suspend fun getAll(): List<RelationshipType>
}
