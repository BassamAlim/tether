package bassamalim.tether.core.data.dataSources.room

import androidx.room.TypeConverter
import bassamalim.tether.core.enums.InteractionType
import bassamalim.tether.core.enums.RelationshipTag
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromEpochDay(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    /**
     * Enums are read by name, and a name that no longer exists decays to null rather than
     * throwing. Room's generated converter would crash instead, which turns dropping a type
     * from the list into a crash for anyone who had logged one.
     */
    @TypeConverter
    fun toInteractionType(name: String?): InteractionType? =
        name?.let { stored -> InteractionType.entries.firstOrNull { it.name == stored } }

    @TypeConverter
    fun fromInteractionType(type: InteractionType?): String? = type?.name

    @TypeConverter
    fun toRelationshipTag(name: String?): RelationshipTag? =
        name?.let { stored -> RelationshipTag.entries.firstOrNull { it.name == stored } }

    @TypeConverter
    fun fromRelationshipTag(tag: RelationshipTag?): String? = tag?.name
}
