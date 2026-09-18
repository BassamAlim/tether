package bassamalim.tether.core.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Deliberately thin, like the Settings screen: when the weekly nudge lands, the default cadence,
 * and whether the app locks. No account, no theme picker, no sync settings.
 */
@Singleton
class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    fun observeNudgeDay(): Flow<DayOfWeek> = dataStore.data.map {
        DayOfWeek.of(it[NUDGE_DAY] ?: DayOfWeek.SUNDAY.value)
    }

    suspend fun setNudgeDay(day: DayOfWeek) {
        dataStore.edit { it[NUDGE_DAY] = day.value }
    }

    fun observeNudgeTime(): Flow<LocalTime> = dataStore.data.map {
        LocalTime.ofSecondOfDay((it[NUDGE_MINUTE_OF_DAY] ?: DEFAULT_NUDGE_MINUTE) * 60L)
    }

    suspend fun setNudgeTime(time: LocalTime) {
        dataStore.edit { it[NUDGE_MINUTE_OF_DAY] = time.toSecondOfDay() / 60 }
    }

    /** Imported people start here, so the import flow stays two taps. */
    fun observeDefaultCadenceDays(): Flow<Int> = dataStore.data.map {
        it[DEFAULT_CADENCE_DAYS] ?: DEFAULT_CADENCE
    }

    suspend fun setDefaultCadenceDays(days: Int) {
        dataStore.edit { it[DEFAULT_CADENCE_DAYS] = days }
    }

    // TODO: default back to true once the Lock screen is built — until then it would
    //  start the app on a placeholder with no way past it.
    fun observeLockEnabled(): Flow<Boolean> = dataStore.data.map { it[LOCK_ENABLED] ?: false }

    suspend fun setLockEnabled(enabled: Boolean) {
        dataStore.edit { it[LOCK_ENABLED] = enabled }
    }

    companion object {
        private val NUDGE_DAY = intPreferencesKey("nudge_day")
        private val NUDGE_MINUTE_OF_DAY = intPreferencesKey("nudge_minute_of_day")
        private val DEFAULT_CADENCE_DAYS = intPreferencesKey("default_cadence_days")
        private val LOCK_ENABLED = booleanPreferencesKey("lock_enabled")

        /** 10:00 on the nudge day. */
        private const val DEFAULT_NUDGE_MINUTE = 10 * 60
        /** "Month" — the preset the New person screen preselects. */
        private const val DEFAULT_CADENCE = 30
    }
}
