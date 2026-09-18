package bassamalim.tether.core.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import bassamalim.tether.core.data.dataSources.room.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "preferences"
)

/**
 * Everything Tether stores lives on the device: a Room database and a DataStore. There is no
 * server, which is exactly why the Settings screen has to be honest about backups.
 *
 * Repositories aren't listed here — they're `@Singleton class ... @Inject constructor`, so Hilt
 * builds them without a module.
 */
@Module @InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides @Singleton
    fun provideAppDatabase(application: Application): AppDatabase =
        Room.databaseBuilder(application, AppDatabase::class.java, "tether.db").build()

    @Provides @Singleton
    fun providePeopleDao(database: AppDatabase) = database.peopleDao()

    @Provides @Singleton
    fun provideInteractionsDao(database: AppDatabase) = database.interactionsDao()

    @Provides @Singleton
    fun providePreferencesDataStore(application: Application): DataStore<Preferences> =
        application.preferencesDataStore

}
