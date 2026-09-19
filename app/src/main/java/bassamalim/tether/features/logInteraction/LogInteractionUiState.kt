package bassamalim.tether.features.logInteraction

import bassamalim.tether.core.enums.Initiator
import bassamalim.tether.core.enums.InteractionType
import java.time.LocalDate

data class LogInteractionUiState(
    val personName: String = "",
    val initials: String = "",
    val type: InteractionType? = null,
    val occurredOn: LocalDate = LocalDate.EPOCH,
    val today: LocalDate = LocalDate.EPOCH,
    /** Free text, and optional: a call happened nowhere in particular. */
    val location: String = "",
    /** Optional too: you ran into each other, or you no longer remember who called. */
    val initiatedBy: Initiator? = null,
    val note: String = "",
    val isPickingDate: Boolean = false,
    val isSaving: Boolean = false,
    /** True when the sheet is correcting a catch-up that's already in the history. */
    val isEditing: Boolean = false
) {
    /** The sheet says which of the two jobs it's doing. */
    val title get() = if (isEditing) "Edit catch-up" else "Log a catch-up"

    /** Which segment of the When control is lit. */
    val whenOption: WhenOption = when (occurredOn) {
        today -> WhenOption.TODAY
        today.minusDays(1) -> WhenOption.YESTERDAY
        else -> WhenOption.PICKED
    }

    /** Nothing is required: a bare catch-up with none of this still resets the clock. */
    val canSave get() = !isSaving
}

enum class WhenOption {
    TODAY,
    YESTERDAY,
    PICKED
}
