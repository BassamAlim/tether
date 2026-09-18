package bassamalim.tether.features.people

import bassamalim.tether.core.enums.RelationshipTag

data class PeopleUiState(
    val isLoading: Boolean = true,
    val filter: PeopleFilter = PeopleFilter.ALL,
    val totalCount: Int = 0,
    val slippingCount: Int = 0,
    val slipping: List<PersonListItem> = emptyList(),
    val inTouch: List<PersonListItem> = emptyList()
) {
    val isEmpty get() = !isLoading && slipping.isEmpty() && inTouch.isEmpty()
}

/** A row, already formatted — the screen shouldn't be doing date math. */
data class PersonListItem(
    val id: Long,
    val name: String,
    val initials: String,
    val tag: RelationshipTag?,
    val cadenceLabel: String,
    val lastContactLabel: String,
    val isSlipping: Boolean
)

enum class PeopleFilter(val label: String) {
    ALL("All"),
    SLIPPING("Slipping"),
    CLOSE("Close"),
    WORK("Work")
}
