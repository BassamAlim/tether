package bassamalim.tether.core.data.dataSources.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import bassamalim.tether.core.enums.InteractionType
import java.time.LocalDateTime

/**
 * A one-off nudge about one person: "coffee with Maya, Saturday at seven".
 *
 * Separate from the cadence, which says how often you mean to keep up in general and is what
 * Catch up and the weekly nudge are built on. This is the intention you have right now, and it
 * is spent when it arrives: the row is deleted once the notification is posted.
 *
 * At most one per person — the unique index says so — because the bell on Person detail is
 * either set or it isn't, and a list of pending alarms about one friend is an inbox, not a
 * reminder.
 */
@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["personId"], unique = true)]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    /** The catch-up you mean to have. Null when it's just "reach out". */
    val type: InteractionType? = null,
    /** Wall clock, not an instant: seven in the evening stays seven if you change timezone. */
    val scheduledFor: LocalDateTime
)
