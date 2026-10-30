package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the [EventEntity] table.
 * Supports complete CRUD (Create, Read, Update, Delete) operations and reactive queries.
 */
@Dao
interface EventDao {

    // --- CREATE ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>): List<Long>

    // --- READ ---

    @Query("SELECT * FROM historical_events WHERE landmarkId = :landmarkId ORDER BY year ASC")
    fun getEventsForLandmark(landmarkId: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM historical_events WHERE landmarkId = :landmarkId ORDER BY year ASC")
    suspend fun getEventsForLandmarkOnce(landmarkId: Long): List<EventEntity>

    @Query("SELECT * FROM historical_events ORDER BY year ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM historical_events ORDER BY year ASC")
    suspend fun getAllEventsOnce(): List<EventEntity>

    @Query("SELECT * FROM historical_events WHERE era = :era ORDER BY year ASC")
    fun getEventsByEra(era: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM historical_events WHERE id = :id LIMIT 1")
    fun getEventById(id: Long): Flow<EventEntity?>

    @Query("SELECT * FROM historical_events WHERE id = :id LIMIT 1")
    suspend fun getEventByIdOnce(id: Long): EventEntity?

    @Query("SELECT COUNT(*) FROM historical_events")
    fun getEventCount(): Flow<Int>

    // --- UPDATE ---

    @Update
    suspend fun updateEvent(event: EventEntity): Int

    @Update
    suspend fun updateEvents(events: List<EventEntity>): Int

    // --- DELETE ---

    @Delete
    suspend fun deleteEvent(event: EventEntity): Int

    @Delete
    suspend fun deleteEvents(events: List<EventEntity>): Int

    @Query("DELETE FROM historical_events WHERE id = :id")
    suspend fun deleteEventById(id: Long): Int

    @Query("DELETE FROM historical_events WHERE landmarkId = :landmarkId")
    suspend fun deleteEventsForLandmark(landmarkId: Long): Int

    @Query("DELETE FROM historical_events")
    suspend fun deleteAllEvents(): Int

    @Query("DELETE FROM historical_events WHERE cachedAt < :cutoffTime")
    suspend fun clearOldCache(cutoffTime: Long): Int
}
