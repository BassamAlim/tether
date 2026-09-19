package bassamalim.tether.core.reminder

import bassamalim.tether.core.enums.InteractionType

/**
 * What a reminder says when it arrives. It names the person and the thing you meant to do,
 * because "you have a reminder" is a notification about the app rather than about them.
 */
data class ReminderCopy(val title: String, val body: String)

fun reminderCopy(
    name: String,
    type: InteractionType?,
    /** Their last-talked line, as Person detail words it. */
    status: String
): ReminderCopy = ReminderCopy(
    title = type?.let { "${it.label} with $name" } ?: "Catch up with $name",
    body = status
)
