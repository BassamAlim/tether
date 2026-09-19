package bassamalim.tether.core.domain

/**
 * One vocabulary for both ends of a relationship: how you know someone, and how two people know
 * each other. They were two closed lists once — a per-person tag and a per-link label — which
 * meant "siblings" could describe a pair but never a person, and "Acquaintance" the reverse.
 *
 * [DEFAULTS] is only where the list starts. Anything typed into a relationship or a connection
 * joins it (`RelationshipTypesRepository`), so the vocabulary is the user's, not the app's.
 */
object RelationshipTypes {

    /**
     * The union of the two original lists, grouped by how close the tie is. "Worked together"
     * and "Studied together" aren't here: with one vocabulary for both ends, "Work" and
     * "School"/"University" already say it, from either side.
     */
    val DEFAULTS = listOf(
        "Close friend",
        "Friend",
        "Old friends",
        "Family",
        "Siblings",
        "Cousins",
        "Work",
        "School",
        "University",
        "Neighbours",
        "Acquaintance"
    )

    /**
     * What the `RelationshipTag` enum's constants meant, for anything reading rows or files
     * written before it was dropped: the 4→5 migration, and a backup exported under an older
     * format. One table, so the two readings can't drift apart.
     */
    val LEGACY_TAG_NAMES = mapOf(
        "CLOSE_FRIEND" to "Close friend",
        "FRIEND" to "Friend",
        "WORK" to "Work",
        "SCHOOL" to "School",
        "UNIVERSITY" to "University",
        "FAMILY" to "Family",
        "ACQUAINTANCE" to "Acquaintance"
    )

    /** Case-insensitive, because "siblings" and "Siblings" are one type, not two. */
    fun isDefault(label: String) = DEFAULTS.any { it.equals(label, ignoreCase = true) }
}
