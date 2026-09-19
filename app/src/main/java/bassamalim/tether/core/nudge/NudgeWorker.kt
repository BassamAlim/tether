package bassamalim.tether.core.nudge

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
import bassamalim.tether.core.data.repositories.PreferencesRepository
import bassamalim.tether.core.domain.DueState
import bassamalim.tether.core.domain.TrackedPeople
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Sends the weekly nudge and books the next one. Weekly rather than daily on purpose: a nudge
 * that arrives every morning becomes wallpaper inside a fortnight.
 */
@HiltWorker
class NudgeWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val trackedPeople: TrackedPeople,
    private val preferencesRepository: PreferencesRepository,
    private val nudges: Nudges,
    private val scheduler: NudgeScheduler
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!preferencesRepository.observeNudgeEnabled().first()) return Result.success()

        val slipping = trackedPeople.observe().first()
            .mapNotNull { person ->
                (person.dueState as? DueState.Slipping)?.let {
                    SlippingPerson(
                        name = person.person.name,
                        daysOverdue = it.daysOverdue,
                        cadenceDays = person.person.cadenceDays
                    )
                }
            }
            .sortedByDescending { it.daysOverdue }

        // Sent even when nobody's slipping: "nobody's due" is still worth reading.
        notify(nudgeCopy(slipping))

        // Book the next one before finishing, so a missed week can't end the series.
        scheduler.sync()

        return Result.success()
    }

    private fun notify(copy: NudgeCopy) {
        if (!nudges.canPost()) return

        val notification = NotificationCompat.Builder(context, Nudges.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.accent))
            .setContentTitle(copy.title)
            .setContentText(copy.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(copy.body))
            .setContentIntent(openCatchUp())
            .addAction(0, "See who", openCatchUp())
            .addAction(0, "Tomorrow", snooze())
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(Nudges.NOTIFICATION_ID, notification)
    }

    private fun openCatchUp(): PendingIntent {
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)!!
            .putExtra(Nudges.EXTRA_OPEN_CATCH_UP, true)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        return PendingIntent.getActivity(
            context,
            REQUEST_OPEN,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun snooze(): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_SNOOZE,
        Intent(context, NudgeActionReceiver::class.java).setAction(Nudges.ACTION_SNOOZE),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private companion object {
        const val REQUEST_OPEN = 1
        const val REQUEST_SNOOZE = 2
    }
}
