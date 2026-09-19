package bassamalim.tether.core.data.repositories

import bassamalim.tether.core.data.dataSources.room.daos.InteractionsDao
import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import bassamalim.tether.core.enums.Initiator
import bassamalim.tether.core.enums.InteractionType
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InteractionsRepository @Inject constructor(
    private val interactionsDao: InteractionsDao
) {

    private val _deletions = MutableSharedFlow<Interaction>(
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Deleted rows as they go, so whichever screen is showing that history can offer the way
     * back — including when the delete was tapped on a sheet that closes over it. Nothing is
     * replayed: an undo bar nobody was there to see isn't worth keeping.
     */
    val deletions: SharedFlow<Interaction> = _deletions.asSharedFlow()

    fun observeForPerson(personId: Long): Flow<List<Interaction>> =
        interactionsDao.observeForPerson(personId)

    fun search(query: String): Flow<List<Interaction>> = interactionsDao.search(query)

    /** Returns the new row's id so a one-tap log can be undone. */
    suspend fun log(
        personId: Long,
        occurredOn: LocalDate,
        type: InteractionType? = null,
        location: String = "",
        initiatedBy: Initiator? = null,
        note: String = ""
    ): Long = interactionsDao.insert(
        Interaction(
            personId = personId,
            type = type,
            occurredOn = occurredOn,
            location = location,
            initiatedBy = initiatedBy,
            note = note
        )
    )

    suspend fun get(id: Long): Interaction? = interactionsDao.get(id)

    /**
     * Rewrites one logged catch-up in place. It is a correction, not a second entry: the row
     * keeps its id, and a changed date re-dates when they next come due.
     */
    suspend fun update(interaction: Interaction) = interactionsDao.update(interaction)

    suspend fun getAll(): List<Interaction> = interactionsDao.getAll()

    /** Removing one entry from the history. The undo bar holds the only copy until it fades. */
    suspend fun delete(interaction: Interaction) {
        interactionsDao.delete(interaction)
        // tryEmit, so a screen that's slow to raise its undo bar can never hold up a write.
        _deletions.tryEmit(interaction)
    }

    /** Puts a deleted catch-up back as it was, id included, so undo restores rather than relogs. */
    suspend fun restore(interaction: Interaction) {
        interactionsDao.insert(interaction)
    }

    suspend fun undo(interactionId: Long) {
        interactionsDao.get(interactionId)?.let { interactionsDao.delete(it) }
    }
}
