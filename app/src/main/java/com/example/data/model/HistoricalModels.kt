package com.example.data.model

import androidx.annotation.StringRes
import com.example.R
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * MediaWiki Geosearch Response model:
 * https://en.wikipedia.org/w/api.php?action=query&list=geosearch&format=json&formatversion=2
 */
@JsonClass(generateAdapter = true)
data class MediaWikiGeosearchResponse(
    val batchcomplete: Boolean? = null,
    val query: MediaWikiGeosearchQuery? = null
)

@JsonClass(generateAdapter = true)
data class MediaWikiGeosearchQuery(
    val geosearch: List<MediaWikiGeoItem>? = null
)

@JsonClass(generateAdapter = true)
data class MediaWikiGeoItem(
    val pageid: Long,
    val ns: Int,
    val title: String,
    val lat: Double,
    val lon: Double,
    val dist: Double,
    val primary: Boolean? = null
)

/**
 * MediaWiki Query Pages response model (prop=extracts|pageimages|coordinates|info):
 */
@JsonClass(generateAdapter = true)
data class MediaWikiPagesResponse(
    val batchcomplete: Boolean? = null,
    val query: MediaWikiPagesQuery? = null
)

@JsonClass(generateAdapter = true)
data class MediaWikiPagesQuery(
    val pages: List<MediaWikiPageDetail>? = null
)

@JsonClass(generateAdapter = true)
data class MediaWikiPageDetail(
    val pageid: Long,
    val ns: Int? = null,
    val title: String,
    val extract: String? = null,
    val thumbnail: MediaWikiThumbnail? = null,
    val coordinates: List<MediaWikiCoordinateItem>? = null,
    val fullurl: String? = null,
    val canonicalurl: String? = null,
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class MediaWikiThumbnail(
    val source: String,
    val width: Int? = null,
    val height: Int? = null
)

@JsonClass(generateAdapter = true)
data class MediaWikiCoordinateItem(
    val lat: Double,
    val lon: Double,
    val primary: Boolean? = null,
    val globe: String? = null
)

/**
 * MediaWiki REST API Page Summary model:
 * https://en.wikipedia.org/api/rest_v1/page/summary/{title}
 */
@JsonClass(generateAdapter = true)
data class WikiPageSummary(
    val title: String,
    @Json(name = "displaytitle") val displayTitle: String? = null,
    val description: String? = null,
    val extract: String? = null,
    @Json(name = "extract_html") val extractHtml: String? = null,
    val thumbnail: MediaWikiThumbnail? = null,
    @Json(name = "originalimage") val originalImage: MediaWikiThumbnail? = null,
    val coordinates: WikiCoordinates? = null,
    @Json(name = "content_urls") val contentUrls: WikiContentUrls? = null
)

@JsonClass(generateAdapter = true)
data class WikiCoordinates(
    val lat: Double,
    val lon: Double
)

@JsonClass(generateAdapter = true)
data class WikiContentUrls(
    val desktop: WikiPageUrl? = null,
    val mobile: WikiPageUrl? = null
)

@JsonClass(generateAdapter = true)
data class WikiPageUrl(
    val page: String? = null
)

/**
 * OpenStreetMap Nominatim reverse geocode response
 */
@JsonClass(generateAdapter = true)
data class NominatimReverseResponse(
    @Json(name = "display_name") val displayName: String?,
    val address: NominatimAddress?
)

@JsonClass(generateAdapter = true)
data class NominatimAddress(
    val road: String? = null,
    val suburb: String? = null,
    val neighbourhood: String? = null,
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val state: String? = null,
    val country: String? = null
)

/**
 * Domain representation of a historical landmark on the radar and map.
 */
data class HistoricalPlace(
    val pageId: Long,
    val title: String,
    val description: String,
    val extract: String,
    val lat: Double,
    val lon: Double,
    val distanceMeters: Double,
    val bearingDegrees: Float,
    val thumbnailUrl: String?,
    val category: PlaceCategory,
    val isBookmarked: Boolean = false,
    val isVisited: Boolean = false,
    val visitedTimestamp: Long? = null,
    val userNote: String? = null,
    val wikiUrl: String = "https://en.wikipedia.org/?curid=$pageId"
)

enum class PlaceCategory(
    @StringRes val stringRes: Int,
    val emoji: String
) {
    MONUMENT(R.string.cat_monument, "🏛️"),
    EVENT(R.string.cat_event, "📜"),
    ARCHITECTURE(R.string.cat_architecture, "🏰"),
    SACRED(R.string.cat_sacred, "⛩️"),
    FIGURE(R.string.cat_figure, "👑"),
    GENERAL(R.string.cat_general, "📍");

    companion object {
        fun fromText(title: String, description: String?): PlaceCategory {
            val text = (title + " " + (description ?: "")).lowercase()
            return when {
                text.contains("temple") || text.contains("church") || text.contains("cathedral") ||
                        text.contains("mosque") || text.contains("synagogue") || text.contains("basilica") ||
                        text.contains("abbey") || text.contains("shrine") || text.contains("monastery") ||
                        text.contains("pagoda") || text.contains("chapel") -> SACRED

                text.contains("castle") || text.contains("palace") || text.contains("tower") ||
                        text.contains("fort") || text.contains("gate") || text.contains("bridge") ||
                        text.contains("wall") || text.contains("building") || text.contains("theatre") ||
                        text.contains("theater") || text.contains("colosseum") || text.contains("arena") -> ARCHITECTURE

                text.contains("monument") || text.contains("memorial") || text.contains("statue") ||
                        text.contains("fountain") || text.contains("column") || text.contains("obelisk") ||
                        text.contains("plaque") || text.contains("sculpture") || text.contains("cenotaph") -> MONUMENT

                text.contains("battle") || text.contains("treaty") || text.contains("riot") ||
                        text.contains("siege") || text.contains("revolution") || text.contains("revolt") ||
                        text.contains("massacre") || text.contains("signing") || text.contains("declaration") ||
                        text.contains("historic site") || text.contains("historical marker") -> EVENT

                text.contains("king") || text.contains("queen") || text.contains("emperor") ||
                        text.contains("general") || text.contains("philosopher") || text.contains("poet") ||
                        text.contains("statesman") || text.contains("president") || text.contains("writer") ||
                        text.contains("artist") || text.contains("saint") -> FIGURE

                else -> GENERAL
            }
        }
    }
}
