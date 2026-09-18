package bassamalim.tether.core.nudge

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import bassamalim.tether.core.data.repositories.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules the nudge as one-time work that re-schedules itself after each run, rather than
 * periodic work: a weekly job that drifts by a flex window stops landing at the hour you asked
 * for, and the whole point is that it arrives when you said.
 */
@Singleton
class NudgeScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository,
    private val clock: Clock
) {

    /** Enqueues the next nudge, or cancels it if the setting is off. */
    suspend fun sync() {
        if (!preferencesRepository.observeNudgeEnabled().first()) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            return
        }

        val day = preferencesRepository.observeNudgeDay().first()
        val time = preferencesRepository.observeNudgeTime().first()
        val now = LocalDateTime.now(clock)

        val next = now.with(TemporalAdjusters.nextOrSame(day))
            .withHour(time.hour)
            .withMinute(time.minute)
            .withSecond(0)
            .withNano(0)
            .let { if (it.isAfter(now)) it else it.plusWeeks(1) }

        enqueue(Duration.between(now, next))
    }

    /** "Tomorrow" on the notification: same nudge, 24 hours later. */
    fun snooze() = enqueue(Duration.ofDays(1))

    private fun enqueue(delay: Duration) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<NudgeWorker>()
                .setInitialDelay(delay)
                .build()
        )
    }

    private companion object {
        const val WORK_NAME = "weekly_nudge"
    }
}
