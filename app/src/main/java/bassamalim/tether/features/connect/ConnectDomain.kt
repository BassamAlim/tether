package bassamalim.tether.features.connect

import bassamalim.tether.core.data.dataSources.room.entities.Person
import bassamalim.tether.core.data.repositories.ConnectionsRepository
import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.data.repositories.RelationshipTypesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConnectDomain @Inject constructor(
    private val peopleRepository: PeopleRepository,
    private val connectionsRepository: ConnectionsRepository,
    private val relationshipTypesRepository: RelationshipTypesRepository
) {

    fun observeRelationshipOptions(): Flow<List<String>> = relationshipTypesRepository.observeAll()

    fun observeName(personId: Long): Flow<String?> =
        peopleRepository.observe(personId).map { it?.person?.name }

    /**
     * Everyone this person could be linked to: not themselves, and not someone the link already
     * exists with — offering a duplicate would only ever write the same row again.
     */
    fun observeCandidates(personId: Long): Flow<List<Person>> = combine(
        peopleRepository.observeAll(),
        connectionsRepository.observeConnectedIds(personId)
    ) { people, connectedIds ->
        val taken = connectedIds.toSet() + personId

        people.map { it.person }.filter { it.id !in taken }
    }

    suspend fun connectAll(personId: Long, labelsByPersonId: Map<Long, String>) {
        // Anything typed over the batch joins the vocabulary, the same as a relationship does.
        relationshipTypesRepository.rememberAll(labelsByPersonId.values.toSet())
        connectionsRepository.connectAll(personId, labelsByPersonId)
    }
}
