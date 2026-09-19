package bassamalim.tether.features.person

import bassamalim.tether.core.enums.CadencePreset
import bassamalim.tether.core.enums.RelationshipTag

data class PersonUiState(
    val isLoading: Boolean = true,
    val id: Long = 0,
    val name: String = "",
    val initials: String = "",
    val tag: RelationshipTag? = null,
    val tagLabel: String? = null,
    val cadence: CadencePreset = CadencePreset.MONTH,
    val cadenceLabel: String = "",
    /** "Last talked 7 weeks ago, 5 weeks overdue". */
    val status: String = "",
    val isOverdue: Boolean = false,
    val phone: String? = null,
    val details: List<DetailRow> = emptyList(),
    val history: List<HistoryEntry> = emptyList(),
    val isPickingTag: Boolean = false,
    val isPickingCadence: Boolean = false,
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
