package bassamalim.tether.features.logInteraction

import bassamalim.tether.core.enums.Initiator
import bassamalim.tether.core.enums.InteractionType
import bassamalim.tether.core.utils.parsePlace
import java.time.LocalDate

data class LogInteractionUiState(
    val personName: String = "",
    val initials: String = "",
    val type: InteractionType? = null,
    val occurredOn: LocalDate = LocalDate.EPOCH,
    val today: LocalDate = LocalDate.EPOCH,
    /** Free text, and optional: a call happened nowhere in particular. */
    val location: String = "",
    /** True while a pasted Maps link's name is being fetched. */
    val isLookingUpPlace: Boolean = false,
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

    /**
     * Under "Where" once a link is in it, saying how the history will read it — a pasted link
     * is unreadable in the field, and a short one carries no name to show.
     */
    val locationHint: String? get() {
        if (isLookingUpPlace) return "Looking up the place\u2026"
        val place = parsePlace(location)?.takeIf { it.url != null } ?: return null
        return if (place.isNamed) {
            "Shows as \u201c${place.label}\u201d and opens the map"
        } else {
            "Shows as \u201c${place.label}\u201d. Type a name before the link to use that instead"
        }
    }

    /** Nothing is required: a bare catch-up with none of this still resets the clock. */
    val canSave get() = !isSaving
}

enum class WhenOption {
    TODAY,
    YESTERDAY,
    PICKED
}
