package bassamalim.tether.core.backup

/**
 * What a backup says it holds, so the question asked before anything is written is about this
 * file rather than about importing in general.
 */
data class ImportPreview(
    /** "12 people · 40 catch-ups · 6 connections". */
    val contents: String,
    /** The day it was exported, as the file gives it. */
    val exportedOn: String
)

/** The line the confirm dialog leads with. */
fun previewOf(file: BackupFile) = ImportPreview(
    contents = buildList {
        add(count(file.people.size, "person", "people"))
        add(count(file.people.sumOf { it.interactions.size }, "catch-up", "catch-ups"))

        // Only when there are any: a file with no connections shouldn't have to say so.
        if (file.connections.isNotEmpty())
            add(count(file.connections.size, "connection", "connections"))
    }.joinToString(" · "),
    exportedOn = file.exportedOn
)

/**
 * What the restore changed, in the order you'd want to hear it.
 *
 * Both screens that can import say it the same way, which is the point of this living here: a
 * merge is mostly invisible, so the count of what it left alone is as much of the answer as the
 * count of what it added.
 */
fun restoreSummary(result: RestoreResult): String {
    if (result.addedNothing) {
        return if (result.peopleMerged == 0) "There was nothing in that backup."
        else "Everything in that backup was already here."
    }

    val added = buildList {
        if (result.peopleAdded > 0) add(count(result.peopleAdded, "person", "people"))
        if (result.catchUpsAdded > 0) add(count(result.catchUpsAdded, "catch-up", "catch-ups"))
        if (result.detailsAdded > 0) add(count(result.detailsAdded, "detail", "details"))
        if (result.connectionsAdded > 0)
            add(count(result.connectionsAdded, "connection", "connections"))
    }.joinToString(", ")

    val alreadyHere = when (result.peopleMerged) {
        0 -> ""
        1 -> " 1 person was already here."
        else -> " ${result.peopleMerged} people were already here."
    }

    return "Imported $added.$alreadyHere"
}

private fun count(n: Int, singular: String, plural: String) =
    if (n == 1) "1 $singular" else "$n $plural"
