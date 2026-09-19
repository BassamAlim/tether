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

    fun observeNudgeEnabled(): Flow<Boolean> = dataStore.data.map { it[NUDGE_ENABLED] ?: true }

    suspend fun setNudgeEnabled(enabled: Boolean) {
        dataStore.edit { it[NUDGE_ENABLED] = enabled }
    }

    /** Off by default: a nudge that says "nobody's due" is still worth reading. */
    fun observeNudgeOnlyWhenOverdue(): Flow<Boolean> =
        dataStore.data.map { it[NUDGE_ONLY_WHEN_OVERDUE] ?: false }

    suspend fun setNudgeOnlyWhenOverdue(enabled: Boolean) {
        dataStore.edit { it[NUDGE_ONLY_WHEN_OVERDUE] = enabled }
    }

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

    /**
     * What new people start on, imported ones included. Null is "Never": they are added without
     * a cadence and nothing nudges you about them until you give them one.
     */
    fun observeDefaultCadenceDays(): Flow<Int?> = dataStore.data.map { preferences ->
        (preferences[DEFAULT_CADENCE_DAYS] ?: NEVER).takeIf { it != NEVER }
    }

    suspend fun setDefaultCadenceDays(days: Int?) {
        dataStore.edit { it[DEFAULT_CADENCE_DAYS] = days ?: NEVER }
    }

    fun observeLockEnabled(): Flow<Boolean> = dataStore.data.map { it[LOCK_ENABLED] ?: true }

    suspend fun setLockEnabled(enabled: Boolean) {
        dataStore.edit { it[LOCK_ENABLED] = enabled }
    }

    companion object {
        private val NUDGE_ENABLED = booleanPreferencesKey("nudge_enabled")
        private val NUDGE_ONLY_WHEN_OVERDUE = booleanPreferencesKey("nudge_only_when_overdue")
        private val NUDGE_DAY = intPreferencesKey("nudge_day")
        private val NUDGE_MINUTE_OF_DAY = intPreferencesKey("nudge_minute_of_day")
        private val DEFAULT_CADENCE_DAYS = intPreferencesKey("default_cadence_days")
        private val LOCK_ENABLED = booleanPreferencesKey("lock_enabled")

        /** 10:00 on the nudge day. */
        private const val DEFAULT_NUDGE_MINUTE = 10 * 60
        /** DataStore has no null, so "Never" is stored as this. */
        private const val NEVER = -1
    }
}
