package bassamalim.tether.features.search

data class SearchUiState(
    val query: String = "",
    val people: List<PersonResult> = emptyList(),
    val notes: List<NoteResult> = emptyList()
) {
    val hasQuery get() = query.isNotBlank()
    val isEmpty get() = hasQuery && people.isEmpty() && notes.isEmpty()
}

data class PersonResult(
    val id: Long,
    val name: String,
    val initials: String,
    /** What matched: their work, the detail line that hit, or else their cadence. */
    val subtitle: String,
    val lastContactLabel: String
)

data class NoteResult(
    val id: Long,
    val personId: Long,
    val personName: String,
    val initials: String,
    /** "Coffee · Blue Tokai · 7w ago". */
    val meta: String,
    val note: String
)
