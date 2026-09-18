package bassamalim.tether.core.data.dataSources.room

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromEpochDay(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)
}
