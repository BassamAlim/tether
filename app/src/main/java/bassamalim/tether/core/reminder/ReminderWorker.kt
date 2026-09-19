package bassamalim.tether.core.reminder

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import bassamalim.tether.R
import bassamalim.tether.core.data.repositories.RemindersRepository
import bassamalim.tether.core.domain.DueState
import bassamalim.tether.core.domain.TrackedPeople
import bassamalim.tether.core.utils.lastTalkedStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Posts one reminder and forgets it. A reminder is spent when it arrives: the row goes, so the
 * bell on Person detail is empty again and you're never quietly re-reminded.
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val remindersRepository: RemindersRepository,
    private val trackedPeople: TrackedPeople,
    private val reminders: Reminders
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val reminderId = inputData.getLong(Reminders.KEY_REMINDER_ID, 0)

        // Cleared or replaced while this was pending, or the person was deleted: nothing to say.
        val reminder = remindersRepository.get(reminderId) ?: return Result.success()
        val person = trackedPeople.observe(reminder.personId).first() ?: return Result.success()

        val copy = reminderCopy(
            name = person.person.name,
            type = reminder.type,
            status = lastTalkedStatus(
                lastInteractionOn = person.lastInteractionOn,
                daysOverdue = (person.dueState as? DueState.Slipping)?.daysOverdue,
                today = trackedPeople.today()
            )
        )

        notify(reminderId, reminder.personId, copy)
        remindersRepository.delete(reminderId)

        return Result.success()
    }

    private fun notify(reminderId: Long, personId: Long, copy: ReminderCopy) {
        if (!reminders.canPost()) return

        val notification = NotificationCompat.Builder(context, Reminders.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.accent))
            .setContentTitle(copy.title)
            .setContentText(copy.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(copy.body))
            .setContentIntent(openPerson(personId))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            reminders.notificationId(reminderId),
            notification
        )
    }

    /** Opens the app on them — through the lock if one is set, never around it. */
    private fun openPerson(personId: Long): PendingIntent {
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)!!
            .putExtra(Reminders.EXTRA_OPEN_PERSON, personId)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        return PendingIntent.getActivity(
            context,
            personId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
