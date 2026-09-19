package bassamalim.tether

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import bassamalim.tether.core.di.ApplicationScope
import bassamalim.tether.core.nudge.NudgeScheduler
import bassamalim.tether.core.nudge.Nudges
import bassamalim.tether.core.reminder.ReminderScheduler
import bassamalim.tether.core.reminder.Reminders
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var nudges: Nudges
    @Inject lateinit var nudgeScheduler: NudgeScheduler
    @Inject lateinit var reminders: Reminders
    @Inject lateinit var reminderScheduler: ReminderScheduler
    @Inject @field:ApplicationScope lateinit var scope: CoroutineScope

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()

        nudges.createChannel()
        reminders.createChannel()

        // Re-books the nudge and every pending reminder on every launch, so they survive a
        // reboot or a force-stop.
        scope.launch {
            nudgeScheduler.sync()
            reminderScheduler.syncAll()
        }
    }
}
