package bassamalim.tether.features.search

import bassamalim.tether.core.data.dataSources.room.entities.Interaction
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
 * name of the person who left Careem, you'll remember Careem.
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
            interactionsRepository.searchNotes(query)
        ) { people, details, notes ->
            val byId = people.associateBy { it.person.id }
            val detailsByPerson = details.groupBy { it.personId }

            SearchResults(
                people = people
                    .filter { it.person.name.contains(query, ignoreCase = true) || it.person.id in detailsByPerson }
                    .map { person ->
                        PersonMatch(
                            person = person,
                            // The line that hit, when the name wasn't what matched.
                            detail = detailsByPerson[person.person.id]?.firstOrNull()
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

data class PersonMatch(val person: TrackedPerson, val detail: PersonDetail?)

data class NoteMatch(val interaction: Interaction, val person: TrackedPerson)
