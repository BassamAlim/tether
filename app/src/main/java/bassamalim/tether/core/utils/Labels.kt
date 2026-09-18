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
    cadenceDays % 365 == 0 -> plural(cadenceDays / 365, "year")
    cadenceDays % 30 == 0 -> plural(cadenceDays / 30, "month")
    cadenceDays % 7 == 0 -> plural(cadenceDays / 7, "week")
    else -> plural(cadenceDays, "day")
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

private fun plural(count: Int, unit: String) =
    if (count == 1) "every $unit" else "every $count ${unit}s"
