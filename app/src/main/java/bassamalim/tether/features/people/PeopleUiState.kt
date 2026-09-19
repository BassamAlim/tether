package bassamalim.tether.features.people

data class PeopleUiState(
    val isLoading: Boolean = true,
    val filter: PeopleFilter = PeopleFilter.All,
    val filters: List<FilterOption> = emptyList(),
    val totalCount: Int = 0,
    val slippingCount: Int = 0,
    val slipping: List<PersonListItem> = emptyList(),
    val inTouch: List<PersonListItem> = emptyList(),
    /** Heads [inTouch]; under the Untracked chip nobody listed is in touch, just untracked. */
    val inTouchLabel: String = "In touch"
) {
    val isEmpty get() = !isLoading && slipping.isEmpty() && inTouch.isEmpty()
}

/** A row, already formatted: the screen shouldn't be doing date math. */
data class PersonListItem(
    val id: Long,
    val name: String,
    val initials: String,
    /** Their relationship, uppercased for the chip. */
    val tag: String?,
    val cadenceLabel: String,
    val lastContactLabel: String,
    val isSlipping: Boolean
)
