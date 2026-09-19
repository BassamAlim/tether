package bassamalim.tether.core.enums

/**
 * The chip on a person's row. One per person: it's a label, not a folder system.
 *
 * [label] is what the New person screen offers; [chipLabel] is the short uppercase form a list
 * row and the detail header wear.
 */
enum class RelationshipTag(val label: String, val chipLabel: String) {
    CLOSE_FRIEND("Close friend", "CLOSE"),
    FRIEND("Friend", "FRIEND"),
    WORK("Work", "WORK"),
    SCHOOL("School", "SCHOOL"),
    UNIVERSITY("University", "UNI"),
    FAMILY("Family", "FAMILY"),
    ACQUAINTANCE("Acquaintance", "ACQUAINTANCE")
}
