package bassamalim.tether.features.people

import bassamalim.tether.core.domain.DueState
import bassamalim.tether.core.domain.relationshipKey
import bassamalim.tether.core.domain.relationshipsInUse
import bassamalim.tether.core.models.TrackedPerson

/**
 * People's chips are drawn from the people themselves rather than fixed: the relationship
 * vocabulary is the user's and open-ended, so a fixed pair of labels would miss most of it and
 * come back empty whenever the user doesn't happen to use those two words. There is no
 * "Slipping" chip — the list already leads with that section, and Catch up is a whole tab of it.
 */
sealed interface PeopleFilter {
    data object All : PeopleFilter

    /** Cadence "Never": the people an import left untracked, waiting to be opted in. */
    data object Untracked : PeopleFilter

    /** Keyed lowercased, so "work" and "Work" typed on different days are one chip. */
    data class Relationship(val key: String) : PeopleFilter
}

data class FilterOption(val filter: PeopleFilter, val label: String)

/**
 * All first, then Untracked if anyone is, then every relationship in use, most people first.
 * A relationship nobody carries has no chip, so no chip ever comes back empty.
 */
fun filterOptions(people: List<TrackedPerson>): List<FilterOption> {
    val untracked = people.count { it.dueState is DueState.NotTracked }

    val relationships = relationshipsInUse(people.map { it.person.tag })
        .map { FilterOption(PeopleFilter.Relationship(it.key), counted(it.label, it.count)) }

    return buildList {
        add(FilterOption(PeopleFilter.All, "All"))
        if (untracked > 0) add(FilterOption(PeopleFilter.Untracked, counted("Untracked", untracked)))
        addAll(relationships)
    }
}

fun TrackedPerson.matches(filter: PeopleFilter) = when (filter) {
    PeopleFilter.All -> true
    PeopleFilter.Untracked -> dueState is DueState.NotTracked
    is PeopleFilter.Relationship -> relationshipKey(person.tag) == filter.key
}

private fun counted(label: String, count: Int) = "$label · $count"
