package bassamalim.tether.core.nudge

import org.junit.Assert.assertEquals
import org.junit.Test

class NudgeCopyTest {

    @Test
    fun `the nudge names the worst case and counts the rest`() {
        val copy = nudgeCopy(
            listOf(
                SlippingPerson("Maya Reyes", daysOverdue = 35, cadenceDays = 14),
                SlippingPerson("Theo Brandt", daysOverdue = 60, cadenceDays = 30),
                SlippingPerson("Nadia Farouk", daysOverdue = 3, cadenceDays = 14)
            )
        )

        assertEquals("3 people are slipping", copy.title)
        assertEquals(
            "Maya Reyes is 5 weeks past your 2-week check-in. " +
                    "Theo Brandt and Nadia Farouk are overdue too.",
            copy.body
        )
    }

    @Test
    fun `one person gets the singular, and no trailing clause`() {
        val copy = nudgeCopy(listOf(SlippingPerson("Sara Lund", daysOverdue = 7, cadenceDays = 7)))

        assertEquals("1 person is slipping", copy.title)
        // Spans under a fortnight stay in days: "7 days" is clearer than "1 week" here.
        assertEquals("Sara Lund is 7 days past your weekly check-in.", copy.body)
    }

    @Test
    fun `the day a check-in comes due reads as due, not as overdue by nothing`() {
        val copy = nudgeCopy(listOf(SlippingPerson("Jon Kwan", daysOverdue = 0, cadenceDays = 30)))

        assertEquals("Jon Kwan's monthly check-in is due today.", copy.body)
    }

    @Test
    fun `a long list names two and totals the others`() {
        val copy = nudgeCopy(
            (1..5).map { SlippingPerson("Person $it", daysOverdue = 10L, cadenceDays = 7) }
        )

        assertEquals("5 people are slipping", copy.title)
        assertEquals(
            "Person 1 is 10 days past your weekly check-in. " +
                    "Person 2, Person 3 and 2 others are overdue too.",
            copy.body
        )
    }

    @Test
    fun `a quiet week says so rather than inventing urgency`() {
        val copy = nudgeCopy(emptyList())

        assertEquals("Nobody's slipping", copy.title)
        assertEquals("Everyone you track has heard from you lately.", copy.body)
    }
}
