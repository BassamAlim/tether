package bassamalim.tether.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhoneNumbersTest {

    @Test
    fun `international numbers lose their punctuation`() {
        assertEquals("966501234412", internationalDigits("+966 50 123 4412"))
        assertEquals("447700900123", internationalDigits("+44 (0)7700 900123"))
    }

    @Test
    fun `00 is the other way of writing plus`() {
        assertEquals("966501234412", internationalDigits("00966501234412"))
    }

    @Test
    fun `a national number is not guessed at`() {
        // No country in it, and inventing one would open a chat with a stranger.
        assertNull(internationalDigits("050 123 4412"))
    }

    @Test
    fun `nothing useful comes back from nothing`() {
        assertNull(internationalDigits(null))
        assertNull(internationalDigits(""))
        assertNull(internationalDigits("+12"))
    }
}
