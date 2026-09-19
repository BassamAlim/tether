package bassamalim.tether.core.data.dataSources.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import bassamalim.tether.core.data.dataSources.room.daos.ConnectionsDao
import bassamalim.tether.core.data.dataSources.room.daos.InteractionsDao
import bassamalim.tether.core.data.dataSources.room.daos.PeopleDao
import bassamalim.tether.core.data.dataSources.room.entities.Connection
import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import bassamalim.tether.core.data.dataSources.room.entities.Person
import bassamalim.tether.core.data.dataSources.room.entities.PersonDetail

@Database(
    entities = [
        Person::class,
        PersonDetail::class,
        Interaction::class,
        Connection::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun peopleDao(): PeopleDao
    abstract fun interactionsDao(): InteractionsDao
    abstract fun connectionsDao(): ConnectionsDao
}
