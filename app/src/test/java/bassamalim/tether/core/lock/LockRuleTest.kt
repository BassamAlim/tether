package bassamalim.tether.core.lock

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LockRuleTest {

    @Test
    fun `a quick hop to another app and back does not re-prompt`() {
        assertFalse(shouldLock(enabled = true, backgroundedAt = 0, now = 10_000))
    }

    @Test
    fun `a minute away is a new session`() {
        assertTrue(shouldLock(enabled = true, backgroundedAt = 0, now = LockManager.GRACE_MILLIS))
        assertTrue(shouldLock(enabled = true, backgroundedAt = 0, now = 5 * 60_000))
    }

    @Test
    fun `an app that was never backgrounded stays open`() {
        assertFalse(shouldLock(enabled = true, backgroundedAt = null, now = 10 * 60_000))
    }

    @Test
    fun `the lock does nothing when it is switched off`() {
        assertFalse(shouldLock(enabled = false, backgroundedAt = 0, now = 10 * 60_000))
    }
}
