package bassamalim.tether.core.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Where a person stands against their cadence.
 *
 * Overdue is a single distance, never an accumulating debt: seven weeks into a two-week cadence
 * is "5 weeks overdue", not "three missed check-ins".
 */
sealed interface DueState {

    /** Cadence is "Never" — the person is out of Catch up and out of the slipping section. */
    data object NotTracked : DueState

    /** Still inside the cadence. */
    data class InTouch(val daysUntilDue: Long) : DueState

    /** Due now, and [daysOverdue] past the cadence (0 on the day it comes due). */
    data class Slipping(val daysOverdue: Long) : DueState
}

/**
 * Someone is due when (today − last contact) >= their cadence. "Last contact" is the most recent
 * interaction, or the day they were added if there isn't one yet.
 */
fun dueState(
    cadenceDays: Int?,
    lastInteractionOn: LocalDate?,
    addedOn: LocalDate,
    today: LocalDate
): DueState {
    if (cadenceDays == null) return DueState.NotTracked

    val since = lastInteractionOn ?: addedOn
    val elapsed = ChronoUnit.DAYS.between(since, today)
    val overdueBy = elapsed - cadenceDays

    return if (overdueBy >= 0) DueState.Slipping(daysOverdue = overdueBy)
    else DueState.InTouch(daysUntilDue = -overdueBy)
}
