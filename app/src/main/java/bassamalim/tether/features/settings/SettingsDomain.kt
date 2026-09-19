package bassamalim.tether.features.settings

import bassamalim.tether.core.data.repositories.ConnectionsRepository
import bassamalim.tether.core.data.repositories.InteractionsRepository
import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.data.repositories.PreferencesRepository
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
    private val nudgeScheduler: NudgeScheduler,
    private val clock: Clock
) {

    fun observeNudgeEnabled(): Flow<Boolean> = preferencesRepository.observeNudgeEnabled()

    fun observeNudgeDay(): Flow<DayOfWeek> = preferencesRepository.observeNudgeDay()

    fun observeNudgeTime(): Flow<LocalTime> = preferencesRepository.observeNudgeTime()

    fun observeNudgeOnlyWhenOverdue(): Flow<Boolean> =
        preferencesRepository.observeNudgeOnlyWhenOverdue()

    fun observeDefaultCadenceDays(): Flow<Int?> = preferencesRepository.observeDefaultCadenceDays()

    /** Every change to when the nudge lands re-books the pending one. */
    suspend fun setNudgeEnabled(enabled: Boolean) {
        preferencesRepository.setNudgeEnabled(enabled)
        nudgeScheduler.sync()
    }

    suspend fun setNudgeSchedule(day: DayOfWeek, time: LocalTime) {
        preferencesRepository.setNudgeDay(day)
        preferencesRepository.setNudgeTime(time)
        nudgeScheduler.sync()
    }

    suspend fun setNudgeOnlyWhenOverdue(enabled: Boolean) =
        preferencesRepository.setNudgeOnlyWhenOverdue(enabled)

    suspend fun setDefaultCadenceDays(days: Int?) = preferencesRepository.setDefaultCadenceDays(days)

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
                    tag = person.tag?.name,
                    cadenceDays = person.cadenceDays,
                    phone = person.phone,
                    addedOn = person.addedOn.toString(),
                    details = detailsByPerson[person.id].orEmpty().map {
                        BackupDetail(label = it.label, value = it.value)
                    },
                    interactions = interactionsByPerson[person.id].orEmpty().map {
                        BackupInteraction(
                            type = it.type?.name,
                            occurredOn = it.occurredOn.toString(),
                            location = it.location,
                            initiatedBy = it.initiatedBy?.name,
                            note = it.note
                        )
                    }
                )
            },
            connections = connectionsRepository.getAll().mapNotNull { connection ->
                val one = peopleById[connection.personAId] ?: return@mapNotNull null
                val other = peopleById[connection.personBId] ?: return@mapNotNull null

                BackupConnection(a = one.name, b = other.name, label = connection.label)
            }
        )

        return json.encodeToString(backup)
    }

    private companion object {
        val json = Json { prettyPrint = true }
    }
}
