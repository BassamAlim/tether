package bassamalim.tether.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.tether.BuildConfig
import bassamalim.tether.core.enums.CadencePreset
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.nav.Screen
import bassamalim.tether.core.utils.cadenceValueLabel
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
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val domain: SettingsDomain,
    private val navigator: Navigator
) : ViewModel() {

    private val dialogs = MutableStateFlow(Dialogs())

    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        domain.observeNudgeEnabled(),
        domain.observeNudgeDay(),
        domain.observeNudgeTime(),
        domain.observeNudgeOnlyWhenOverdue(),
        domain.observeDefaultCadenceDays(),
        dialogs
    ) { values ->
        val enabled = values[0] as Boolean
        val day = values[1] as DayOfWeek
        val time = values[2] as LocalTime
        val onlyWhenOverdue = values[3] as Boolean
        val cadenceDays = values[4] as Int?
        val open = values[5] as Dialogs

        SettingsUiState(
            nudgeEnabled = enabled,
            nudgeDay = day,
            nudgeTime = time,
            nudgeScheduleLabel = "${day.label()}, ${time.format(clockFormat)}",
            nudgeOnlyWhenOverdue = onlyWhenOverdue,
            defaultCadence = CadencePreset.of(cadenceDays),
            defaultCadenceLabel = cadenceValueLabel(cadenceDays),
            version = BuildConfig.VERSION_NAME,
            isPickingSchedule = open.schedule,
            isPickingCadence = open.cadence
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun onNudgeEnabledChange(enabled: Boolean) {
        viewModelScope.launch { domain.setNudgeEnabled(enabled) }
    }

    fun onNudgeOnlyWhenOverdueChange(enabled: Boolean) {
        viewModelScope.launch { domain.setNudgeOnlyWhenOverdue(enabled) }
    }

    fun onScheduleClick() = dialogs.update { it.copy(schedule = true) }

    fun onScheduleDismiss() = dialogs.update { it.copy(schedule = false) }

    fun onScheduleChange(day: DayOfWeek, time: LocalTime) {
        dialogs.update { it.copy(schedule = false) }
        viewModelScope.launch { domain.setNudgeSchedule(day, time) }
    }

    fun onCadenceClick() = dialogs.update { it.copy(cadence = true) }

    fun onCadenceDismiss() = dialogs.update { it.copy(cadence = false) }

    fun onCadenceChange(preset: CadencePreset) {
        dialogs.update { it.copy(cadence = false) }

        viewModelScope.launch { domain.setDefaultCadenceDays(preset.days) }
    }

    fun backupFileName(): String = domain.backupFileName()

    /** The screen owns the file URI; this owns what goes in it. */
    fun onExport(write: (String) -> Boolean) {
        viewModelScope.launch {
            val json = domain.buildBackup()
            _events.send(SettingsEvent.BackupWritten(succeeded = write(json)))
        }
    }

    fun onImportClick() = navigator.navigate(Screen.ImportContacts)

    private data class Dialogs(
        val schedule: Boolean = false,
        val cadence: Boolean = false
    )

    private companion object {
        val clockFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun DayOfWeek.label() = getDisplayName(
            java.time.format.TextStyle.FULL,
            Locale.getDefault()
        )
    }
}
