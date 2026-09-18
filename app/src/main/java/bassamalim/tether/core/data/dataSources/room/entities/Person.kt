package bassamalim.tether.core.data.dataSources.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import bassamalim.tether.core.enums.RelationshipTag
import java.time.LocalDate

@Entity(tableName = "people")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val tag: RelationshipTag? = null,
    /**
     * Days between check-ins. Null means "Never": the person stays out of Catch up and out of
     * the slipping section, but still appears in People and in search.
     */
    val cadenceDays: Int? = null,
    val phone: String? = null,
    val email: String? = null,
    val birthday: LocalDate? = null,
    /** The details you'd be embarrassed to forget. */
    val notes: String = "",
    /**
     * Someone with no interactions yet counts from the date they were added, so a new person
     * goes overdue on schedule rather than immediately.
     */
    val addedOn: LocalDate,
    val archived: Boolean = false
)
