package bassamalim.tether.core.models

/** A contact as the phone's address book has it. Read once, never written back to. */
data class DeviceContact(
    val id: Long,
    val name: String,
    val phone: String? = null,
    val email: String? = null
)
