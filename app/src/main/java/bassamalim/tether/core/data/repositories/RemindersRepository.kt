package bassamalim.tether.core.data.repositories

import bassamalim.tether.core.data.dataSources.room.daos.RemindersDao
import bassamalim.tether.core.data.dataSources.room.entities.Reminder
import bassamalim.tether.core.enums.InteractionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemindersRepository @Inject constructor(
    private val remindersDao: RemindersDao
) {

    fun observeForPerson(personId: Long): Flow<Reminder?> = remindersDao.observeForPerson(personId)

    suspend fun get(id: Long): Reminder? = remindersDao.get(id)

    suspend fun getAll(): List<Reminder> = remindersDao.getAll()

    /**
     * Replaces whatever that person's bell was set to and returns the new row's id, which is
     * what the alarm is booked against.
     */
    suspend fun set(
        personId: Long,
        type: InteractionType?,
        scheduledFor: LocalDateTime
    ): Long {
        remindersDao.deleteForPerson(personId)

        return remindersDao.insert(
            Reminder(personId = personId, type = type, scheduledFor = scheduledFor)
        )
    }

    suspend fun clear(personId: Long) = remindersDao.deleteForPerson(personId)

    /** Once it has been posted it has done its job. */
    suspend fun delete(id: Long) = remindersDao.delete(id)
}
