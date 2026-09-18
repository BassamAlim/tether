package bassamalim.tether.core.enums

/**
 * The cadences the app offers. Cadence is set at creation rather than buried in settings — it's
 * the single field the whole nudge engine reads.
 */
enum class CadencePreset(val label: String, val days: Int?) {
    WEEK("Week", 7),
    TWO_WEEKS("2 weeks", 14),
    MONTH("Month", 30),
    THREE_MONTHS("3 months", 90),
    NEVER("Never", null);

    companion object {
        /** The preset a stored cadence came from, or [MONTH] if it was set some other way. */
        fun of(days: Int?) = entries.firstOrNull { it.days == days } ?: MONTH
    }
}
