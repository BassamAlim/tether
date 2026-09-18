package bassamalim.tether.features.settings

import kotlinx.serialization.Serializable

/**
 * The shape of an exported backup. With no server, this file is the only copy of your archive
 * that survives a lost phone, so it's plain readable JSON rather than a database dump — it
 * should still be openable in ten years by something that isn't Tether.
 */
@Serializable
data class BackupFile(
    val format: Int = FORMAT,
    val exportedOn: String,
    val people: List<BackupPerson>
) {
    companion object {
        const val FORMAT = 1
    }
}

@Serializable
data class BackupPerson(
    val name: String,
    val tag: String? = null,
    val cadenceDays: Int? = null,
    val phone: String? = null,
    val addedOn: String,
    val details: List<BackupDetail> = emptyList(),
    val interactions: List<BackupInteraction> = emptyList()
)

@Serializable
data class BackupDetail(val label: String, val value: String)

@Serializable
data class BackupInteraction(
    val type: String? = null,
    val occurredOn: String,
    val note: String = ""
)
