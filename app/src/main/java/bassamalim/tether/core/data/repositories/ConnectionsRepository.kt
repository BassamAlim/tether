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

    /** Connecting someone to themselves is a no-op rather than a row nothing can render. */
    suspend fun connect(oneId: Long, otherId: Long, label: String) {
        if (oneId == otherId) return

        connectionsDao.insert(Connection.between(oneId, otherId, label.trim()))
    }

    suspend fun setLabel(id: Long, label: String) = connectionsDao.updateLabel(id, label.trim())

    suspend fun disconnect(id: Long) = connectionsDao.deleteById(id)
}
