package bassamalim.tether.core.data.dataSources.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import bassamalim.tether.core.domain.RelationshipTypes

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

/**
 * Unifies the per-person relationship tag and the per-connection label into one vocabulary.
 *
 * `people.tag` held enum names; it now holds the label itself, so stored rows are rewritten to
 * the words they used to stand for. Labels already typed into connections join the list, because
 * the point of the list is that it's yours — it would be odd for the ones you'd written to be
 * the only ones missing.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `relationship_types` " +
                    "(`label` TEXT NOT NULL COLLATE NOCASE, PRIMARY KEY(`label`))"
        )

        for ((name, label) in RelationshipTypes.LEGACY_TAG_NAMES)
            db.execSQL("UPDATE `people` SET `tag` = ? WHERE `tag` = ?", arrayOf(label, name))

        db.execSQL(
            """
            INSERT OR IGNORE INTO `relationship_types` (`label`)
            SELECT DISTINCT TRIM(`label`) FROM `connections` WHERE TRIM(`label`) <> ''
            """.trimIndent()
        )
    }
}

/** Adds per-person reminders: one pending alarm per person, hence the unique index. */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `reminders` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `personId` INTEGER NOT NULL,
                `type` TEXT,
                `scheduledFor` INTEGER NOT NULL,
                FOREIGN KEY(`personId`) REFERENCES `people`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_reminders_personId` " +
                    "ON `reminders` (`personId`)"
        )
    }
}

/** Adds where someone works and their role there. Nobody has said yet, so both start empty. */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `people` ADD COLUMN `workplace` TEXT")
        db.execSQL("ALTER TABLE `people` ADD COLUMN `jobTitle` TEXT")
    }
}

/** Every migration the app ships, in order. */
val MIGRATIONS = arrayOf(
    MIGRATION_1_2,
    MIGRATION_2_3,
    MIGRATION_3_4,
    MIGRATION_4_5,
    MIGRATION_5_6,
    MIGRATION_6_7
)
