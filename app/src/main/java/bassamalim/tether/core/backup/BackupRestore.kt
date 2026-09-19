package bassamalim.tether.core.backup

import bassamalim.tether.core.data.dataSources.room.entities.Connection
import bassamalim.tether.core.data.dataSources.room.entities.Person
import bassamalim.tether.core.data.dataSources.room.entities.PersonDetail
import bassamalim.tether.core.data.repositories.ConnectionsRepository
import bassamalim.tether.core.data.repositories.InteractionsRepository
import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.data.repositories.RelationshipTypesRepository
import kotlinx.serialization.json.Json
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/**
 * Reading a backup back in — the other half of the export in `SettingsDomain`.
 *
 * A restore **merges and never overwrites.** The archive on this phone is the live one; the file
 * is a record of what it held when it was written, which may be older, may be from a phone that
 * has since been used, and may be the same file you imported ten minutes ago. So:
 *
 * - people are matched by name, and a matched person keeps everything they've got — the file
 *   only fills in fields that are empty here;
 * - details and catch-ups are added only where the file holds more of them than the database
 *   does, compared as whole entries, so importing the same file twice adds nothing the second
 *   time and an import interrupted halfway can simply be run again;
 * - nothing is ever deleted, which is why there's no undo bar to write: there is nothing to
 *   take back.
 *
 * The cost of matching on names is that two people who share one can't be told apart. Same-name
 * rows are handed out one at a time in file order, so a file holding two Ahmeds restores two
 * Ahmeds and re-importing it restores neither — but if you've since renamed one of them, the
 * file's copy comes back as a new person. Names are all the file has: ids are deliberately left
 * out of it so that something which isn't Tether can still read it in ten years.
 */
