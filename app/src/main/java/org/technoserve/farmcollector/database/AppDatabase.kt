package org.technoserve.farmcollector.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.technoserve.farmcollector.database.converters.AccuracyListConvert
import org.technoserve.farmcollector.database.converters.CoordinateListConvert
import org.technoserve.farmcollector.database.converters.DateConverter
import org.technoserve.farmcollector.database.dao.FarmDAO
import org.technoserve.farmcollector.database.helpers.ContextProvider
import org.technoserve.farmcollector.database.helpers.MigrationHelper
import org.technoserve.farmcollector.database.models.CollectionSite
import org.technoserve.farmcollector.database.models.Farm

/**
 * This class is used to create app database and to run migrations from one db version to another
 */

@Database(entities = [Farm::class, CollectionSite::class], version = 20, exportSchema = true)
@TypeConverters(CoordinateListConvert::class, AccuracyListConvert::class, DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun farmsDAO(): FarmDAO

    companion object {
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_12_16 = object : Migration(12, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val context = ContextProvider.getContext()
                MigrationHelper(context).executeSqlFromFile(db, "migration_12_16.sql")
            }
        }



        // Define a migration from version 15 to 16
        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val context = ContextProvider.getContext()
                MigrationHelper(context).executeSqlFromFile(db, "migration_15_16.sql")
            }
        }


        // Define a migration from version 16 to 17
        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val context = ContextProvider.getContext()
                MigrationHelper(context).executeSqlFromFile(db, "migration_16_17.sql")
            }
        }

        // Define a migration from version 17 to 18
        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val context = ContextProvider.getContext()
                MigrationHelper(context).executeSqlFromFile(db, "migration_17_18.sql")
            }

        }

        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val context = ContextProvider.getContext()
                MigrationHelper(context).executeSqlFromFile(db, "migration_18_19.sql")
            }

        }

        private val MIGRATION_19_20 = object : Migration(19, 20) {

            override fun migrate(db: SupportSQLiteDatabase) {
                val context = ContextProvider.getContext()
                MigrationHelper(context).executeSqlFromFile(db, "migration_19_20.sql")
            }
        }


        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "farm_collector_database"
                    )
                        .addMigrations(
                            MIGRATION_12_16,
                            MIGRATION_15_16,
                            MIGRATION_16_17,
                            MIGRATION_17_18,
                            MIGRATION_18_19,
                            MIGRATION_19_20
                        )
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}
