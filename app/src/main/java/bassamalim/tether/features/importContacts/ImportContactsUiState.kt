package bassamalim.tether.features.importContacts

data class ImportContactsUiState(
    val hasPermission: Boolean = false,
    val isPermissionDenied: Boolean = false,
    val isLoading: Boolean = false,
    val query: String = "",
    /** Everything in the address book, before the search filter. */
    val totalCount: Int = 0,
    val contacts: List<ContactRow> = emptyList(),
    val selectedCount: Int = 0,
    val isImporting: Boolean = false
) {
    val canImport get() = selectedCount > 0 && !isImporting
}

data class ContactRow(
    val id: Long,
    val name: String,
    val initials: String,
    /** Their number, or their email if that's all the address book has. */
    val subtitle: String,
    val isSelected: Boolean
)
