package bassamalim.tether.features.circle

import bassamalim.tether.core.data.dataSources.room.entities.Connection
import bassamalim.tether.core.data.dataSources.room.entities.Person
import bassamalim.tether.core.domain.DueState
import bassamalim.tether.core.models.TrackedPerson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import kotlin.math.hypot

class CircleLayoutTest {

    private fun tracked(id: Long, tag: String?, state: DueState, cadence: Int? = 14) = TrackedPerson(
        person = Person(
            id = id,
            name = "Person $id",
            tag = tag,
            cadenceDays = if (state is DueState.NotTracked) null else cadence,
            addedOn = LocalDate.of(2026, 1, 1)
        ),
        lastInteractionOn = null,
        dueState = state
    )

    private fun OrbitPoint.radius() = hypot(x, y)

    @Test
    fun `the due ring separates in touch from slipping, and the untracked sit outside both`() {
        val people = (1L..40L).map {
            val orbit = Orbit.entries[(it % 3).toInt()]
            OrbitInput(it, orbit, reach = (it % 5) / 4f)
        }
        val layout = layOutOrbits(people)
        val orbitOf = people.associate { it.id to it.orbit }

        val slippingInner = layout.points.filter { orbitOf[it.id] == Orbit.SLIPPING }.minOf { it.radius() }
        val untrackedInner = layout.points.filter { orbitOf[it.id] == Orbit.UNTRACKED }.minOf { it.radius() }

        layout.points.filter { orbitOf[it.id] == Orbit.IN_TOUCH }.forEach {
            assertTrue(it.radius() < layout.dueRadius)
        }
        assertTrue(slippingInner > layout.dueRadius)
        assertTrue(untrackedInner > layout.points.filter { orbitOf[it.id] == Orbit.SLIPPING }.maxOf { it.radius() })
        assertTrue(layout.points.all { it.radius() <= layout.extent + 1e-3f })
    }

    @Test
    fun `further through the cadence sits further out`() {
        val layout = layOutOrbits(
            listOf(
                OrbitInput(1, Orbit.IN_TOUCH, reach = 0.1f),
                OrbitInput(2, Orbit.IN_TOUCH, reach = 0.9f)
            )
        )

        assertTrue(layout.points[0].radius() < layout.points[1].radius())
    }

    @Test
    fun `nobody lands on top of anybody`() {
        // Everyone on the same spot of the same ring: the worst case relaxation has to undo.
        val layout = layOutOrbits((1L..30L).map { OrbitInput(it, Orbit.IN_TOUCH, reach = 0f) })

        for (a in layout.points) for (b in layout.points) {
            if (a.id < b.id) assertTrue(hypot(a.x - b.x, a.y - b.y) > MIN_GAP * 0.9f)
        }
    }

    @Test
    fun `the same people always land in the same places`() {
        val people = (1L..20L).map { OrbitInput(it, Orbit.SLIPPING, reach = it / 20f) }

        assertEquals(layOutOrbits(people), layOutOrbits(people))
    }

    @Test
    fun `reach is how much of the cadence has gone by, or how many cadences overdue`() {
        assertEquals(0.75f, tracked(1, null, DueState.InTouch(daysUntilDue = 7), cadence = 28).toOrbitInput().reach)
        assertEquals(0.5f, tracked(1, null, DueState.Slipping(daysOverdue = 7), cadence = 14).toOrbitInput().reach)
        assertEquals(Orbit.UNTRACKED, tracked(1, null, DueState.NotTracked).toOrbitInput().orbit)
    }

    @Test
    fun `the five most carried relationships get hues and the rest share other`() {
        val tags = listOf("Family", "Family", "Work", "Work", "School", "Friend", "Gym", "Neighbours", null)
        val people = tags.mapIndexed { i, tag -> tracked(i + 1L, tag, DueState.InTouch(3)) }

        val graph = buildGraph(people, emptyList())
        val slotOf = graph.people.associate { it.tracked.person.tag to it.slot }

        assertEquals(HueSlot.ONE, slotOf["Family"])
        assertEquals(HueSlot.TWO, slotOf["Work"])
        assertEquals(HueSlot.NONE, slotOf[null])
        assertEquals(
            listOf("Family", "Work", "Friend", "Gym", "Neighbours", "Other", "No relationship"),
            graph.groups.map { it.label }
        )
        assertEquals(HueSlot.OTHER, slotOf["School"])
    }

    @Test
    fun `links to people not on the graph are dropped`() {
        val people = listOf(tracked(1, null, DueState.InTouch(3)), tracked(2, null, DueState.InTouch(3)))

        val graph = buildGraph(people, listOf(Connection.between(1, 2), Connection.between(1, 99)))

        assertEquals(listOf(1L to 2L), graph.links)
    }
}
