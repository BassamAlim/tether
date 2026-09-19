package bassamalim.tether.features.settings

import kotlinx.serialization.Serializable

/**
 * The shape of an exported backup. With no server, this file is the only copy of your archive
 * that survives a lost phone, so it's plain readable JSON rather than a database dump; it
 * should still be openable in ten years by something that isn't Tether.
 */
@Serializable
data class BackupFile(
    val format: Int = FORMAT,
    val exportedOn: String,
    val people: List<BackupPerson>,
    /** Who knows who. Top-level, because a connection belongs to the pair, not to either one. */
    val connections: List<BackupConnection> = emptyList()
) {
    companion object {
        const val FORMAT = 3
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

/**
 * The two ends are written as names, not ids: the point of the file is that something which
 * isn't Tether can still read it in ten years, and an id means nothing on its own.
 */
@Serializable
data class BackupConnection(val a: String, val b: String, val label: String = "")

@Serializable
data class BackupInteraction(
    val type: String? = null,
    val occurredOn: String,
    val location: String = "",
    /** "ME" or "THEM", absent when the log never asked. */
    val initiatedBy: String? = null,
    val note: String = ""
)
