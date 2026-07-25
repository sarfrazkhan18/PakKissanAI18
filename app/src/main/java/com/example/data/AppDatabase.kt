package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// exportSchema is enabled so every version's schema is captured under app/schemas/. Those
// JSON snapshots are what real Room Migration objects are written and tested against — see
// build.gradle.kts (room.schemaLocation). Add a Migration to MIGRATIONS below on every future
// version bump so a farmer's profile and chat history survive app updates.
@Database(entities = [ChatSession::class, ChatMessage::class, UserProfile::class, AgriKnowledge::class], version = 6, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kisaanDao(): KisaanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // v4 -> v5: add the My Farm profile columns (P2.1/P2.2). Additive and non-destructive,
        // so existing farmers keep their profile and full chat history across the update.
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN district TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN cropVariety TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN landArea TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN areaUnit TEXT NOT NULL DEFAULT 'Acre'")
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN sowingDateMillis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN irrigationSource TEXT NOT NULL DEFAULT ''")
            }
        }

        // v5 -> v6: safety-layer columns on chat_messages (P2.5). Additive, non-destructive.
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE chat_messages ADD COLUMN usedVerifiedSource INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE chat_messages ADD COLUMN feedback INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Register every schema migration here as the database evolves. Never replace this with
        // destructive migration for released versions: that wipes the farmer's on-device
        // profile and full conversation history, which has no backup.
        private val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_4_5, MIGRATION_5_6)

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
