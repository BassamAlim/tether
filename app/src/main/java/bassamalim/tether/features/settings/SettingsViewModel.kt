package bassamalim.tether.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.tether.BuildConfig
import bassamalim.tether.core.backup.BackupFile
import bassamalim.tether.core.backup.BackupRead
import bassamalim.tether.core.backup.ImportPreview
import bassamalim.tether.core.backup.previewOf
import bassamalim.tether.core.backup.restoreSummary
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.utils.timeLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val domain: SettingsDomain,
    private val navigator: Navigator
) : ViewModel() {

    /** What this screen owns rather than the database: its dialogs and its in-flight import. */
    private val localState = MutableStateFlow(LocalState())

    /**
     * The parsed file behind [LocalState.pendingImport], held here rather than in the state: the
     * state carries what the dialog says, and this is what Import would write.
     */
    private var picked: BackupFile? = null

    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        domain.observeNudgeEnabled(),
        domain.observeNudgeDay(),
        domain.observeNudgeTime(),
        localState
    ) { enabled, day, time, local ->
        // Four flows fit the typed overload, so the state reads without casting an array.
        SettingsUiState(
            nudgeEnabled = enabled,
            nudgeDay = day,
            nudgeTime = time,
            nudgeScheduleLabel = if (enabled) "${day.label()}s at ${timeLabel(time)}" else "Off",
            version = BuildConfig.VERSION_NAME,
            isPickingSchedule = local.isPickingSchedule,
            pendingImport = local.pendingImport,
            isImporting = local.isImporting
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun onNudgeEnabledChange(enabled: Boolean) {
        viewModelScope.launch { domain.setNudgeEnabled(enabled) }
    }

    fun onScheduleClick() = localState.update { it.copy(isPickingSchedule = true) }

    fun onScheduleDismiss() = localState.update { it.copy(isPickingSchedule = false) }

    fun onScheduleChange(day: DayOfWeek, time: LocalTime) {
        localState.update { it.copy(isPickingSchedule = false) }
        viewModelScope.launch { domain.setNudgeSchedule(day, time) }
    }

    fun backupFileName(): String = domain.backupFileName()

    /** The screen owns the file URI; this owns what goes in it. */
    fun onExport(write: (String) -> Boolean) {
        viewModelScope.launch {
            val json = domain.buildBackup()
            _events.send(SettingsEvent.BackupWritten(succeeded = write(json)))
        }
    }

    /**
     * The screen hands over the file's text, or null if it couldn't be read at all. Nothing is
     * written yet: this only works out what the file is, so the dialog can say so.
     */
    fun onImportPicked(json: String?) {
        if (json == null) return report(SettingsEvent.BackupUnreadable)

        when (val read = domain.readBackup(json)) {
            is BackupRead.Readable -> {
                picked = read.file
                localState.update { it.copy(pendingImport = previewOf(read.file)) }
            }

            BackupRead.Unreadable -> report(SettingsEvent.BackupUnreadable)

            is BackupRead.TooNew -> report(SettingsEvent.BackupTooNew)
        }
    }

    fun onImportDismiss() {
        picked = null
        localState.update { it.copy(pendingImport = null) }
    }

    fun onImportConfirm() {
        val file = picked ?: return

        picked = null
        localState.update { it.copy(pendingImport = null, isImporting = true) }

        viewModelScope.launch {
            val result = domain.restoreBackup(file)

            localState.update { it.copy(isImporting = false) }
            report(SettingsEvent.BackupRestored(restoreSummary(result)))
        }
    }

    private fun report(event: SettingsEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private data class LocalState(
        val isPickingSchedule: Boolean = false,
        val pendingImport: ImportPreview? = null,
        val isImporting: Boolean = false
    )

    private companion object {
        fun DayOfWeek.label() = getDisplayName(
            java.time.format.TextStyle.FULL,
            Locale.getDefault()
        )
    }
}
