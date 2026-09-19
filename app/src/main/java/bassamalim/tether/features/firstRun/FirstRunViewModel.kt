package bassamalim.tether.features.firstRun

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.tether.core.backup.BackupFile
import bassamalim.tether.core.backup.BackupRead
import bassamalim.tether.core.backup.ImportPreview
import bassamalim.tether.core.backup.previewOf
import bassamalim.tether.core.backup.restoreSummary
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.nav.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirstRunViewModel @Inject constructor(
    private val domain: FirstRunDomain,
    private val navigator: Navigator
) : ViewModel() {

    /** What this screen owns rather than the database: the import it's part way through. */
    private val localState = MutableStateFlow(LocalState())

    /** The parsed file behind [LocalState.pendingImport]: what Import would write. */
    private var picked: BackupFile? = null

    val uiState: StateFlow<FirstRunUiState> = combine(
        domain.observePeopleCount(),
        localState
    ) { count, local ->
        FirstRunUiState(
            isLoading = false,
            hasPeople = count > 0,
            pendingImport = local.pendingImport,
            isImporting = local.isImporting,
            importProblem = local.importProblem
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FirstRunUiState()
    )

    fun onAddPersonClick() = navigator.navigate(Screen.AddPerson)

    fun onImportContactsClick() = navigator.navigate(Screen.ImportContacts)

    /**
     * The file's text, or null when it couldn't be read off the disk. Nothing is written yet:
     * this only works out what the file is, so the dialog can say so.
     */
    fun onBackupPicked(json: String?) {
        if (json == null) return refuse("That file couldn't be opened.")

        when (val read = domain.readBackup(json)) {
            is BackupRead.Readable -> {
                picked = read.file
                localState.update {
                    it.copy(pendingImport = previewOf(read.file), importProblem = null)
                }
            }

            BackupRead.Unreadable -> refuse("That doesn't look like a Tether backup.")

            is BackupRead.TooNew ->
                refuse("That backup was written by a newer Tether than this one.")
        }
    }

    fun onImportDismiss() {
        picked = null
        localState.update { it.copy(pendingImport = null) }
    }

    /**
     * On this screen a restore that works needs no announcement: the moment there are people,
     * [onPeopleExist] steps aside to People and the restored archive is the answer. Only a file
     * that turned out to hold nothing leaves you here, with something to say.
     */
    fun onImportConfirm() {
        val file = picked ?: return

        picked = null
        localState.update {
            it.copy(pendingImport = null, isImporting = true, importProblem = null)
        }

        viewModelScope.launch {
            val result = domain.restoreBackup(file)

            localState.update {
                it.copy(
                    isImporting = false,
                    importProblem = if (result.peopleAdded == 0) restoreSummary(result) else null
                )
            }
        }
    }

    /**
     * The moment there's someone to show, this screen has nothing left to say.
     *
     * It steps aside from the screen rather than from here, because an import adds people while
     * the contacts picker and the set-up walk are stacked on top of this screen: navigating the
     * instant the rows land would pop that flow away mid-way. Read with the screen's lifecycle,
     * this only fires when First run is the one you're looking at.
     */
    fun onPeopleExist() = navigator.navigate(Screen.Main()) {
        popUpTo(Screen.FirstRun) { inclusive = true }
    }

    private fun refuse(problem: String) {
        picked = null
        localState.update { it.copy(pendingImport = null, importProblem = problem) }
    }

    private data class LocalState(
        val pendingImport: ImportPreview? = null,
        val isImporting: Boolean = false,
        val importProblem: String? = null
    )
}
