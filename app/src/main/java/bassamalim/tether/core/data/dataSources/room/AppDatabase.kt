package bassamalim.tether.core.data.dataSources.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import bassamalim.tether.core.data.dataSources.room.daos.ConnectionsDao
import bassamalim.tether.core.data.dataSources.room.daos.InteractionsDao
import bassamalim.tether.core.data.dataSources.room.daos.PeopleDao
import bassamalim.tether.core.data.dataSources.room.daos.RelationshipTypesDao
import bassamalim.tether.core.data.dataSources.room.daos.RemindersDao
import bassamalim.tether.core.data.dataSources.room.entities.Connection
import bassamalim.tether.core.data.dataSources.room.entities.Interaction
import bassamalim.tether.core.data.dataSources.room.entities.Person
import bassamalim.tether.core.data.dataSources.room.entities.PersonDetail
import bassamalim.tether.core.data.dataSources.room.entities.RelationshipType
import bassamalim.tether.core.data.dataSources.room.entities.Reminder

@Database(
    entities = [
        Person::class,
        PersonDetail::class,
        Interaction::class,
        Connection::class,
        RelationshipType::class,
        Reminder::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun peopleDao(): PeopleDao
    abstract fun interactionsDao(): InteractionsDao
    abstract fun connectionsDao(): ConnectionsDao
    abstract fun relationshipTypesDao(): RelationshipTypesDao
    abstract fun remindersDao(): RemindersDao
}
