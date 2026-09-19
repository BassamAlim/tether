package bassamalim.tether.features.addPerson

import bassamalim.tether.core.enums.CadencePreset

data class AddPersonUiState(
    val name: String = "",
    val tag: String = "",
    /** The shared vocabulary; anything typed here joins it. */
    val relationshipOptions: List<String> = emptyList(),
    /**
     * Never until you say otherwise: someone you just wrote down isn't yet someone you've
     * decided to keep up with, and the picker on this screen is where that decision is made.
     */
    val cadence: CadencePreset = CadencePreset.NEVER,
    val howYouMet: String = "",
    val workplace: String = "",
    val jobTitle: String = "",
    val isSaving: Boolean = false
) {
    /** A name is the only thing Tether insists on; everything else can be filled in later. */
    val canSave get() = name.isNotBlank() && !isSaving
}
