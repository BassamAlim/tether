package bassamalim.tether.core.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DueStateTest {

    private val addedOn = LocalDate.of(2026, 1, 1)

    @Test
    fun `a cadence of never is not tracked`() {
        val state = dueState(
            cadenceDays = null,
            lastInteractionOn = LocalDate.of(2020, 1, 1),
            addedOn = addedOn,
            today = LocalDate.of(2026, 9, 19)
        )

        assertEquals(DueState.NotTracked, state)
    }

    @Test
    fun `inside the cadence, it counts down to the due date`() {
        val state = dueState(
            cadenceDays = 14,
            lastInteractionOn = LocalDate.of(2026, 9, 15),
            addedOn = addedOn,
            today = LocalDate.of(2026, 9, 19)
        )

        assertEquals(DueState.InTouch(daysUntilDue = 10), state)
    }

    @Test
    fun `it comes due on the day the cadence elapses`() {
        val state = dueState(
            cadenceDays = 14,
            lastInteractionOn = LocalDate.of(2026, 9, 5),
            addedOn = addedOn,
            today = LocalDate.of(2026, 9, 19)
        )

        assertEquals(DueState.Slipping(daysOverdue = 0), state)
    }

    @Test
    fun `overdue is one distance, not an accumulating debt`() {
        // Seven weeks into a two-week cadence is five weeks overdue, not three missed check-ins.
        val state = dueState(
            cadenceDays = 14,
            lastInteractionOn = LocalDate.of(2026, 8, 1),
            addedOn = addedOn,
            today = LocalDate.of(2026, 9, 19)
        )

        assertEquals(DueState.Slipping(daysOverdue = 35), state)
    }

    @Test
    fun `someone with no interactions counts from the day they were added`() {
        val state = dueState(
            cadenceDays = 7,
            lastInteractionOn = null,
            addedOn = LocalDate.of(2026, 9, 17),
            today = LocalDate.of(2026, 9, 19)
        )

        assertEquals(DueState.InTouch(daysUntilDue = 5), state)
    }
}
