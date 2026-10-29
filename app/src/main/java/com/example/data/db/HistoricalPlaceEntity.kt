package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.HistoricalPlace
import com.example.data.model.PlaceCategory

@Entity(tableName = "historical_places")
data class HistoricalPlaceEntity(
    @PrimaryKey val pageId: Long,
    val title: String,
    val description: String,
    val extract: String,
    val lat: Double,
    val lon: Double,
    val distanceMeters: Double,
    val bearingDegrees: Float,
    val thumbnailUrl: String?,
    val category: String,
    val isBookmarked: Boolean = false,
    val isVisited: Boolean = false,
    val visitedTimestamp: Long? = null,
    val userNote: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toHistoricalPlace(): HistoricalPlace {
        val cat = try {
            PlaceCategory.valueOf(category)
        } catch (_: Exception) {
            PlaceCategory.GENERAL
        }
        return HistoricalPlace(
            pageId = pageId,
            title = title,
            description = description,
            extract = extract,
            lat = lat,
            lon = lon,
            distanceMeters = distanceMeters,
            bearingDegrees = bearingDegrees,
            thumbnailUrl = thumbnailUrl,
            category = cat,
            isBookmarked = isBookmarked,
            isVisited = isVisited,
            visitedTimestamp = visitedTimestamp,
            userNote = userNote,
            wikiUrl = "https://en.wikipedia.org/?curid=$pageId"
        )
    }

    companion object {
        fun fromHistoricalPlace(place: HistoricalPlace): HistoricalPlaceEntity {
            return HistoricalPlaceEntity(
                pageId = place.pageId,
                title = place.title,
                description = place.description,
                extract = place.extract,
                lat = place.lat,
                lon = place.lon,
                distanceMeters = place.distanceMeters,
                bearingDegrees = place.bearingDegrees,
                thumbnailUrl = place.thumbnailUrl,
                category = place.category.name,
                isBookmarked = place.isBookmarked,
                isVisited = place.isVisited,
                visitedTimestamp = place.visitedTimestamp,
                userNote = place.userNote
            )
        }
    }
}
