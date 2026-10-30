package com.example.data.repository

import com.example.data.db.EventDao
import com.example.data.db.EventEntity
import com.example.data.db.LandmarkDao
import com.example.data.db.LandmarkEntity
import com.example.data.db.LandmarkWithEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository class managing data operations for [LandmarkEntity] and [EventEntity],
 * coordinating with [LandmarkDao] and [EventDao] to provide offline caching,
 * CRUD operations, and reactive data streams to the ViewModel.
 */
class LandmarkRepository(
    private val landmarkDao: LandmarkDao,
    private val eventDao: EventDao
) {

    // --- Reactive Data Streams ---

    val allLandmarks: Flow<List<LandmarkEntity>> = landmarkDao.getAllLandmarks()

    val bookmarkedLandmarks: Flow<List<LandmarkEntity>> = landmarkDao.getBookmarkedLandmarks()

    val visitedLandmarks: Flow<List<LandmarkEntity>> = landmarkDao.getVisitedLandmarks()

    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()

    val landmarksWithEvents: Flow<List<LandmarkWithEvents>> = landmarkDao.getLandmarksWithEvents()

    // --- Query Operations ---

    fun getLandmarkById(id: Long): Flow<LandmarkEntity?> = landmarkDao.getLandmarkById(id)

    suspend fun getLandmarkByIdOnce(id: Long): LandmarkEntity? = withContext(Dispatchers.IO) {
        landmarkDao.getLandmarkByIdOnce(id)
    }

    fun getLandmarkWithEvents(id: Long): Flow<LandmarkWithEvents?> =
        landmarkDao.getLandmarkWithEventsById(id)

    fun getEventsForLandmark(landmarkId: Long): Flow<List<EventEntity>> =
        eventDao.getEventsForLandmark(landmarkId)

    fun searchLandmarks(query: String): Flow<List<LandmarkEntity>> =
        landmarkDao.searchLandmarks(query)

    fun getLandmarksByCategory(category: String): Flow<List<LandmarkEntity>> =
        landmarkDao.getLandmarksByCategory(category)

    fun getEventsByEra(era: String): Flow<List<EventEntity>> =
        eventDao.getEventsByEra(era)

    // --- Landmark Mutation Operations (CRUD) ---

    suspend fun insertLandmark(landmark: LandmarkEntity): Long = withContext(Dispatchers.IO) {
        landmarkDao.insertLandmark(landmark)
    }

    suspend fun insertLandmarks(landmarks: List<LandmarkEntity>): List<Long> = withContext(Dispatchers.IO) {
        landmarkDao.insertLandmarks(landmarks)
    }

    suspend fun updateLandmark(landmark: LandmarkEntity): Int = withContext(Dispatchers.IO) {
        landmarkDao.updateLandmark(landmark)
    }

    suspend fun deleteLandmark(landmark: LandmarkEntity): Int = withContext(Dispatchers.IO) {
        landmarkDao.deleteLandmark(landmark)
    }

    suspend fun deleteLandmarkById(id: Long): Int = withContext(Dispatchers.IO) {
        landmarkDao.deleteLandmarkById(id)
    }

    suspend fun toggleBookmark(landmarkId: Long, isBookmarked: Boolean) = withContext(Dispatchers.IO) {
        landmarkDao.setBookmark(landmarkId, isBookmarked)
    }

    suspend fun setVisited(landmarkId: Long, isVisited: Boolean, timestamp: Long? = System.currentTimeMillis()) =
        withContext(Dispatchers.IO) {
            landmarkDao.setVisited(landmarkId, isVisited, if (isVisited) timestamp else null)
        }

    suspend fun updateNotes(landmarkId: Long, notes: String?) = withContext(Dispatchers.IO) {
        landmarkDao.updateNotes(landmarkId, notes)
    }

    // --- Event Mutation Operations (CRUD) ---

    suspend fun insertEvent(event: EventEntity): Long = withContext(Dispatchers.IO) {
        eventDao.insertEvent(event)
    }

    suspend fun insertEvents(events: List<EventEntity>): List<Long> = withContext(Dispatchers.IO) {
        eventDao.insertEvents(events)
    }

    suspend fun updateEvent(event: EventEntity): Int = withContext(Dispatchers.IO) {
        eventDao.updateEvent(event)
    }

    suspend fun deleteEvent(event: EventEntity): Int = withContext(Dispatchers.IO) {
        eventDao.deleteEvent(event)
    }

    suspend fun deleteEventsForLandmark(landmarkId: Long): Int = withContext(Dispatchers.IO) {
        eventDao.deleteEventsForLandmark(landmarkId)
    }

    // --- Cache Maintenance ---

    suspend fun clearOldCache(cutoffTime: Long) = withContext(Dispatchers.IO) {
        landmarkDao.clearOldUnsavedCache(cutoffTime)
        eventDao.clearOldCache(cutoffTime)
    }
}
