package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Main application Room database integrating [LandmarkEntity], [EventEntity],
 * and [HistoricalPlaceEntity], providing DAOs for full offline caching.
 */
@Database(
    entities = [
        HistoricalPlaceEntity::class,
        LandmarkEntity::class,
        EventEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun landmarkDao(): LandmarkDao
    abstract fun eventDao(): EventDao
    abstract fun historicalPlaceDao(): HistoricalPlaceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "geochronicle_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Typealias for backward compatibility across the app.
 */
typealias GeoChronicleDatabase = AppDatabase
