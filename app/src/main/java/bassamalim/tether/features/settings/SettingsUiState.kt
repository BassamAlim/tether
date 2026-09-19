package bassamalim.tether.features.settings

import bassamalim.tether.core.backup.ImportPreview
import java.time.DayOfWeek
import java.time.LocalTime

data class SettingsUiState(
    val nudgeEnabled: Boolean = true,
    val nudgeDay: DayOfWeek = DayOfWeek.SUNDAY,
    val nudgeTime: LocalTime = LocalTime.of(10, 0),
    /** "Sundays at 10:00 AM", or "Off" when the nudge is. */
    val nudgeScheduleLabel: String = "",
    val version: String = "",
    val isPickingSchedule: Boolean = false,
    /** The picked file, waiting on a yes. Null when no import has been offered. */
    val pendingImport: ImportPreview? = null,
    val isImporting: Boolean = false
)

/** One-shot outcomes the screen reports and then forgets. */
sealed interface SettingsEvent {
    data class BackupWritten(val succeeded: Boolean) : SettingsEvent

    /** Already phrased: what the restore actually changed. */
    data class BackupRestored(val summary: String) : SettingsEvent

    /** The file didn't parse, or couldn't be read off the disk at all. */
    data object BackupUnreadable : SettingsEvent

    /** A file from a later Tether, which this version would only half understand. */
    data object BackupTooNew : SettingsEvent
}
