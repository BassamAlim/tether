package bassamalim.tether.core.data.dataSources.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import bassamalim.tether.core.data.dataSources.room.daos.InteractionsDao
import bassamalim.tether.core.data.dataSources.room.daos.PeopleDao
import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import bassamalim.tether.core.data.dataSources.room.entities.Person

@Database(
    entities = [
        Person::class,
        Interaction::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun peopleDao(): PeopleDao
    abstract fun interactionsDao(): InteractionsDao
}
