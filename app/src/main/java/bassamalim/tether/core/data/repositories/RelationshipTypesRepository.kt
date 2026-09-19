package bassamalim.tether.core.data.repositories

import bassamalim.tether.core.data.dataSources.room.daos.RelationshipTypesDao
import bassamalim.tether.core.data.dataSources.room.entities.RelationshipType
import bassamalim.tether.core.domain.RelationshipTypes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The relationship vocabulary: the built-in list plus everything you've typed into a
 * relationship or a connection since.
 */
@Singleton
class RelationshipTypesRepository @Inject constructor(
    private val relationshipTypesDao: RelationshipTypesDao
) {

    /**
     * Defaults first, in their curated order, then your own alphabetically — so the list you
     * learned doesn't reshuffle every time you add to it.
     */
    fun observeAll(): Flow<List<String>> = relationshipTypesDao.observeAll().map { added ->
        RelationshipTypes.DEFAULTS + added.map { it.label }.filterNot(RelationshipTypes::isDefault)
    }

    suspend fun getAdded(): List<String> = relationshipTypesDao.getAll().map { it.label }

    /**
     * Keeps a type you typed, so next time it's a tap. A blank isn't a type, and a default is
     * already in the list, so neither is stored.
     */
    suspend fun remember(label: String?) {
        val trimmed = label?.trim().orEmpty()
        if (trimmed.isEmpty() || RelationshipTypes.isDefault(trimmed)) return

        relationshipTypesDao.insert(RelationshipType(trimmed))
    }

    suspend fun rememberAll(labels: Collection<String>) = labels.forEach { remember(it) }
}
