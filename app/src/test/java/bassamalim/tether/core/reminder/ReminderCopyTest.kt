package bassamalim.tether.core.reminder

import bassamalim.tether.core.enums.InteractionType
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderCopyTest {

    @Test
    fun `the type names what you meant to do`() {
        val copy = reminderCopy(
            name = "Maya Reyes",
            type = InteractionType.COFFEE,
            status = "Last talked 7 weeks ago"
        )

        assertEquals("Coffee with Maya Reyes", copy.title)
        assertEquals("Last talked 7 weeks ago", copy.body)
    }

    @Test
    fun `without a type it still says what it is for`() {
        val copy = reminderCopy(name = "Omar", type = null, status = "No catch-ups logged yet")

        assertEquals("Catch up with Omar", copy.title)
    }
}
