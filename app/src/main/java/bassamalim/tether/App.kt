package bassamalim.tether

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import bassamalim.tether.core.di.ApplicationScope
import bassamalim.tether.core.nudge.NudgeScheduler
import bassamalim.tether.core.nudge.Nudges
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var nudges: Nudges
    @Inject lateinit var nudgeScheduler: NudgeScheduler
    @Inject @field:ApplicationScope lateinit var scope: CoroutineScope

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()

        nudges.createChannel()

        // Re-books the nudge on every launch, so it survives a reboot or a force-stop.
        scope.launch { nudgeScheduler.sync() }
    }
}