class BackupRestore @Inject constructor(
    private val peopleRepository: PeopleRepository,
    private val interactionsRepository: InteractionsRepository,
    private val connectionsRepository: ConnectionsRepository,
    private val relationshipTypesRepository: RelationshipTypesRepository,
    private val clock: Clock
) {

    /** What a picked file turned out to be. */
    fun read(json: String): BackupRead {
        val file = runCatching { parser.decodeFromString<BackupFile>(json) }.getOrNull()
            ?: return BackupRead.Unreadable

        // A file from a later Tether may carry people in a shape this version would silently
        // drop half of. Saying so is better than importing part of someone.
        if (file.format > BackupFile.FORMAT) return BackupRead.TooNew(file.format)

        return BackupRead.Readable(file)
    }

    suspend fun restore(file: BackupFile): RestoreResult {
        // Taken one at a time as names are claimed, so a second import of the same file finds
        // everyone already accounted for.
        val unclaimed = peopleRepository.getAll()
            .groupBy { it.name.matchKey() }
            .mapValues { (_, rows) -> ArrayDeque(rows) }

        val detailsByPerson = peopleRepository.getAllDetails().groupBy { it.personId }
        val historyByPerson = interactionsRepository.getAll().groupBy { it.personId }

        var peopleAdded = 0
        var peopleMerged = 0
        var detailsAdded = 0
        var catchUpsAdded = 0

        for (person in file.people) {
            val name = person.name.trim()
            if (name.isEmpty()) continue

            val relationship = person.relationship(file.format)
            val existing = unclaimed[name.matchKey()]?.removeFirstOrNull()

            if (existing == null) {
                val personId = peopleRepository.create(
                    person = Person(
                        name = name,
                        tag = relationship,
                        cadenceDays = person.cadenceDays,
                        phone = person.phone,
                        // A file with no readable date still describes a real person; today is
                        // the one day we can be sure they were in Tether.
                        addedOn = person.addedOn.asLocalDate() ?: LocalDate.now(clock)
                    ),
                    details = person.details.map {
                        PersonDetail(personId = 0, label = it.label, value = it.value)
                    }
                )

                peopleAdded++
                detailsAdded += person.details.size
                catchUpsAdded += addCatchUps(personId, person.interactions, have = emptyList())
            } else {
                peopleMerged++

                // Blanks are filled in and answers are left alone: a restore is not the place
                // to argue with the phone you're holding.
                val filled = existing.copy(
                    tag = existing.tag ?: relationship,
                    cadenceDays = existing.cadenceDays ?: person.cadenceDays,
                    phone = existing.phone ?: person.phone
                )
                if (filled != existing) peopleRepository.save(filled)

                val newDetails = missingCopies(
                    have = detailsByPerson[existing.id].orEmpty()
                        .map { BackupDetail(label = it.label, value = it.value) },
                    want = person.details
                )

                if (newDetails.isNotEmpty()) {
                    peopleRepository.addDetails(
                        newDetails.map {
                            PersonDetail(personId = existing.id, label = it.label, value = it.value)
                        }
                    )
                    detailsAdded += newDetails.size
                }

                catchUpsAdded += addCatchUps(
                    personId = existing.id,
                    want = person.interactions,
                    have = historyByPerson[existing.id].orEmpty().map { it.asBackup() }
                )
            }
        }

        return RestoreResult(
            peopleAdded = peopleAdded,
            peopleMerged = peopleMerged,
            detailsAdded = detailsAdded,
            catchUpsAdded = catchUpsAdded,
            connectionsAdded = restoreConnections(file),
            relationshipTypesAdded = rememberRelationships(file)
        )
    }

    private suspend fun addCatchUps(
        personId: Long,
        want: List<BackupInteraction>,
        have: List<BackupInteraction>
    ): Int {
        val rows = missingCopies(have = have, want = want).mapNotNull { it.asRow(personId) }
        if (rows.isNotEmpty()) interactionsRepository.addAll(rows)

        return rows.size
    }

    /**
     * Links are resolved after the people pass, against everyone then in Tether rather than only
     * the people this file brought: a file can name a connection between two people you already
     * had. A name the database doesn't know is skipped — a connection to someone who isn't a
     * person is not a thing Tether has.
     */
    private suspend fun restoreConnections(file: BackupFile): Int {
        if (file.connections.isEmpty()) return 0

        val idsByName = peopleRepository.getAll()
            .groupBy { it.name.matchKey() }
            // A shared name can't be told apart here either, so the first row wears the link.
            .mapValues { (_, rows) -> rows.first().id }

        val existingPairs = connectionsRepository.getAll()
            .map { it.personAId to it.personBId }
            .toSet()

        val links = file.connections
            .mapNotNull { link ->
                val one = idsByName[link.a.matchKey()] ?: return@mapNotNull null
                val other = idsByName[link.b.matchKey()] ?: return@mapNotNull null
                if (one == other) return@mapNotNull null

                Connection.between(one, other, link.label)
            }
            .distinctBy { it.personAId to it.personBId }
            .filterNot { (it.personAId to it.personBId) in existingPairs }

        links.groupBy { it.personAId }.forEach { (personId, forPerson) ->
            connectionsRepository.connectAll(
                personId = personId,
                labelsByPersonId = forPerson.associate { it.personBId to it.label }
            )
        }

        return links.size
    }

    /**
     * The vocabulary, and only as the file recorded it — never swept out of the words the file's
     * people and connections happen to use.
     *
     * The list of types is a decision, not a summary of the data: a word can be removed from it
     * while the connections that already carry it keep carrying it, and that is a thing people
     * do. Harvesting the labels coming back would quietly put those words back in the pills. So
     * a restore repopulates what was in the list at export, the imported labels stay on the rows
     * they belong to, and a file older than format 4 — which has no list — changes the
     * vocabulary not at all.
     */
    private suspend fun rememberRelationships(file: BackupFile): Int {
        if (file.relationshipTypes.isEmpty()) return 0

        val before = relationshipTypesRepository.getAdded().size
        relationshipTypesRepository.rememberAll(file.relationshipTypes)

        return relationshipTypesRepository.getAdded().size - before
    }

    /** Names match on the words, not the spacing or the case: "  ahmed" is Ahmed. */
    private fun String.matchKey() = trim().lowercase()

    private companion object {
        /**
         * Lenient about keys it doesn't know: a file from a later Tether should lose only the
         * fields this version has never heard of, not the whole import.
         */
        val parser = Json { ignoreUnknownKeys = true }
    }
}

/**
 * The entries [want] holds more copies of than [have] does.
 *
 * A multiset difference rather than a set one, so two coffees logged on the same day with the
 * same note both come back, while a file imported twice adds neither of them again. This is the
 * whole of what makes a restore repeatable, which is why it's out here on its own.
 */
internal fun <T> missingCopies(have: List<T>, want: List<T>): List<T> {
    val spare = have.groupingBy { it }.eachCount().toMutableMap()

    return want.filter { entry ->
        val copies = spare[entry] ?: 0
        if (copies == 0) return@filter true

        spare[entry] = copies - 1
        false
    }
}

/** What [BackupRestore.read] made of a picked file. */
sealed interface BackupRead {
    data class Readable(val file: BackupFile) : BackupRead
    data object Unreadable : BackupRead
    data class TooNew(val format: Int) : BackupRead
}

/** What a restore actually changed, which is what the bar afterwards reports. */
data class RestoreResult(
    val peopleAdded: Int = 0,
    /** People the file described who were already here, and were left as they are. */
    val peopleMerged: Int = 0,
    val detailsAdded: Int = 0,
    val catchUpsAdded: Int = 0,
    val connectionsAdded: Int = 0,
    val relationshipTypesAdded: Int = 0
) {
    /** Nothing landed: every word of that file was already in Tether. */
    val addedNothing
        get() = peopleAdded == 0 && detailsAdded == 0 && catchUpsAdded == 0 &&
                connectionsAdded == 0 && relationshipTypesAdded == 0
}
