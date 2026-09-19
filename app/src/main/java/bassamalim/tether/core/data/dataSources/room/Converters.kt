package bassamalim.tether.core.data.dataSources.room

import androidx.room.TypeConverter
import bassamalim.tether.core.enums.Initiator
import bassamalim.tether.core.enums.InteractionType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class Converters {

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromEpochDay(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    /**
     * A wall clock, stored as seconds against a fixed reference rather than as an instant: a
     * reminder set for seven in the evening should still say seven after a flight.
     */
    @TypeConverter
    fun toEpochSecond(dateTime: LocalDateTime?): Long? = dateTime?.toEpochSecond(ZoneOffset.UTC)

    @TypeConverter
    fun fromEpochSecond(seconds: Long?): LocalDateTime? =
        seconds?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }

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
    fun toInitiator(name: String?): Initiator? =
        name?.let { stored -> Initiator.entries.firstOrNull { it.name == stored } }

    @TypeConverter
    fun fromInitiator(initiator: Initiator?): String? = initiator?.name
}
