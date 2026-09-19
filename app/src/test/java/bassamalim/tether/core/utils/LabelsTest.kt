package bassamalim.tether.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class LabelsTest {

    @Test
    fun `an overdue reason names the span and the cadence`() {
        assertEquals(
            "5 weeks past your 2-week check-in",
            overdueReason(daysOverdue = 35, cadenceDays = 14)
        )
        assertEquals(
            "2 months past your monthly check-in",
            overdueReason(daysOverdue = 62, cadenceDays = 30)
        )
    }

    @Test
    fun `the day a check-in comes due reads as due, not as overdue by zero`() {
        assertEquals(
            "Your weekly check-in is due today",
            overdueReason(daysOverdue = 0, cadenceDays = 7)
        )
    }

    @Test
    fun `upcoming check-ins count down in days`() {
        assertEquals("Due today", dueReason(0))
        assertEquals("Due tomorrow", dueReason(1))
        assertEquals("Due in 3 days", dueReason(3))
    }

    @Test
    fun `cadence labels read as english`() {
        assertEquals("every week", cadenceLabel(7))
        assertEquals("every 2 weeks", cadenceLabel(14))
        assertEquals("every month", cadenceLabel(30))
        assertEquals("Never", cadenceLabel(null))
    }

    @Test
    fun `the status line pairs when you last talked with how overdue that leaves you`() {
        assertEquals(
            "Last talked 7 weeks ago, 5 weeks overdue",
            lastTalkedStatus(
                lastInteractionOn = LocalDate.of(2026, 8, 1),
                daysOverdue = 35,
                today = LocalDate.of(2026, 9, 19)
            )
        )
    }

    @Test
    fun `someone in touch gets no overdue clause, and someone new gets no line at all`() {
        assertEquals(
            "Last talked yesterday",
            lastTalkedStatus(
                lastInteractionOn = LocalDate.of(2026, 9, 18),
                daysOverdue = null,
                today = LocalDate.of(2026, 9, 19)
            )
        )
        assertEquals(
            "No catch-ups logged yet",
            lastTalkedStatus(null, null, LocalDate.of(2026, 9, 19))
        )
    }

    @Test
    fun `cadence reads as a settings value, not as a phrase`() {
        assertEquals("Weekly", cadenceValueLabel(7))
        assertEquals("Monthly", cadenceValueLabel(30))
        assertEquals("Every 2 weeks", cadenceValueLabel(14))
        assertEquals("Never", cadenceValueLabel(null))
    }

    @Test
    fun `initials take the first two words`() {
        assertEquals("MR", initials("Maya Reyes"))
        assertEquals("O", initials("Omar"))
        assertEquals("?", initials("   "))
    }
}
