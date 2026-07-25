package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration

// exportSchema is enabled so every version's schema is captured under app/schemas/. Those
// JSON snapshots are what real Room Migration objects are written and tested against — see
// build.gradle.kts (room.schemaLocation). Add a Migration to MIGRATIONS below on every future
// version bump so a farmer's profile and chat history survive app updates.
@Database(entities = [ChatSession::class, ChatMessage::class, UserProfile::class, AgriKnowledge::class], version = 4, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kisaanDao(): KisaanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Register schema migrations here as the database evolves past version 4, e.g.
        //   val MIGRATION_4_5 = object : Migration(4, 5) { override fun migrate(db) { ... } }
        // Never replace this with destructive migration for released versions: doing so wipes
        // the farmer's on-device profile and full conversation history, which has no backup.
        private val MIGRATIONS: Array<Migration> = arrayOf()

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kisaan_database"
                )
                .addMigrations(*MIGRATIONS)
                // Only rebuild for the pre-release dev schemas (1-3, before passcode hashing).
                // From version 4 onward a missing migration throws loudly in development
                // instead of silently destroying production data.
                .fallbackToDestructiveMigrationFrom(dropAllTables = true, 1, 2, 3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
