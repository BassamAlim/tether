package bassamalim.tether.features.person

data class PersonUiState(
    val isLoading: Boolean = true,
    val id: Long = 0,
    val name: String = "",
    val initials: String = "",
    val tagLabel: String? = null,
    val cadenceLabel: String = "",
    /** "Last talked 7 weeks ago — 5 weeks overdue". */
    val status: String = "",
    val isOverdue: Boolean = false,
    val phone: String? = null,
    val details: List<DetailRow> = emptyList(),
    val history: List<HistoryEntry> = emptyList(),
    val isMenuOpen: Boolean = false,
    val isConfirmingDelete: Boolean = false
) {
    val canMessage get() = !phone.isNullOrBlank()
}

data class DetailRow(val id: Long, val label: String, val value: String)

data class HistoryEntry(
    val id: Long,
    /** The interaction type, or "Caught up" for a one-tap log that didn't ask. */
    val title: String,
    val timeLabel: String,
    val note: String?
)
