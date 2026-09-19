package bassamalim.tether.features.setUpImported

import bassamalim.tether.core.enums.CadencePreset

data class SetUpImportedUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val initials: String = "",
    /** Their number as the address book had it, blank if it had none. */
    val phone: String = "",
    /** "2 of 5": how far through the people you just picked. */
    val progress: String = "",
    /** The last one, so the action reads "Done" rather than "Next". */
    val isLast: Boolean = true,
    val tag: String = "",
    val relationshipOptions: List<String> = emptyList(),
    /**
     * Never until you say otherwise, exactly as on New person: copying someone in isn't the
     * same as deciding to keep up with them.
     */
    val cadence: CadencePreset = CadencePreset.NEVER,
    val howYouMet: String = "",
    /** Prefilled from the address book's Organization entry, and correctable here. */
    val workplace: String = "",
    val jobTitle: String = "",
    val isSaving: Boolean = false
) {
    val canSave get() = !isLoading && !isSaving

    val actionLabel get() = if (isLast) "Done" else "Next"
}
