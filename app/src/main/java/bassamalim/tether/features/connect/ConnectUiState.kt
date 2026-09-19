package bassamalim.tether.features.connect

data class ConnectUiState(
    val isLoading: Boolean = true,
    /** The person you came from; the one end of every link on this screen. */
    val personName: String = "",
    val query: String = "",
    /** The shared relationship vocabulary; anything typed here joins it. */
    val relationshipOptions: List<String> = emptyList(),
    val candidates: List<ConnectCandidate> = emptyList(),
    /** True once the picking is done and the labels are being written. */
    val isLabelling: Boolean = false,
    /** The line everyone follows until they're given their own. */
    val sharedLabel: String = "",
    val picks: List<ConnectPick> = emptyList(),
    /** Non-null while one person's own line is being written. */
    val editing: ConnectPickEdit? = null
) {
    val selectedCount get() = picks.size
    val hasSelection get() = picks.isNotEmpty()
    val hasNobodyToConnect get() = !isLoading && query.isBlank() && candidates.isEmpty()
}

data class ConnectCandidate(
    val id: Long,
    val name: String,
    val initials: String,
    /** Their relationship tag, when they have one; a connection is easier to place with it. */
    val tagLabel: String?,
    val isSelected: Boolean
)

/** Someone picked, with the label their link will be saved under. */
data class ConnectPick(
    val id: Long,
    val name: String,
    val initials: String,
    val label: String,
    /** What the row shows, which has to say something even when the label is empty. */
    val subtitle: String,
    /** True when this one was written by hand rather than inherited from the shared line. */
    val hasOwnLabel: Boolean
)

data class ConnectPickEdit(val id: Long, val name: String, val label: String)
