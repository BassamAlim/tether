package bassamalim.tether.core.data.dataSources.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "people")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** How you know them, from the shared relationship vocabulary. Null means unsaid. */
    val tag: String? = null,
    /**
     * Days between check-ins. Null means "Never": the person stays out of Catch up and out of
     * the slipping section, but still appears in People and in search.
     */
    val cadenceDays: Int? = null,
    /** Only for the message action and for people copied in from contacts. */
    val phone: String? = null,
    /**
     * Where they work and what they do there, each null when unsaid. Columns rather than detail
     * rows because they're asked for by name — on New person, on the set-up walk, and read off
     * the address book's Organization — and shown under the name rather than in the card.
     */
    val workplace: String? = null,
    val jobTitle: String? = null,
    /**
     * Someone with no interactions yet counts from the date they were added, so a new person
     * goes overdue on schedule rather than immediately.
     */
    val addedOn: LocalDate,
    val archived: Boolean = false
)
