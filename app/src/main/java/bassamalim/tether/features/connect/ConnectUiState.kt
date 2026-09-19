package bassamalim.tether.features.connect

data class ConnectUiState(
    val isLoading: Boolean = true,
    /** The person you came from; the one end of the link that's already decided. */
    val personName: String = "",
    val query: String = "",
    val candidates: List<ConnectCandidate> = emptyList(),
    val selected: ConnectCandidate? = null,
    val label: String = ""
) {
    val canSave get() = selected != null
    val hasNobodyToConnect get() = !isLoading && query.isBlank() && candidates.isEmpty()
}

data class ConnectCandidate(
    val id: Long,
    val name: String,
    val initials: String,
    /** Their relationship tag, when they have one; a connection is easier to place with it. */
    val tagLabel: String?
)

/**
 * Openers for the label. One shared phrase describes the link from both ends, so these read as
 * facts about the pair rather than about either person.
 */
val CONNECTION_SUGGESTIONS = listOf(
    "Siblings",
    "Cousins",
    "Old friends",
    "Studied together",
    "Worked together",
    "Neighbours"
)
