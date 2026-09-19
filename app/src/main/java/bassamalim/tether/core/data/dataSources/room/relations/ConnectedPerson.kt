package bassamalim.tether.core.data.dataSources.room.relations

import androidx.room.Embedded
import bassamalim.tether.core.data.dataSources.room.entities.Person

/**
 * The far end of a connection, as the person you're looking at sees it: the other person, plus
 * the label the two of them share. Which column they sat in is resolved in the query, so no
 * screen has to know the pair is stored in id order.
 */
data class ConnectedPerson(
    @Embedded val person: Person,
    val connectionId: Long,
    val label: String
)
