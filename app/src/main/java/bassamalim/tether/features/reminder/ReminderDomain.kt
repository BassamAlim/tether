package bassamalim.tether.features.reminder

import bassamalim.tether.core.data.dataSources.room.entities.Reminder
import bassamalim.tether.core.data.repositories.RemindersRepository
import bassamalim.tether.core.domain.TrackedPeople
import bassamalim.tether.core.enums.InteractionType
import bassamalim.tether.core.models.TrackedPerson
import bassamalim.tether.core.reminder.ReminderScheduler
import bassamalim.tether.core.reminder.Reminders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class ReminderDomain @Inject constructor(
    private val trackedPeople: TrackedPeople,
    private val remindersRepository: RemindersRepository,
    private val scheduler: ReminderScheduler,
    private val reminders: Reminders,
    private val clock: Clock
) {

    fun observePerson(id: Long): Flow<TrackedPerson?> = trackedPeople.observe(id)

    fun today(): LocalDate = trackedPeople.today()

    fun now(): LocalDateTime = LocalDateTime.now(clock)

    /** A reminder that can't post is a reminder that doesn't exist, so the screen says so. */
    fun canNotify(): Boolean = reminders.canPost()

    suspend fun current(personId: Long): Reminder? =
        remindersRepository.observeForPerson(personId).first()

    /**
     * Storing it and booking it are one act: a row with no alarm behind it would be a promise
     * the app doesn't keep.
     */
    suspend fun set(personId: Long, type: InteractionType?, at: LocalDateTime) {
        val id = remindersRepository.set(personId = personId, type = type, scheduledFor = at)

        scheduler.schedule(Reminder(id = id, personId = personId, type = type, scheduledFor = at))
    }

    suspend fun clear(personId: Long) {
        remindersRepository.clear(personId)
        scheduler.cancel(personId)
    }
}
