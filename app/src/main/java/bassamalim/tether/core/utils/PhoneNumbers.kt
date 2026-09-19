package bassamalim.tether.core.utils

/**
 * Phone numbers as WhatsApp wants them: digits only, in international form, no plus.
 *
 * Returns null when the number can't be put in that form. A national number ("050 123 4567")
 * has no country in it, and guessing one would open a chat with a stranger, so it isn't
 * converted: the caller hands the raw number to WhatsApp instead and lets it match the contact
 * the way the dialler would.
 */
fun internationalDigits(phone: String?): String? {
    val trimmed = phone?.trim().orEmpty()
    if (trimmed.isEmpty()) return null

    // "+44 (0)7700 900123": the bracketed zero is the national trunk prefix and has no place
    // in an international number. A bracketed area code is left alone.
    val digits = trimmed.replace(TRUNK_PREFIX, "").filter { it.isDigit() }
    if (digits.length < MIN_LENGTH) return null

    return when {
        trimmed.startsWith("+") -> digits
        // 00 is the other way of writing +.
        digits.startsWith("00") -> digits.drop(2).takeIf { it.length >= MIN_LENGTH }
        else -> null
    }
}

private val TRUNK_PREFIX = Regex("""\(\s*0\s*\)""")

/** Shortest plausible international number, so typos don't open a chat with someone. */
private const val MIN_LENGTH = 8
