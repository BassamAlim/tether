package bassamalim.tether.features.circle

import bassamalim.tether.core.data.dataSources.room.entities.Connection
import bassamalim.tether.core.data.repositories.ConnectionsRepository
import bassamalim.tether.core.di.DefaultDispatcher
import bassamalim.tether.core.domain.DueState
import bassamalim.tether.core.domain.RelationshipInUse
import bassamalim.tether.core.domain.TrackedPeople
import bassamalim.tether.core.domain.relationshipKey
import bassamalim.tether.core.domain.relationshipsInUse
import bassamalim.tether.core.models.TrackedPerson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDate
import javax.inject.Inject

/**
 * The colour a person is drawn in. The vocabulary is open-ended but a palette isn't — past
 * eight hues, dots stop being tellable apart — so the eight relationships most people carry get
 * a hue each and the rest share a neutral OTHER. NONE is someone whose relationship is unsaid.
 * The hued slots are `RelationshipHues` in order, so the two lists are the same length.
 */
enum class HueSlot { ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, OTHER, NONE }

/** One colour group, for the legend, with how many people are drawn in it. */
data class HueGroup(val slot: HueSlot, val label: String, val count: Int)

data class PlacedPerson(val tracked: TrackedPerson, val x: Float, val y: Float, val slot: HueSlot)

data class CircleGraph(
    val people: List<PlacedPerson>,
    /** Pairs of ids, both ends among [people]. */
    val links: List<Pair<Long, Long>>,
    val groups: List<HueGroup>,
    val dueRadius: Float,
    val extent: Float
)

/**
 * You in the middle and everyone around you, as far out as they've drifted. Distance is the
 * same cadence maths as Catch up (`DueState`), so the picture and the list never disagree.
 */
class CircleDomain @Inject constructor(
    private val trackedPeople: TrackedPeople,
    private val connectionsRepository: ConnectionsRepository,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    /** The layout is quadratic in headcount, so it stays off the main thread. */
    fun observeGraph(): Flow<CircleGraph> = combine(
        trackedPeople.observe(),
        connectionsRepository.observeAll(),
        ::buildGraph
    ).flowOn(defaultDispatcher)

    fun today(): LocalDate = trackedPeople.today()
}

internal fun buildGraph(people: List<TrackedPerson>, connections: List<Connection>): CircleGraph {
    val relationships = relationshipsInUse(people.map { it.person.tag })
    val slotByKey = hueSlots(relationships)

    fun slotOf(person: TrackedPerson) =
        relationshipKey(person.person.tag)?.let { slotByKey[it] } ?: HueSlot.NONE

    // Grouped by colour, so each relationship gathers into its own wedge of the circle.
    val ordered = people.sortedWith(
        compareBy<TrackedPerson> { slotOf(it).ordinal }.thenBy { it.person.name.lowercase() }
    )

    val layout = layOutOrbits(ordered.map { it.toOrbitInput() })
    val placed = ordered.zip(layout.points) { person, point ->
        PlacedPerson(person, point.x, point.y, slotOf(person))
    }

    val ids = people.mapTo(HashSet()) { it.person.id }

    return CircleGraph(
        people = placed,
        // The people query leaves archived rows out; their links go with them.
        links = connections
            .filter { it.personAId in ids && it.personBId in ids }
            .map { it.personAId to it.personBId },
        groups = hueGroups(relationships, placed),
        dueRadius = layout.dueRadius,
        extent = layout.extent
    )
}

/** The eight most-carried relationships get a hue each, in legend order; the rest are OTHER. */
internal fun hueSlots(relationships: List<RelationshipInUse>): Map<String, HueSlot> =
    relationships.mapIndexed { i, relationship ->
        relationship.key to (HUED.getOrNull(i) ?: HueSlot.OTHER)
    }.toMap()

private fun hueGroups(relationships: List<RelationshipInUse>, placed: List<PlacedPerson>) =
    buildList {
        relationships.take(HUED.size).forEachIndexed { i, it ->
            add(HueGroup(HUED[i], it.label, it.count))
        }
        val other = placed.count { it.slot == HueSlot.OTHER }
        if (other > 0) add(HueGroup(HueSlot.OTHER, "Other", other))
        val none = placed.count { it.slot == HueSlot.NONE }
        if (none > 0) add(HueGroup(HueSlot.NONE, "No relationship", none))
    }

internal fun TrackedPerson.toOrbitInput(): OrbitInput {
    val cadence = person.cadenceDays?.toFloat()
    return when (val state = dueState) {
        DueState.NotTracked -> OrbitInput(person.id, Orbit.UNTRACKED, reach = 0.5f)
        // How much of the cadence has gone by.
        is DueState.InTouch -> OrbitInput(
            person.id, Orbit.IN_TOUCH, reach = 1f - state.daysUntilDue / (cadence ?: 1f)
        )
        // How many cadences past due; a whole one or more is the rim.
        is DueState.Slipping -> OrbitInput(
            person.id, Orbit.SLIPPING, reach = state.daysOverdue / (cadence ?: 1f)
        )
    }
}

private val HUED = HueSlot.entries - HueSlot.OTHER - HueSlot.NONE
