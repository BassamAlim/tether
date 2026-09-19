package bassamalim.tether.features.settings

import bassamalim.tether.core.backup.BackupConnection
import bassamalim.tether.core.backup.BackupDetail
import bassamalim.tether.core.backup.BackupFile
import bassamalim.tether.core.backup.BackupPerson
import bassamalim.tether.core.backup.BackupRead
import bassamalim.tether.core.backup.BackupRestore
import bassamalim.tether.core.backup.RestoreResult
import bassamalim.tether.core.backup.asBackup
import bassamalim.tether.core.data.repositories.ConnectionsRepository
import bassamalim.tether.core.data.repositories.InteractionsRepository
import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.data.repositories.PreferencesRepository
import bassamalim.tether.core.data.repositories.RelationshipTypesRepository
import bassamalim.tether.core.nudge.NudgeScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class SettingsDomain @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val peopleRepository: PeopleRepository,
    private val interactionsRepository: InteractionsRepository,
    private val connectionsRepository: ConnectionsRepository,
    private val relationshipTypesRepository: RelationshipTypesRepository,
    private val backupRestore: BackupRestore,
    private val nudgeScheduler: NudgeScheduler,
    private val clock: Clock
) {

    fun observeNudgeEnabled(): Flow<Boolean> = preferencesRepository.observeNudgeEnabled()

    fun observeNudgeDay(): Flow<DayOfWeek> = preferencesRepository.observeNudgeDay()

    fun observeNudgeTime(): Flow<LocalTime> = preferencesRepository.observeNudgeTime()

    /** Every change to when the nudge lands re-books the pending one. */
    suspend fun setNudgeEnabled(enabled: Boolean) {
        preferencesRepository.setNudgeEnabled(enabled)
        nudgeScheduler.sync()
    }

    /** Picking a time is a request to be nudged at it, so it turns the nudge on as well. */
    suspend fun setNudgeSchedule(day: DayOfWeek, time: LocalTime) {
        preferencesRepository.setNudgeDay(day)
        preferencesRepository.setNudgeTime(time)
        preferencesRepository.setNudgeEnabled(true)
        nudgeScheduler.sync()
    }

    fun backupFileName(): String = "tether-backup-${LocalDate.now(clock)}.json"

    suspend fun buildBackup(): String {
        val people = peopleRepository.getAll()
        val detailsByPerson = peopleRepository.getAllDetails().groupBy { it.personId }
        val interactionsByPerson = interactionsRepository.getAll().groupBy { it.personId }
        val peopleById = people.associateBy { it.id }

        val backup = BackupFile(
            exportedOn = LocalDate.now(clock).toString(),
            people = people.map { person ->
                BackupPerson(
                    name = person.name,
                    tag = person.tag,
                    cadenceDays = person.cadenceDays,
                    phone = person.phone,
                    workplace = person.workplace,
                    jobTitle = person.jobTitle,
                    addedOn = person.addedOn.toString(),
                    details = detailsByPerson[person.id].orEmpty().map {
                        BackupDetail(label = it.label, value = it.value)
                    },
                    interactions = interactionsByPerson[person.id].orEmpty().map { it.asBackup() }
                )
            },
            connections = connectionsRepository.getAll().mapNotNull { connection ->
                val one = peopleById[connection.personAId] ?: return@mapNotNull null
                val other = peopleById[connection.personBId] ?: return@mapNotNull null

                BackupConnection(a = one.name, b = other.name, label = connection.label)
            },
            // Only the ones that were typed: the built-in list ships with the app, so writing
            // it out would let an old file put back words a later version had dropped.
            relationshipTypes = relationshipTypesRepository.getAdded()
        )

        return json.encodeToString(backup)
    }

    /** What a picked file is, before a word of it is written. */
    fun readBackup(json: String): BackupRead = backupRestore.read(json)

    suspend fun restoreBackup(file: BackupFile): RestoreResult = backupRestore.restore(file)

    private companion object {
        val json = Json { prettyPrint = true }
    }
}
