package bassamalim.tether.features.circle

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Which ring someone sits on. The rings never overlap, so where a dot lands always tells the
 * truth: inside the dashed due ring you're in touch, outside it you're slipping, and the
 * untracked sit apart on the rim — they have no clock to be early or late against.
 */
enum class Orbit { IN_TOUCH, SLIPPING, UNTRACKED }

/**
 * One person going in. [reach] is how far through their ring they are, 0 to 1: for someone in
 * touch it's how much of the cadence has gone by, for someone slipping how many cadences
 * overdue (one or more is the rim). People are spread around the circle in list order, so the
 * caller sorts them — by relationship, so the colours gather into wedges.
 */
data class OrbitInput(val id: Long, val orbit: Orbit, val reach: Float)

data class OrbitPoint(val id: Long, val x: Float, val y: Float)

/** [dueRadius] is the dashed ring; [extent] the furthest a node's centre can be. */
data class OrbitLayout(val points: List<OrbitPoint>, val dueRadius: Float, val extent: Float)

/**
 * Places everyone around you, in units of one node's diameter with you at the origin, so the
 * screen only has to multiply. Pure and deterministic: the same people always land in the same
 * places, and nothing jumps when you come back to the tab.
 *
 * Each person starts on their ring at an even angle, then a few rounds of relaxation push apart
 * any two dots closer than [MIN_GAP] — a dot and a sliver of air — without ever letting one
 * leave its ring. The rings widen with the square root of the headcount, so the area grows with
 * the number of people and a large circle stays as legible as a small one.
 */
fun layOutOrbits(people: List<OrbitInput>): OrbitLayout {
    val width = max(1f, sqrt(people.size.toFloat()) / 3f) * RING_WIDTH

    val inTouch = YOU_CLEARANCE..(YOU_CLEARANCE + width)
    val dueRadius = inTouch.endInclusive + RING_GAP
    val slipping = (dueRadius + RING_GAP)..(dueRadius + RING_GAP + width)
    val untracked = (slipping.endInclusive + RING_GAP * 2)..(slipping.endInclusive + RING_GAP * 2 + width / 2)

    fun ringOf(orbit: Orbit) = when (orbit) {
        Orbit.IN_TOUCH -> inTouch
        Orbit.SLIPPING -> slipping
        Orbit.UNTRACKED -> untracked
    }

    val xs = FloatArray(people.size)
    val ys = FloatArray(people.size)
    people.forEachIndexed { i, person ->
        val ring = ringOf(person.orbit)
        val radius = ring.start + person.reach.coerceIn(0f, 1f) * (ring.endInclusive - ring.start)
        // From twelve o'clock, clockwise, as a watch face reads.
        val angle = 2 * PI * i / people.size - PI / 2
        xs[i] = (radius * cos(angle)).toFloat()
        ys[i] = (radius * sin(angle)).toFloat()
    }

    repeat(RELAXATION_ROUNDS) {
        for (i in people.indices) for (j in i + 1 until people.size) {
            var dx = xs[j] - xs[i]
            var dy = ys[j] - ys[i]
            var distance = sqrt(dx * dx + dy * dy)
            if (distance >= MIN_GAP) continue
            if (distance < 1e-4f) {
                // Stacked exactly: part them sideways along the ring rather than not at all.
                val angle = atan2(ys[i], xs[i]) + PI.toFloat() / 2
                dx = cos(angle); dy = sin(angle); distance = 1f
            }
            val push = (MIN_GAP - distance) / 2 / distance
            xs[i] -= dx * push; ys[i] -= dy * push
            xs[j] += dx * push; ys[j] += dy * push
        }
        // Back inside their own ring, keeping the angle, so relaxation never moves someone from
        // in touch to slipping.
        people.forEachIndexed { i, person ->
            val ring = ringOf(person.orbit)
            val radius = sqrt(xs[i] * xs[i] + ys[i] * ys[i])
            val clamped = radius.coerceIn(ring.start, ring.endInclusive)
            if (radius > 1e-4f && clamped != radius) {
                xs[i] *= clamped / radius
                ys[i] *= clamped / radius
            }
        }
    }

    val extent = if (people.any { it.orbit == Orbit.UNTRACKED }) untracked.endInclusive
    else slipping.endInclusive

    return OrbitLayout(
        points = people.mapIndexed { i, person -> OrbitPoint(person.id, xs[i], ys[i]) },
        dueRadius = dueRadius,
        extent = extent
    )
}

/** Room for your own node, plus a little air. */
private const val YOU_CLEARANCE = 2f
/** A ring's depth at nine people or fewer; it grows with the square root beyond that. */
private const val RING_WIDTH = 3f
/** Air either side of the due ring, so no dot sits on the line and reads as both. */
private const val RING_GAP = 0.8f
/** One dot and a sliver of air; the name is inside it, so there's nothing beneath to clear. */
internal const val MIN_GAP = 1.15f
private const val RELAXATION_ROUNDS = 120
