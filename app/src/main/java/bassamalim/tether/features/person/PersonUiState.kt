package bassamalim.tether.features.person

import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import bassamalim.tether.core.enums.CadencePreset

data class PersonUiState(
    val isLoading: Boolean = true,
    val id: Long = 0,
    val name: String = "",
    val initials: String = "",
    /** Their relationship as stored, which is what the editor starts from. */
    val tag: String = "",
    /** The chip's uppercase form, or null when they have none. */
    val tagLabel: String? = null,
    /** The shared vocabulary, for both the relationship and a connection's label. */
    val relationshipOptions: List<String> = emptyList(),
    /** The draft while the relationship is being rewritten. */
    val tagDraft: String = "",
    val cadence: CadencePreset = CadencePreset.MONTH,
    val cadenceLabel: String = "",
    /** "Last talked 7 weeks ago, 5 weeks overdue". */
    val status: String = "",
    val isOverdue: Boolean = false,
    val phone: String? = null,
    /** "Coffee · Tomorrow, 19:00", or null when the bell isn't set. */
    val reminderLabel: String? = null,
    val details: List<DetailRow> = emptyList(),
    val connections: List<ConnectionEntry> = emptyList(),
    /** Non-null while the label of one connection is being rewritten. */
    val editingConnection: ConnectionEdit? = null,
    val history: List<HistoryEntry> = emptyList(),
    val isPickingTag: Boolean = false,
    val isPickingCadence: Boolean = false,
    val isMenuOpen: Boolean = false,
    val isConfirmingDelete: Boolean = false
) {
    val canMessage get() = !phone.isNullOrBlank()
}

data class DetailRow(val id: Long, val label: String, val value: String)

/** Someone this person knows. [personId] is the other person, not the one on screen. */
data class ConnectionEntry(
    val connectionId: Long,
    val personId: Long,
    val name: String,
    val initials: String,
    /** The shared label as stored, which is what the editor starts from. */
    val label: String,
    /** What the row shows: the label, or a stand-in when the link has no words yet. */
    val subtitle: String
)

data class ConnectionEdit(val connectionId: Long, val name: String, val label: String)

data class HistoryEntry(
    val id: Long,
    /** True while this row's options menu is open. Only ever one at a time. */
    val isMenuOpen: Boolean = false,
    /** The interaction type, or "Caught up" for a one-tap log that didn't ask. */
    val title: String,
    /**
     * Who reached out and where, already joined: "They reached out · Blue Tokai". Null when the
     * log answered neither, which most one-tap logs don't.
     */
    val meta: String?,
    val timeLabel: String,
    val note: String?
)

/** One-shot signals the screen acts on rather than renders. */
sealed interface PersonEvent {
    /**
     * A history entry has been removed and the row itself is the only copy left, so the undo
     * bar carries it back rather than looking it up again.
     */
    data class HistoryDeleted(val interaction: Interaction) : PersonEvent
}
