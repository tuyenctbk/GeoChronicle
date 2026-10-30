package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.HistoricalPlace
import com.example.data.model.PlaceCategory

/**
 * Room database entity representing a historical landmark cached for offline exploration.
 */
@Entity(
    tableName = "landmarks",
    indices = [
        Index(value = ["isBookmarked"]),
        Index(value = ["isVisited"]),
        Index(value = ["category"])
    ]
)
data class LandmarkEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val description: String,
    val historicalDetails: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val thumbnailUrl: String? = null,
    val era: String? = null,
    val architecturalStyle: String? = null,
    val address: String? = null,
    val isBookmarked: Boolean = false,
    val isVisited: Boolean = false,
    val visitedTimestamp: Long? = null,
    val userNotes: String? = null,
    val wikiUrl: String = "https://en.wikipedia.org/?curid=$id",
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toHistoricalPlace(): HistoricalPlace {
        val cat = try {
            PlaceCategory.valueOf(category)
        } catch (_: Exception) {
            PlaceCategory.GENERAL
        }
        return HistoricalPlace(
            pageId = id,
            title = title,
            description = description,
            extract = historicalDetails,
            lat = latitude,
            lon = longitude,
            distanceMeters = 0.0,
            bearingDegrees = 0f,
            thumbnailUrl = thumbnailUrl,
            category = cat,
            isBookmarked = isBookmarked,
            isVisited = isVisited,
            visitedTimestamp = visitedTimestamp,
            userNote = userNotes,
            wikiUrl = wikiUrl
        )
    }

    companion object {
        fun fromHistoricalPlace(place: HistoricalPlace, era: String? = null, address: String? = null): LandmarkEntity {
            return LandmarkEntity(
                id = place.pageId,
                title = place.title,
                description = place.description,
                historicalDetails = place.extract,
                latitude = place.lat,
                longitude = place.lon,
                category = place.category.name,
                thumbnailUrl = place.thumbnailUrl,
                era = era,
                address = address,
                isBookmarked = place.isBookmarked,
                isVisited = place.isVisited,
                visitedTimestamp = place.visitedTimestamp,
                userNotes = place.userNote,
                wikiUrl = place.wikiUrl
            )
        }
    }
}
