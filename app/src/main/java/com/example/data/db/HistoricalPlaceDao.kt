package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoricalPlaceDao {

    @Query("SELECT * FROM historical_places ORDER BY distanceMeters ASC")
    fun getAllPlaces(): Flow<List<HistoricalPlaceEntity>>

    @Query("SELECT * FROM historical_places")
    suspend fun getAllPlacesOnce(): List<HistoricalPlaceEntity>

    @Query("SELECT * FROM historical_places WHERE isBookmarked = 1 ORDER BY title ASC")
    fun getBookmarkedPlaces(): Flow<List<HistoricalPlaceEntity>>

    @Query("SELECT * FROM historical_places WHERE isVisited = 1 ORDER BY visitedTimestamp DESC")
    fun getVisitedPlaces(): Flow<List<HistoricalPlaceEntity>>

    @Query("SELECT * FROM historical_places WHERE pageId = :pageId LIMIT 1")
    suspend fun getPlaceById(pageId: Long): HistoricalPlaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaces(places: List<HistoricalPlaceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: HistoricalPlaceEntity)

    @Update
    suspend fun updatePlace(place: HistoricalPlaceEntity)

    @Query("UPDATE historical_places SET isBookmarked = :bookmarked WHERE pageId = :pageId")
    suspend fun setBookmark(pageId: Long, bookmarked: Boolean)

    @Query("UPDATE historical_places SET isVisited = :visited, visitedTimestamp = :timestamp WHERE pageId = :pageId")
    suspend fun setVisited(pageId: Long, visited: Boolean, timestamp: Long?)

    @Query("UPDATE historical_places SET userNote = :note WHERE pageId = :pageId")
    suspend fun updateNote(pageId: Long, note: String?)

    @Query("DELETE FROM historical_places WHERE isBookmarked = 0 AND isVisited = 0")
    suspend fun clearUnsavedCache()
}
