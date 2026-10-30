package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the [LandmarkEntity] table.
 * Supports complete CRUD (Create, Read, Update, Delete) operations and reactive queries.
 */
@Dao
interface LandmarkDao {

    // --- CREATE ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLandmark(landmark: LandmarkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLandmarks(landmarks: List<LandmarkEntity>): List<Long>

    // --- READ ---

    @Query("SELECT * FROM landmarks ORDER BY title ASC")
    fun getAllLandmarks(): Flow<List<LandmarkEntity>>

    @Query("SELECT * FROM landmarks ORDER BY title ASC")
    suspend fun getAllLandmarksOnce(): List<LandmarkEntity>

    @Query("SELECT * FROM landmarks WHERE id = :id LIMIT 1")
    fun getLandmarkById(id: Long): Flow<LandmarkEntity?>

    @Query("SELECT * FROM landmarks WHERE id = :id LIMIT 1")
    suspend fun getLandmarkByIdOnce(id: Long): LandmarkEntity?

    @Query("SELECT * FROM landmarks WHERE isBookmarked = 1 ORDER BY title ASC")
    fun getBookmarkedLandmarks(): Flow<List<LandmarkEntity>>

    @Query("SELECT * FROM landmarks WHERE isVisited = 1 ORDER BY visitedTimestamp DESC")
    fun getVisitedLandmarks(): Flow<List<LandmarkEntity>>

    @Query("SELECT * FROM landmarks WHERE category = :category ORDER BY title ASC")
    fun getLandmarksByCategory(category: String): Flow<List<LandmarkEntity>>

    @Query("SELECT * FROM landmarks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR historicalDetails LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchLandmarks(query: String): Flow<List<LandmarkEntity>>

    @Query("SELECT COUNT(*) FROM landmarks")
    fun getLandmarkCount(): Flow<Int>

    // --- READ RELATIONS ---

    @Transaction
    @Query("SELECT * FROM landmarks ORDER BY title ASC")
    fun getLandmarksWithEvents(): Flow<List<LandmarkWithEvents>>

    @Transaction
    @Query("SELECT * FROM landmarks WHERE id = :id LIMIT 1")
    fun getLandmarkWithEventsById(id: Long): Flow<LandmarkWithEvents?>

    // --- UPDATE ---

    @Update
    suspend fun updateLandmark(landmark: LandmarkEntity): Int

    @Update
    suspend fun updateLandmarks(landmarks: List<LandmarkEntity>): Int

    @Query("UPDATE landmarks SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun setBookmark(id: Long, bookmarked: Boolean)

    @Query("UPDATE landmarks SET isVisited = :visited, visitedTimestamp = :timestamp WHERE id = :id")
    suspend fun setVisited(id: Long, visited: Boolean, timestamp: Long?)

    @Query("UPDATE landmarks SET userNotes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String?)

    // --- DELETE ---

    @Delete
    suspend fun deleteLandmark(landmark: LandmarkEntity): Int

    @Delete
    suspend fun deleteLandmarks(landmarks: List<LandmarkEntity>): Int

    @Query("DELETE FROM landmarks WHERE id = :id")
    suspend fun deleteLandmarkById(id: Long): Int

    @Query("DELETE FROM landmarks")
    suspend fun deleteAllLandmarks(): Int

    @Query("DELETE FROM landmarks WHERE isBookmarked = 0 AND isVisited = 0 AND cachedAt < :cutoffTime")
    suspend fun clearOldUnsavedCache(cutoffTime: Long): Int
}
