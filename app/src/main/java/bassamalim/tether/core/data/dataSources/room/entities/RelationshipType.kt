package bassamalim.tether.core.data.dataSources.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A relationship type you typed rather than tapped. The defaults live in code
 * (`RelationshipTypes.DEFAULTS`); this table is only what you've added since, so the built-in
 * list can grow in a later version without fighting rows already stored.
 *
 * The label is the key, collated NOCASE, so "old friends" can't become a second "Old friends".
 */
@Entity(tableName = "relationship_types")
data class RelationshipType(
    @PrimaryKey @ColumnInfo(collate = ColumnInfo.NOCASE) val label: String
)
