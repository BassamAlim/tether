package bassamalim.tether.features.firstRun

import bassamalim.tether.core.backup.BackupFile
import bassamalim.tether.core.backup.BackupRead
import bassamalim.tether.core.backup.BackupRestore
import bassamalim.tether.core.backup.RestoreResult
import bassamalim.tether.core.data.repositories.PeopleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FirstRunDomain @Inject constructor(
    private val peopleRepository: PeopleRepository,
    private val backupRestore: BackupRestore
) {

    /** The only thing this screen waits for: somebody, anybody. */
    fun observePeopleCount(): Flow<Int> = peopleRepository.observeCount()

    fun readBackup(json: String): BackupRead = backupRestore.read(json)

    suspend fun restoreBackup(file: BackupFile): RestoreResult = backupRestore.restore(file)
}
