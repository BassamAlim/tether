package bassamalim.tether.core.backup

import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import bassamalim.tether.core.domain.RelationshipTypes
import bassamalim.tether.core.enums.Initiator
import bassamalim.tether.core.enums.InteractionType
import kotlinx.serialization.Serializable
import java.time.LocalDate

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
    val connections: List<BackupConnection> = emptyList(),
    /**
     * The relationship types you added by hand. The built-in list is the app's and comes back
     * with it; these are yours, and nothing else in the file would bring them back — a type you
     * typed but haven't used on anyone yet exists nowhere else.
     */
    val relationshipTypes: List<String> = emptyList()
) {
    companion object {
        /**
         * 5 since people carry where they work ([BackupPerson.workplace], [BackupPerson.jobTitle]).
         * The fields are optional, so a format-4 file still reads; the bump is so a Tether that
         * predates them refuses the file rather than quietly dropping everyone's job.
         *
         * 4 was when relationships became one free-text vocabulary: [BackupPerson.tag] kept its
         * type but changed content, from the old enum's constant ("CLOSE_FRIEND") to the label
         * itself ("Close friend"). A reader has no other way to tell the two apart, which is
         * what the number is for.
         */
        const val FORMAT = 5

        /** Up to and including this, a `tag` is an enum constant rather than a label. */
        const val LAST_ENUM_TAG_FORMAT = 3
    }
}

@Serializable
data class BackupPerson(
    val name: String,
    val tag: String? = null,
    val cadenceDays: Int? = null,
    val phone: String? = null,
    val workplace: String? = null,
    val jobTitle: String? = null,
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

/**
 * A logged catch-up in the file's shape. The writer and the reader share this one mapping, so
 * what a restore compares a stored row against is exactly what an export would have written
 * for it — which is what lets the same file be imported twice without doubling the history.
 */
fun Interaction.asBackup() = BackupInteraction(
    type = type?.name,
    occurredOn = occurredOn.toString(),
    location = location,
    initiatedBy = initiatedBy?.name,
    note = note
)

/**
 * The row to write for one catch-up in the file, or null when it has no date that can be read:
 * a catch-up with no day can't be placed on a timeline, and guessing one would move a cadence.
 *
 * An unrecognised type or initiator is dropped rather than refused — a file from a later Tether
 * may name kinds this version has never heard of, and the date and the note are the parts worth
 * keeping.
 */
fun BackupInteraction.asRow(personId: Long): Interaction? {
    val date = occurredOn.asLocalDate() ?: return null

    return Interaction(
        personId = personId,
        type = type?.let { name -> InteractionType.entries.firstOrNull { it.name == name } },
        occurredOn = date,
        location = location,
        initiatedBy = initiatedBy?.let { name -> Initiator.entries.firstOrNull { it.name == name } },
        note = note
    )
}

/**
 * The relationship this person's [BackupPerson.tag] names, in today's words.
 *
 * Up to format 3 the field held the old enum's constant, so "CLOSE_FRIEND" has to be read back
 * as "Close friend" — the same translation [RelationshipTypes.LEGACY_TAG_NAMES] does for rows
 * written before the enum was dropped. Anything it doesn't recognise is left exactly as it is,
 * since from format 4 on the field holds whatever words the user chose.
 */
fun BackupPerson.relationship(format: Int): String? {
    val stored = tag?.trim()?.takeIf { it.isNotEmpty() } ?: return null

    return if (format <= BackupFile.LAST_ENUM_TAG_FORMAT)
        RelationshipTypes.LEGACY_TAG_NAMES[stored] ?: stored
    else stored
}

/** An ISO date as the file gives it, or null if it isn't one. */
fun String.asLocalDate(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()
