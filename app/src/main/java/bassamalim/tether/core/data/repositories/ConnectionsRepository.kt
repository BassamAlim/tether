package bassamalim.tether.core.data.repositories

import bassamalim.tether.core.data.dataSources.room.daos.ConnectionsDao
import bassamalim.tether.core.data.dataSources.room.entities.Connection
import bassamalim.tether.core.data.dataSources.room.relations.ConnectedPerson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** The only way into the connections table. */
@Singleton
class ConnectionsRepository @Inject constructor(
    private val connectionsDao: ConnectionsDao
) {

    fun observeFor(personId: Long): Flow<List<ConnectedPerson>> =
        connectionsDao.observeFor(personId)

    fun observeConnectedIds(personId: Long): Flow<List<Long>> =
        connectionsDao.observeConnectedIds(personId)

    suspend fun getAll(): List<Connection> = connectionsDao.getAll()

    /**
     * Links one person to many at once, each with its own label — a batch is how these are
     * actually gathered, and writing them one by one would leave a half-connected person behind
     * if it failed partway. Connecting someone to themselves is dropped rather than written as a
     * row nothing can render.
     */
    suspend fun connectAll(personId: Long, labelsByPersonId: Map<Long, String>) {
        val connections = labelsByPersonId
            .filterKeys { it != personId }
            .map { (otherId, label) -> Connection.between(personId, otherId, label.trim()) }

        if (connections.isNotEmpty()) connectionsDao.insertAll(connections)
    }

    suspend fun setLabel(id: Long, label: String) = connectionsDao.updateLabel(id, label.trim())

    suspend fun disconnect(id: Long) = connectionsDao.deleteById(id)
}
