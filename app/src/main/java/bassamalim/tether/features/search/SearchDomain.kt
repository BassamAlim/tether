package bassamalim.tether.features.search

import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import bassamalim.tether.core.data.dataSources.room.entities.Person
import bassamalim.tether.core.data.dataSources.room.entities.PersonDetail
import bassamalim.tether.core.data.repositories.InteractionsRepository
import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.domain.TrackedPeople
import bassamalim.tether.core.models.TrackedPerson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import javax.inject.Inject

/**
 * Searching your own notes is the feature that pays off in year three: you won't remember the
 * name of the person who left Careem, you'll remember Careem — or the cafe you were sitting in.
 *
 * Name matches and note matches answer different questions, so they come back apart.
 */
class SearchDomain @Inject constructor(
    private val trackedPeople: TrackedPeople,
    private val peopleRepository: PeopleRepository,
    private val interactionsRepository: InteractionsRepository
) {

    fun observeResults(query: String): Flow<SearchResults> {
        if (query.isBlank()) return flowOf(SearchResults())

        return combine(
            trackedPeople.observe(),
            peopleRepository.searchDetails(query),
            interactionsRepository.search(query)
        ) { people, details, notes ->
            val byId = people.associateBy { it.person.id }
            val detailsByPerson = details.groupBy { it.personId }

            SearchResults(
                people = people
                    .mapNotNull { person ->
                        // Work is a column rather than a detail row, so it can't come back from
                        // the query that finds detail matches; it's matched here instead.
                        val matchedWork = person.person.workMatches(query)
                        val detail = detailsByPerson[person.person.id]?.firstOrNull()
                        val matchedName = person.person.name.contains(query, ignoreCase = true)
                        if (!matchedName && !matchedWork && detail == null) return@mapNotNull null

                        PersonMatch(
                            person = person,
                            // The line that hit, when the name wasn't what matched.
                            detail = detail,
                            matchedWork = matchedWork
                        )
                    },
                notes = notes.mapNotNull { interaction ->
                    byId[interaction.personId]?.let { NoteMatch(interaction, it) }
                }
            )
        }
    }

    fun today(): LocalDate = trackedPeople.today()
}

data class SearchResults(
    val people: List<PersonMatch> = emptyList(),
    val notes: List<NoteMatch> = emptyList()
)

data class PersonMatch(
    val person: TrackedPerson,
    val detail: PersonDetail?,
    /** True when it was where they work, or what they do, that the query hit. */
    val matchedWork: Boolean = false
)

private fun Person.workMatches(query: String) =
    workplace?.contains(query, ignoreCase = true) == true ||
            jobTitle?.contains(query, ignoreCase = true) == true

data class NoteMatch(val interaction: Interaction, val person: TrackedPerson)
