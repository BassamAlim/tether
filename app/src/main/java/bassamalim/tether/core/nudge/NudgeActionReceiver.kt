package bassamalim.tether.core.nudge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** "Tomorrow": dismissing shouldn't be the only way out of a nudge. */
@AndroidEntryPoint
class NudgeActionReceiver : BroadcastReceiver() {

    @Inject lateinit var nudges: Nudges
    @Inject lateinit var scheduler: NudgeScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Nudges.ACTION_SNOOZE) return

        nudges.cancel()
        scheduler.snooze()
    }
}
