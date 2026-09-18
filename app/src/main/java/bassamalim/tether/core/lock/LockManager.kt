package bassamalim.tether.core.lock

import bassamalim.tether.core.data.repositories.PreferencesRepository
import bassamalim.tether.core.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * When the app should ask who you are.
 *
 * Cold start always asks. After that it asks again only once you've been away for
 * [GRACE_MILLIS] — a quick hop to a messaging app and back doesn't re-prompt, so texting someone
 * and then logging it stays one flow.
 */
@Singleton
class LockManager @Inject constructor(
    preferencesRepository: PreferencesRepository,
    @ApplicationScope scope: CoroutineScope
) {

    private val lockEnabled = preferencesRepository.observeLockEnabled()
        .stateIn(scope, SharingStarted.Eagerly, false)

    /** Monotonic time (not wall clock) so changing the device clock can't skip the grace. */
    private var backgroundedAt: Long? = null
    private var unlocked = false

    val isEnabled get() = lockEnabled.value

    fun onUnlocked() {
        unlocked = true
        backgroundedAt = null
    }

    fun onBackgrounded(now: Long) {
        if (unlocked) backgroundedAt = now
    }

    fun onLocked() {
        unlocked = false
        backgroundedAt = null
    }

    fun shouldLockOnResume(now: Long): Boolean =
        shouldLock(enabled = lockEnabled.value, backgroundedAt = backgroundedAt, now = now)

    companion object {
        /** A minute away is a different session; ten seconds is the same one. */
        const val GRACE_MILLIS = 60_000L
    }
}

/** Pulled out of [LockManager] so the rule itself can be tested without a DataStore. */
internal fun shouldLock(enabled: Boolean, backgroundedAt: Long?, now: Long): Boolean {
    if (!enabled) return false

    val since = backgroundedAt ?: return false
    return now - since >= LockManager.GRACE_MILLIS
}
