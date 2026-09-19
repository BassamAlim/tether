package bassamalim.tether.core.data.dataSources.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * The database is the archive: there is no server to re-fetch from, so every schema change is a
 * migration rather than a destructive rebuild.
 */

/** Adds the connections table — who knows who. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `connections` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `personAId` INTEGER NOT NULL,
                `personBId` INTEGER NOT NULL,
                `label` TEXT NOT NULL,
                FOREIGN KEY(`personAId`) REFERENCES `people`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`personBId`) REFERENCES `people`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_connections_personAId_personBId` " +
                    "ON `connections` (`personAId`, `personBId`)"
        )

        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_connections_personBId` " +
                    "ON `connections` (`personBId`)"
        )
    }
}

/** Adds where a catch-up happened. Existing rows never recorded one, so they keep an empty one. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `interactions` ADD COLUMN `location` TEXT NOT NULL DEFAULT ''")
    }
}

/** Adds who reached out. Rows logged before it were never asked, so they stay unanswered. */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `interactions` ADD COLUMN `initiatedBy` TEXT")
    }
}

/** Every migration the app ships, in order. */
val MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
