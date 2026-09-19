package bassamalim.tether.features.reminder

import bassamalim.tether.core.enums.InteractionType
import java.time.LocalDate
import java.time.LocalTime

data class ReminderUiState(
    val personName: String = "",
    val initials: String = "",
    /** What you mean to do. Optional, like it is on the log sheet. */
    val type: InteractionType? = null,
    val date: LocalDate = LocalDate.EPOCH,
    val time: LocalTime = DEFAULT_TIME,
    val today: LocalDate = LocalDate.EPOCH,
    /** "Sat 27 Sep" and "19:00", formatted where the dates are. */
    val dateLabel: String = "",
    val timeLabel: String = "",
    val isPickingDate: Boolean = false,
    val isPickingTime: Boolean = false,
    /** True when the bell was already set, so Save rewrites it and Remove is offered. */
    val isEditing: Boolean = false,
    /** False when notifications are off for Tether, which a reminder should say out loud. */
    val canNotify: Boolean = true,
    /** False once the chosen moment has passed, which happens while the screen sits open. */
    val isInFuture: Boolean = true,
    val isSaving: Boolean = false
) {
    /** Which segment of the When control is lit. */
    val whenOption: WhenOption = when (date) {
        today.plusDays(1) -> WhenOption.TOMORROW
        today.plusWeeks(1) -> WhenOption.NEXT_WEEK
        else -> WhenOption.PICKED
    }

    /** A reminder for a moment that has already gone by would never arrive. */
    val canSave get() = isInFuture && !isSaving

    companion object {
        /** Early evening: when you'd actually message someone. */
        val DEFAULT_TIME: LocalTime = LocalTime.of(19, 0)
    }
}

enum class WhenOption {
    TOMORROW,
    NEXT_WEEK,
    PICKED
}
