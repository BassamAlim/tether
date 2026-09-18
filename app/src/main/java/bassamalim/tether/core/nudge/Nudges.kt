package bassamalim.tether.core.nudge

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Notification plumbing kept in one place: the channel, the ids, and the intent extras. */
@Singleton
class Nudges @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Weekly nudge",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Once a week, who you're losing touch with."
        }

        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }

    fun canPost() = NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun cancel() = NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)

    companion object {
        const val CHANNEL_ID = "weekly_nudge"
        const val NOTIFICATION_ID = 1

        /** Set on the launch intent so the app opens on Catch up rather than People. */
        const val EXTRA_OPEN_CATCH_UP = "open_catch_up"
        const val ACTION_SNOOZE = "bassamalim.tether.SNOOZE_NUDGE"
    }
}
