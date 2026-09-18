package bassamalim.tether.features.logInteraction

import bassamalim.tether.core.enums.InteractionType
import java.time.LocalDate

data class LogInteractionUiState(
    val personName: String = "",
    val initials: String = "",
    val type: InteractionType? = null,
    val occurredOn: LocalDate = LocalDate.EPOCH,
    val today: LocalDate = LocalDate.EPOCH,
    val note: String = "",
    val isPickingDate: Boolean = false,
    val isSaving: Boolean = false
) {
    /** Which segment of the When control is lit. */
    val whenOption: WhenOption = when (occurredOn) {
        today -> WhenOption.TODAY
        today.minusDays(1) -> WhenOption.YESTERDAY
        else -> WhenOption.PICKED
    }

    /** Nothing is required: a catch-up with no type and no note still resets the clock. */
    val canSave get() = !isSaving
}

enum class WhenOption {
    TODAY,
    YESTERDAY,
    PICKED
}
