package bassamalim.tether.core.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The reminders' own notification plumbing, kept apart from the weekly nudge's: you might want
 * the one you asked for by hand and not the standing one, and a separate channel is how Android
 * lets you say that.
 */
@Singleton
class Reminders @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "The reminders you set about one person."
        }

        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }

    fun canPost() = NotificationManagerCompat.from(context).areNotificationsEnabled()

    /**
     * One notification per reminder, so two that land the same morning don't overwrite each
     * other. Offset past the weekly nudge's id, which is 1.
     */
    fun notificationId(reminderId: Long): Int = (NOTIFICATION_ID_BASE + reminderId).toInt()

    companion object {
        const val CHANNEL_ID = "person_reminder"
        const val KEY_REMINDER_ID = "reminder_id"

        /** Set on the launch intent so tapping the reminder opens the person it was about. */
        const val EXTRA_OPEN_PERSON = "open_person"

        private const val NOTIFICATION_ID_BASE = 1000L
    }
}
