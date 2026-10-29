package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.db.HistoricalPlaceDao
import com.example.data.db.HistoricalPlaceEntity
import com.example.data.model.HistoricalPlace
import com.example.data.model.PlaceCategory
import com.example.location.LocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.URLEncoder

class HistoryRepository(
    private val placeDao: HistoricalPlaceDao
) {
    private val mediaWikiApi = ApiClient.mediaWikiService
    private val nominatimApi = ApiClient.nominatimService

    val bookmarkedPlaces: Flow<List<HistoricalPlace>> =
        placeDao.getBookmarkedPlaces().map { list -> list.map { it.toHistoricalPlace() } }

    val visitedPlaces: Flow<List<HistoricalPlace>> =
        placeDao.getVisitedPlaces().map { list -> list.map { it.toHistoricalPlace() } }

    suspend fun reverseGeocode(lat: Double, lon: Double, languageCode: String = "en"): String {
        return withContext(Dispatchers.IO) {
            try {
                val resp = nominatimApi.reverseGeocode(lat, lon, language = languageCode)
                val addr = resp.address
                val locality = addr?.neighbourhood ?: addr?.suburb ?: addr?.road ?: addr?.city ?: addr?.town
                val region = addr?.city ?: addr?.state ?: addr?.country
                if (locality != null && region != null && locality != region) {
                    "$locality, $region"
                } else {
                    resp.displayName?.split(",")?.take(2)?.joinToString(",")?.trim() ?: "Historic District"
                }
            } catch (_: Exception) {
                "Historic District"
            }
        }
    }

    suspend fun fetchNearbyHistory(
        lat: Double,
        lon: Double,
        radiusMeters: Int,
        languageCode: String = "en"
    ): Result<List<HistoricalPlace>> {
        return withContext(Dispatchers.IO) {
            try {
                val endpointUrl = "https://$languageCode.wikipedia.org/w/api.php"
                val coordParam = "$lat|$lon"

                // 1. Call MediaWiki Geosearch API
                val response = mediaWikiApi.searchNearbyLandmarks(
                    endpointUrl = endpointUrl,
                    coord = coordParam,
                    radiusMeters = radiusMeters,
                    limit = 35
                )

                val geoItems = response.query?.geosearch ?: emptyList()

                if (geoItems.isEmpty()) {
                    return@withContext Result.success(emptyList())
                }

                // 2. Concurrently enrich each landmark with summary, extract, and thumbnail
                val placesWithDetails = coroutineScope {
                    geoItems.map { item ->
                        async {
                            val existingEntity = placeDao.getPlaceById(item.pageid)
                            val distance = LocationHelper.calculateDistanceMeters(lat, lon, item.lat, item.lon)
                            val bearing = LocationHelper.calculateBearing(lat, lon, item.lat, item.lon)

                            var desc = ""
                            var extract = ""
                            var thumbUrl: String? = null

                            try {
                                val encodedTitle = URLEncoder.encode(item.title.replace(" ", "_"), "UTF-8")
                                val summaryUrl = "https://$languageCode.wikipedia.org/api/rest_v1/page/summary/$encodedTitle"
                                val summary = mediaWikiApi.getPageSummary(summaryUrl)
                                desc = summary.description ?: ""
                                extract = summary.extract ?: ""
                                thumbUrl = summary.thumbnail?.source ?: summary.originalImage?.source
                            } catch (_: Exception) {
                                desc = existingEntity?.description ?: ""
                                extract = existingEntity?.extract ?: ""
                                thumbUrl = existingEntity?.thumbnailUrl
                            }

                            val category = PlaceCategory.fromText(item.title, desc.ifBlank { extract })

                            HistoricalPlace(
                                pageId = item.pageid,
                                title = item.title,
                                description = desc.ifBlank { "Historical landmark recorded in regional archives." },
                                extract = extract.ifBlank { "Notable historical site in this vicinity." },
                                lat = item.lat,
                                lon = item.lon,
                                distanceMeters = distance,
                                bearingDegrees = bearing,
                                thumbnailUrl = thumbUrl,
                                category = category,
                                isBookmarked = existingEntity?.isBookmarked ?: false,
                                isVisited = existingEntity?.isVisited ?: false,
                                visitedTimestamp = existingEntity?.visitedTimestamp,
                                userNote = existingEntity?.userNote,
                                wikiUrl = "https://$languageCode.wikipedia.org/?curid=${item.pageid}"
                            )
                        }
                    }.awaitAll()
                }

                val sortedPlaces = placesWithDetails.sortedBy { it.distanceMeters }

                // Cache to Room database
                val entitiesToCache = sortedPlaces.map { HistoricalPlaceEntity.fromHistoricalPlace(it) }
                placeDao.insertPlaces(entitiesToCache)

                Result.success(sortedPlaces)
            } catch (e: Exception) {
                try {
                    val cachedEntities = placeDao.getAllPlacesOnce()
                    if (cachedEntities.isNotEmpty()) {
                        val recalculated = cachedEntities.map { entity ->
                            val dist = LocationHelper.calculateDistanceMeters(lat, lon, entity.lat, entity.lon)
                            val bearing = LocationHelper.calculateBearing(lat, lon, entity.lat, entity.lon)
                            entity.toHistoricalPlace().copy(
                                distanceMeters = dist,
                                bearingDegrees = bearing
                            )
                        }.filter { it.distanceMeters <= radiusMeters * 2.0 }
                            .sortedBy { it.distanceMeters }

                        if (recalculated.isNotEmpty()) {
                            return@withContext Result.success(recalculated)
                        }
                    }
                    Result.failure(e)
                } catch (_: Exception) {
                    Result.failure(e)
                }
            }
        }
    }

    suspend fun toggleBookmark(place: HistoricalPlace) {
        withContext(Dispatchers.IO) {
            val newStatus = !place.isBookmarked
            placeDao.setBookmark(place.pageId, newStatus)
            val updated = place.copy(isBookmarked = newStatus)
            placeDao.insertPlace(HistoricalPlaceEntity.fromHistoricalPlace(updated))
        }
    }

    suspend fun toggleVisited(place: HistoricalPlace) {
        withContext(Dispatchers.IO) {
            val newStatus = !place.isVisited
            val timestamp = if (newStatus) System.currentTimeMillis() else null
            placeDao.setVisited(place.pageId, newStatus, timestamp)
            val updated = place.copy(isVisited = newStatus, visitedTimestamp = timestamp)
            placeDao.insertPlace(HistoricalPlaceEntity.fromHistoricalPlace(updated))
        }
    }

    suspend fun saveNote(placeId: Long, note: String?) {
        withContext(Dispatchers.IO) {
            placeDao.updateNote(placeId, note?.ifBlank { null })
        }
    }
}
