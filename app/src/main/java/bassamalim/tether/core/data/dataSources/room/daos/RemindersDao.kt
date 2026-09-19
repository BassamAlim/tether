package bassamalim.tether.core.data.dataSources.room.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import bassamalim.tether.core.data.dataSources.room.entities.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface RemindersDao {

    /** Null when the bell isn't set: there is at most one pending reminder per person. */
    @Query("SELECT * FROM reminders WHERE personId = :personId")
    fun observeForPerson(personId: Long): Flow<Reminder?>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun get(id: Long): Reminder?

    /** Every pending reminder, for re-booking the alarms after a reboot or a force-stop. */
    @Query("SELECT * FROM reminders ORDER BY scheduledFor")
    suspend fun getAll(): List<Reminder>

    @Insert
    suspend fun insert(reminder: Reminder): Long

    @Query("DELETE FROM reminders WHERE personId = :personId")
    suspend fun deleteForPerson(personId: Long)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun delete(id: Long)
}
