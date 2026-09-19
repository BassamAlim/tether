package bassamalim.tether.core.data.dataSources.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Who knows who: one undirected edge between two people, with a shared free-form [label].
 *
 * The label is written to read the same from either end ("Siblings", "Work together at Careem")
 * rather than one word per direction, so there is a single sentence to keep true instead of two
 * that can drift apart.
 *
 * The pair is stored canonically — [personAId] is always the smaller id — and the index over the
 * pair is unique, so connecting Ahmed to Sara and Sara to Ahmed is the same row either way.
 */
@Entity(
    tableName = "connections",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personAId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personBId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["personAId", "personBId"], unique = true),
        Index("personBId")
    ]
)
data class Connection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personAId: Long,
    val personBId: Long,
    val label: String = ""
) {
    companion object {
        /** The only way to build one: ordering the pair here is what keeps the edge single. */
        fun between(oneId: Long, otherId: Long, label: String = "") = Connection(
            personAId = minOf(oneId, otherId),
            personBId = maxOf(oneId, otherId),
            label = label
        )
    }
}
