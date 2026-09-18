package bassamalim.tether.core.data.dataSources.room.relations

import androidx.room.Embedded
import bassamalim.tether.core.data.dataSources.room.entities.Person
import java.time.LocalDate

/** A person plus the one fact every list in the app sorts and colors by. */
data class PersonWithLastInteraction(
    @Embedded val person: Person,
    val lastInteractionOn: LocalDate?
)
