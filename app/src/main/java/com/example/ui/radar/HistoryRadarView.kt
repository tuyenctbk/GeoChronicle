package com.example.ui.radar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.data.db.EventEntity
import com.example.data.model.HistoricalPlace
import com.example.data.model.PlaceCategory
import com.example.location.HistoricalTourLocation
import com.example.ui.components.RadarView

/**
 * Historical Radar View wrapper that passes user coordinates and nearby places/events
 * to [RadarView] for custom canvas rendering.
 */
@Composable
fun HistoryRadarView(
    places: List<HistoricalPlace>,
    selectedPlace: HistoricalPlace?,
    radiusMeters: Int,
    onPlaceSelected: (HistoricalPlace) -> Unit,
    events: List<EventEntity> = emptyList(),
    currentLocation: HistoricalTourLocation? = null,
    showSuggestedRoute: Boolean = true,
    modifier: Modifier = Modifier
) {
    val userLat = currentLocation?.lat ?: (places.firstOrNull()?.lat ?: 41.8902)
    val userLon = currentLocation?.lon ?: (places.firstOrNull()?.lon ?: 12.4922)

    RadarView(
        userLat = userLat,
        userLon = userLon,
        landmarks = places,
        events = events,
        radiusMeters = radiusMeters,
        selectedLandmarkId = selectedPlace?.pageId,
        onLandmarkSelected = onPlaceSelected,
        showWalkingRoute = showSuggestedRoute,
        modifier = modifier
    )
}

fun getCategoryColor(category: PlaceCategory): Color {
    return when (category) {
        PlaceCategory.MONUMENT -> Color(0xFFFFD56B)
        PlaceCategory.EVENT -> Color(0xFFFF6B6B)
        PlaceCategory.SACRED -> Color(0xFF5CE1E6)
        PlaceCategory.ARCHITECTURE -> Color(0xFFFFA94D)
        PlaceCategory.FIGURE -> Color(0xFFB197FC)
        PlaceCategory.GENERAL -> Color(0xFFD4C8B8)
    }
}
