package bassamalim.tether.features.addPerson

import bassamalim.tether.core.enums.CadencePreset
import bassamalim.tether.core.enums.RelationshipTag

data class AddPersonUiState(
    val name: String = "",
    val tag: RelationshipTag? = null,
    val cadence: CadencePreset = CadencePreset.NEVER,
    val howYouMet: String = "",
    val isSaving: Boolean = false
) {
    /** A name is the only thing Tether insists on; everything else can be filled in later. */
    val canSave get() = name.isNotBlank() && !isSaving
}
