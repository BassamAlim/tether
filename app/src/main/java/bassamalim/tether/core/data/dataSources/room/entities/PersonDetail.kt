package bassamalim.tether.core.data.dataSources.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One line of the DETAILS card, e.g. "Met: Riyadh JS meetup, 2023", "Kids: Lina (4)".
 *
 * Free-form label/value pairs rather than fixed columns, because the details worth keeping are
 * different for every person and the point is to record what you'd be embarrassed to forget.
 */
@Entity(
    tableName = "person_details",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("personId")]
)
data class PersonDetail(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val label: String,
    val value: String,
    /** Order within the card; the person decides what matters most. */
    val position: Int = 0
)
