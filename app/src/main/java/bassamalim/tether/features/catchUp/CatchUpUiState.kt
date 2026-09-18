package bassamalim.tether.features.catchUp

data class CatchUpUiState(
    val isLoading: Boolean = true,
    val overdue: List<CatchUpItem> = emptyList(),
    val dueThisWeek: List<CatchUpItem> = emptyList()
) {
    val total get() = overdue.size + dueThisWeek.size
    val isEmpty get() = !isLoading && total == 0
}

data class CatchUpItem(
    val id: Long,
    val name: String,
    val initials: String,
    /** Why they're here: "5 weeks past your 2-week check-in", "Due in 3 days". */
    val reason: String,
    val isOverdue: Boolean
)

/** One-shot things the screen reacts to rather than renders. */
sealed interface CatchUpEvent {
    data class Logged(val personName: String, val interactionId: Long) : CatchUpEvent
}
