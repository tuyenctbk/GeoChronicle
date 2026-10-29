package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoricalPlaceEntity::class], version = 1, exportSchema = false)
abstract class GeoChronicleDatabase : RoomDatabase() {
    abstract fun historicalPlaceDao(): HistoricalPlaceDao

    companion object {
        @Volatile
        private var INSTANCE: GeoChronicleDatabase? = null

        fun getInstance(context: Context): GeoChronicleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GeoChronicleDatabase::class.java,
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
