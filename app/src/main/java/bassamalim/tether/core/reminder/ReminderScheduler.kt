package bassamalim.tether.core.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import bassamalim.tether.core.data.dataSources.room.entities.Reminder
import bassamalim.tether.core.data.repositories.RemindersRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Books each reminder as its own one-time job, named after the person so that re-setting the
 * bell replaces the pending alarm rather than adding a second one.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val remindersRepository: RemindersRepository,
    private val clock: Clock
) {

    fun schedule(reminder: Reminder) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            workName(reminder.personId),
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayUntil(reminder.scheduledFor))
                .setInputData(workDataOf(Reminders.KEY_REMINDER_ID to reminder.id))
                .build()
        )
    }

    fun cancel(personId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(personId))
    }

    /**
     * Re-books everything still pending. WorkManager survives a reboot on its own; this is for
     * the cases it doesn't — a force-stop, a restore onto a new phone, a reminder whose time
     * passed while the phone was off, which arrives late rather than not at all.
     */
    suspend fun syncAll() {
        remindersRepository.getAll().forEach(::schedule)
    }

    /** Never negative: a reminder whose moment has passed is due now. */
    private fun delayUntil(scheduledFor: LocalDateTime): Duration =
        Duration.between(LocalDateTime.now(clock), scheduledFor).coerceAtLeast(Duration.ZERO)

    private fun workName(personId: Long) = "reminder_$personId"
}
