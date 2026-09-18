package bassamalim.tether.core.data.repositories

import bassamalim.tether.core.data.dataSources.room.daos.InteractionsDao
import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import bassamalim.tether.core.enums.InteractionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InteractionsRepository @Inject constructor(
    private val interactionsDao: InteractionsDao
) {

    fun observeForPerson(personId: Long): Flow<List<Interaction>> =
        interactionsDao.observeForPerson(personId)

    fun searchNotes(query: String): Flow<List<Interaction>> = interactionsDao.searchNotes(query)

    /** Returns the new row's id so a one-tap log can be undone. */
    suspend fun log(
        personId: Long,
        occurredOn: LocalDate,
        type: InteractionType? = null,
        note: String = ""
    ): Long = interactionsDao.insert(
        Interaction(personId = personId, type = type, occurredOn = occurredOn, note = note)
    )

    suspend fun getAll(): List<Interaction> = interactionsDao.getAll()

    suspend fun undo(interactionId: Long) {
        interactionsDao.get(interactionId)?.let { interactionsDao.delete(it) }
    }
}
