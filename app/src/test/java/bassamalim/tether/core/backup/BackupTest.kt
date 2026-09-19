package bassamalim.tether.core.backup

import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import bassamalim.tether.core.enums.Initiator
import bassamalim.tether.core.enums.InteractionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/**
 * The backup file, which is the only copy of the archive that survives a lost phone — and the
 * merge rule that lets it be read back more than once.
 */
class BackupTest {

    @Test
    fun `a catch-up survives the round trip through the file`() {
        val row = Interaction(
            id = 7,
            personId = 3,
            type = InteractionType.COFFEE,
            occurredOn = LocalDate.of(2026, 3, 14),
            location = "Blue Tokai",
            initiatedBy = Initiator.THEM,
            note = "Talked about the move"
        )

        // The id is the one thing that doesn't come back: the file carries none, so a restore
        // writes new rows rather than fighting over ids that mean nothing outside this phone.
        assertEquals(row.copy(id = 0, personId = 3), row.asBackup().asRow(personId = 3))
    }

    @Test
    fun `a catch-up with no readable date is dropped rather than guessed`() {
        assertNull(BackupInteraction(occurredOn = "some time last spring").asRow(personId = 1))
    }

    @Test
    fun `a kind this version has never heard of loses the kind, not the catch-up`() {
        val read = BackupInteraction(type = "HOT_AIR_BALLOON", occurredOn = "2026-03-14")
            .asRow(personId = 1)

        assertNull(read?.type)
        assertEquals(LocalDate.of(2026, 3, 14), read?.occurredOn)
    }

    @Test
    fun `an old file's tag is read back as the words it stood for`() {
        val person = BackupPerson(name = "Sara", tag = "CLOSE_FRIEND", addedOn = "2026-01-01")

        assertEquals("Close friend", person.relationship(format = 3))
    }

    @Test
    fun `from format 4 on a tag is already the words`() {
        val person = BackupPerson(name = "Sara", tag = "Old friends", addedOn = "2026-01-01")

        assertEquals("Old friends", person.relationship(format = BackupFile.FORMAT))
    }

    @Test
    fun `an old file's unrecognised tag is left alone rather than dropped`() {
        val person = BackupPerson(name = "Sara", tag = "Padel", addedOn = "2026-01-01")

        assertEquals("Padel", person.relationship(format = 3))
    }

    @Test
    fun `a blank tag is no relationship at all`() {
        val person = BackupPerson(name = "Sara", tag = "   ", addedOn = "2026-01-01")

        assertNull(person.relationship(format = BackupFile.FORMAT))
    }

    @Test
    fun `importing the same file twice adds nothing the second time`() {
        val file = listOf(coffee("2026-03-14"), coffee("2026-04-01"))

        assertEquals(file, missingCopies(have = emptyList(), want = file))
        assertEquals(emptyList<BackupInteraction>(), missingCopies(have = file, want = file))
    }

    @Test
    fun `two identical catch-ups both come back`() {
        val twice = listOf(coffee("2026-03-14"), coffee("2026-03-14"))

        // A set difference would have collapsed these into one; two coffees on one day are two.
        assertEquals(twice, missingCopies(have = emptyList(), want = twice))
        assertEquals(1, missingCopies(have = twice.take(1), want = twice).size)
    }

    @Test
    fun `only what the file has more of is added`() {
        val have = listOf(coffee("2026-03-14"))
        val want = listOf(coffee("2026-03-14"), coffee("2026-04-01"))

        assertEquals(listOf(coffee("2026-04-01")), missingCopies(have = have, want = want))
    }

    @Test
    fun `a catch-up that differs in any part is a different catch-up`() {
        val have = listOf(coffee("2026-03-14"))
        val want = listOf(coffee("2026-03-14").copy(note = "and the move"))

        assertEquals(want, missingCopies(have = have, want = want))
    }

    @Test
    fun `a file says what it holds, and doesn't mention what it hasn't got`() {
        val file = BackupFile(
            exportedOn = "2026-09-12",
            people = listOf(
                BackupPerson(
                    name = "Sara",
                    addedOn = "2026-01-01",
                    interactions = listOf(coffee("2026-03-14"), coffee("2026-04-01"))
                ),
                BackupPerson(name = "Ahmed", addedOn = "2026-01-02")
            )
        )

        assertEquals("2 people · 2 catch-ups", previewOf(file).contents)
        assertEquals("2026-09-12", previewOf(file).exportedOn)
    }

    @Test
    fun `one of something is not 1 people`() {
        val file = BackupFile(
            exportedOn = "2026-09-12",
            people = listOf(
                BackupPerson(
                    name = "Sara",
                    addedOn = "2026-01-01",
                    interactions = listOf(coffee("2026-03-14"))
                )
            ),
            connections = listOf(BackupConnection(a = "Sara", b = "Ahmed", label = "Siblings"))
        )

        assertEquals("1 person · 1 catch-up · 1 connection", previewOf(file).contents)
    }

    @Test
    fun `the summary names what was added and who was already here`() {
        val result = RestoreResult(peopleAdded = 3, peopleMerged = 2, catchUpsAdded = 11)

        assertEquals("Imported 3 people, 11 catch-ups. 2 people were already here.",
            restoreSummary(result))
    }

    @Test
    fun `a second import of the same file says so rather than claiming nothing happened`() {
        val result = RestoreResult(peopleMerged = 4)

        assertEquals("Everything in that backup was already here.", restoreSummary(result))
    }

    @Test
    fun `an empty file is not the same as one that was already here`() {
        assertEquals("There was nothing in that backup.", restoreSummary(RestoreResult()))
    }

    private fun coffee(on: String) = BackupInteraction(
        type = InteractionType.COFFEE.name,
        occurredOn = on,
        location = "Blue Tokai",
        initiatedBy = Initiator.ME.name,
        note = ""
    )
}
