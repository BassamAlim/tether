package bassamalim.tether.core.data.dataSources.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import bassamalim.tether.core.enums.InteractionType
import java.time.LocalDate

/** History is append-only: you log, you don't curate. Deletion exists only to undo a mistake. */
@Entity(
    tableName = "interactions",
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
data class Interaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val type: InteractionType? = null,
    /**
     * The day it happened, not the day it was logged: backfilling last Tuesday's coffee
     * shouldn't buy an extra week.
     */
    val occurredOn: LocalDate,
    /** Where the value compounds over the years. */
    val note: String = ""
)
