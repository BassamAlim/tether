package bassamalim.tether.features.settings

import bassamalim.tether.core.enums.CadencePreset
import java.time.DayOfWeek
import java.time.LocalTime

data class SettingsUiState(
    val nudgeEnabled: Boolean = true,
    val nudgeDay: DayOfWeek = DayOfWeek.SUNDAY,
    val nudgeTime: LocalTime = LocalTime.of(10, 0),
    /** "Sunday, 09:00". */
    val nudgeScheduleLabel: String = "",
    val nudgeOnlyWhenOverdue: Boolean = false,
    val defaultCadence: CadencePreset = CadencePreset.MONTH,
    val defaultCadenceLabel: String = "",
    val version: String = "",
    val isPickingSchedule: Boolean = false,
    val isPickingCadence: Boolean = false
)

/** One-shot outcomes the screen reports and then forgets. */
sealed interface SettingsEvent {
    data class BackupWritten(val succeeded: Boolean) : SettingsEvent
}
