package bassamalim.tether.core.utils

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** The two-letter monogram a row shows instead of a photo. */
fun initials(name: String): String = name
    .split(' ')
    .filter { it.isNotBlank() }
    .take(2)
    .map { it.first().uppercaseChar() }
    .joinToString("")
    .ifEmpty { "?" }

/** "every 2 weeks", "every month", "Never". */
fun cadenceLabel(cadenceDays: Int?): String = when {
    cadenceDays == null -> "Never"
    cadenceDays % 365 == 0 -> every(cadenceDays / 365, "year")
    cadenceDays % 30 == 0 -> every(cadenceDays / 30, "month")
    cadenceDays % 7 == 0 -> every(cadenceDays / 7, "week")
    else -> every(cadenceDays, "day")
}

/** The adjective form of a cadence: "weekly", "2-week", "monthly", "3-month". */
fun cadenceAdjective(cadenceDays: Int?): String = when (cadenceDays) {
    null -> "occasional"
    7 -> "weekly"
    30 -> "monthly"
    365 -> "yearly"
    else -> when {
        cadenceDays % 365 == 0 -> "${cadenceDays / 365}-year"
        cadenceDays % 30 == 0 -> "${cadenceDays / 30}-month"
        cadenceDays % 7 == 0 -> "${cadenceDays / 7}-week"
        else -> "$cadenceDays-day"
    }
}

/** "3d ago", "7w ago", "3mo ago", or "never" for someone you've not logged yet. */
fun elapsedLabel(date: LocalDate?, today: LocalDate): String {
    if (date == null) return "never"

    val days = ChronoUnit.DAYS.between(date, today)
    return when {
        days <= 0L -> "today"
        days < 7L -> "${days}d ago"
        days < 60L -> "${days / 7}w ago"
        days < 365L -> "${days / 30}mo ago"
        else -> "${days / 365}y ago"
    }
}

/** "today", "yesterday", "7 weeks ago" — the long form, for a person's own screen. */
fun agoLabel(date: LocalDate, today: LocalDate): String {
    val days = ChronoUnit.DAYS.between(date, today)

    return when {
        days <= 0L -> "today"
        days == 1L -> "yesterday"
        else -> "${durationLabel(days)} ago"
    }
}

/** "5 weeks past your 2-week check-in" — why this person is on the Catch up list. */
fun overdueReason(daysOverdue: Long, cadenceDays: Int?): String {
    val cadence = cadenceAdjective(cadenceDays)

    return if (daysOverdue == 0L) "Your $cadence check-in is due today"
    else "${durationLabel(daysOverdue)} past your $cadence check-in"
}

/**
 * The line under a person's name: "Last talked 7 weeks ago — 5 weeks overdue".
 *
 * [daysOverdue] is null when they aren't due (or aren't tracked at all).
 */
fun lastTalkedStatus(lastInteractionOn: LocalDate?, daysOverdue: Long?, today: LocalDate): String {
    if (lastInteractionOn == null) return "No catch-ups logged yet"

    val talked = "Last talked ${agoLabel(lastInteractionOn, today)}"

    return when {
        daysOverdue == null -> talked
        daysOverdue == 0L -> "$talked — due today"
        else -> "$talked — ${durationLabel(daysOverdue)} overdue"
    }
}

/** "Due today", "Due tomorrow", "Due in 3 days". */
fun dueReason(daysUntilDue: Long): String = when (daysUntilDue) {
    0L -> "Due today"
    1L -> "Due tomorrow"
    else -> "Due in $daysUntilDue days"
}

/** "3 days", "5 weeks", "2 months" — a span, without the "ago". */
fun durationLabel(days: Long): String = when {
    days < 14L -> count(days, "day")
    days < 60L -> count(days / 7, "week")
    days < 365L -> count(days / 30, "month")
    else -> count(days / 365, "year")
}

private fun every(count: Int, unit: String) =
    if (count == 1) "every $unit" else "every $count ${unit}s"

private fun count(count: Long, unit: String) =
    if (count == 1L) "1 $unit" else "$count ${unit}s"
