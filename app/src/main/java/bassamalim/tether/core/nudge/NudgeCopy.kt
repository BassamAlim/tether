package bassamalim.tether.core.nudge

import bassamalim.tether.core.utils.cadenceAdjective
import bassamalim.tether.core.utils.durationLabel

/** What a weekly nudge says. Pure, so the copy can be tested without a notification manager. */
data class NudgeCopy(val title: String, val body: String)

/** One slipping person, as far as the notification is concerned. */
data class SlippingPerson(val name: String, val daysOverdue: Long, val cadenceDays: Int?)

/**
 * The nudge names people instead of counting them — it's the highest-leverage copy in the app,
 * because most weeks you meet Tether here rather than by opening it.
 */
fun nudgeCopy(slipping: List<SlippingPerson>): NudgeCopy {
    if (slipping.isEmpty()) {
        return NudgeCopy(
            title = "Nobody's slipping",
            body = "Everyone you track has heard from you lately."
        )
    }

    val worst = slipping.first()
    val others = slipping.drop(1).map { it.name }

    val title =
        if (slipping.size == 1) "1 person is slipping"
        else "${slipping.size} people are slipping"

    val cadence = cadenceAdjective(worst.cadenceDays)

    val lead =
        if (worst.daysOverdue == 0L) "${worst.name}'s $cadence check-in is due today."
        else "${worst.name} is ${durationLabel(worst.daysOverdue)} past your $cadence check-in."

    return NudgeCopy(title = title, body = (lead + othersClause(others)).trim())
}

private fun othersClause(others: List<String>): String = when (others.size) {
    0 -> ""
    1 -> " ${others[0]} is overdue too."
    2 -> " ${others[0]} and ${others[1]} are overdue too."
    else -> {
        val remaining = others.size - 2
        val rest = if (remaining == 1) "1 other" else "$remaining others"
        " ${others[0]}, ${others[1]} and $rest are overdue too."
    }
}
