package bassamalim.tether.core.domain

/**
 * One relationship as the people carry it: [key] is lowercased, so "work" and "Work" typed on
 * different days are one relationship, and [label] is the spelling most of them use.
 */
data class RelationshipInUse(val key: String, val label: String, val count: Int)

/** The key a person's tag is grouped under, or null when they have none. */
fun relationshipKey(tag: String?): String? = tag?.trim()?.takeIf(String::isNotEmpty)?.lowercase()

/**
 * Every relationship somebody carries, most people first, then alphabetically. People's filter
 * chips and Circle's colours both read this, so a relationship is the same group on both.
 */
fun relationshipsInUse(tags: List<String?>): List<RelationshipInUse> = tags
    .mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) }
    .groupBy { it.lowercase() }
    .map { (key, spellings) ->
        // Show the spelling most of them use; ties go to whichever came first.
        val label = spellings.groupingBy { it }.eachCount().maxBy { it.value }.key
        RelationshipInUse(key, label, spellings.size)
    }
    .sortedWith(compareByDescending<RelationshipInUse> { it.count }.thenBy { it.label.lowercase() })
