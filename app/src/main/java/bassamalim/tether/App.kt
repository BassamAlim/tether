package bassamalim.tether

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import bassamalim.tether.core.nudge.Nudges
import bassamalim.tether.core.reminder.Reminders
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var nudges: Nudges
    @Inject lateinit var reminders: Reminders

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()

        // The channels, and nothing else. Re-booking the nudge and the reminders belongs to
        // opening the app (see Activity): this runs in *every* process start, including the one
        // a worker itself woke up, and both schedulers enqueue with REPLACE.
        nudges.createChannel()
        reminders.createChannel()
    }
}
