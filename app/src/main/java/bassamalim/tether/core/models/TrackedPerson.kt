package bassamalim.tether.core.models

import bassamalim.tether.core.data.dataSources.room.entities.Person
import bassamalim.tether.core.domain.DueState
import java.time.LocalDate

/** A person as the app thinks about them: the record plus where they stand against the cadence. */
data class TrackedPerson(
    val person: Person,
    val lastInteractionOn: LocalDate?,
    val dueState: DueState
) {
    val isSlipping get() = dueState is DueState.Slipping
}
