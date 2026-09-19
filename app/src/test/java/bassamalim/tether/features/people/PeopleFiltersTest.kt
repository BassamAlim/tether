package bassamalim.tether.features.people

import bassamalim.tether.core.data.dataSources.room.entities.Person
import bassamalim.tether.core.domain.DueState
import bassamalim.tether.core.models.TrackedPerson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PeopleFiltersTest {

    private fun person(tag: String?, tracked: Boolean = true) = TrackedPerson(
        person = Person(name = "Someone", tag = tag, cadenceDays = if (tracked) 14 else null, addedOn = LocalDate.of(2026, 1, 1)),
        lastInteractionOn = null,
        dueState = if (tracked) DueState.InTouch(daysUntilDue = 3) else DueState.NotTracked
    )

    @Test
    fun `relationships in use come after All, most people first`() {
        val labels = filterOptions(
            listOf(person("Work"), person("Family"), person("Family"), person(null))
        ).map { it.label }

        assertEquals(listOf("All", "Family · 2", "Work · 1"), labels)
    }

    @Test
    fun `untracked appears only when someone is`() {
        assertFalse(filterOptions(listOf(person("Work"))).any { it.filter == PeopleFilter.Untracked })

        val options = filterOptions(listOf(person("Work"), person(null, tracked = false)))
        assertEquals(FilterOption(PeopleFilter.Untracked, "Untracked · 1"), options[1])
    }

    @Test
    fun `spellings of one relationship share a chip under the commoner one`() {
        val options = filterOptions(listOf(person("work"), person("Work"), person("Work ")))

        assertEquals(listOf("All", "Work · 3"), options.map { it.label })
        assertTrue(options.drop(1).all { opt -> listOf(person("work"), person("WORK")).all { it.matches(opt.filter) } })
    }

    @Test
    fun `untracked matches only cadence never`() {
        assertTrue(person(null, tracked = false).matches(PeopleFilter.Untracked))
        assertFalse(person(null).matches(PeopleFilter.Untracked))
    }
}
