package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.db.EventEntity
import com.example.data.model.HistoricalPlace
import com.example.data.model.PlaceCategory
import com.example.location.LocationHelper
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

/**
 * RadarView composable that uses canvas drawing to visualize nearby landmarks and historical events
 * relative to the user's current GPS location.
 */
@Composable
fun RadarView(
    userLat: Double,
    userLon: Double,
    landmarks: List<HistoricalPlace>,
    events: List<EventEntity> = emptyList(),
    radiusMeters: Int = 1000,
    selectedLandmarkId: Long? = null,
    selectedEventId: Long? = null,
    onLandmarkSelected: ((HistoricalPlace) -> Unit)? = null,
    onEventSelected: ((EventEntity) -> Unit)? = null,
    showWalkingRoute: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarCanvasTransition")

    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarSweepAngle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "UserPulseScale"
    )

    val routeDashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -36f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RouteDashPhase"
    )

    // Compute relative distance and bearing for each landmark relative to user GPS
    val computedLandmarks = landmarks.map { place ->
        val dist = LocationHelper.calculateDistanceMeters(userLat, userLon, place.lat, place.lon)
        val bearing = LocationHelper.calculateBearing(userLat, userLon, place.lat, place.lon)
        place.copy(distanceMeters = dist, bearingDegrees = bearing)
    }

    // Map landmark ID to place for event coordinates lookup
    val landmarkMap = computedLandmarks.associateBy { it.pageId }

    val routeLabel = stringResource(R.string.route_trail)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(12.dp)
            .testTag("radar_canvas_view")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(computedLandmarks, events, radiusMeters) {
                    detectTapGestures { tapOffset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radarRadius = min(size.width, size.height) / 2f * 0.88f

                        var closestLandmark: HistoricalPlace? = null
                        var minDistanceToTap = Float.MAX_VALUE

                        // Check tap against landmarks
                        for (place in computedLandmarks) {
                            val normalizedDist = (place.distanceMeters / radiusMeters).toFloat().coerceIn(0f, 1f)
                            val r = normalizedDist * radarRadius
                            val rad = Math.toRadians((place.bearingDegrees - 90.0)).toFloat()
                            val blipX = center.x + r * cos(rad)
                            val blipY = center.y + r * sin(rad)

                            val distToTap = hypot(tapOffset.x - blipX, tapOffset.y - blipY)
                            if (distToTap < 36.dp.toPx() && distToTap < minDistanceToTap) {
                                minDistanceToTap = distToTap
                                closestLandmark = place
                            }
                        }

                        if (closestLandmark != null && onLandmarkSelected != null) {
                            onLandmarkSelected(closestLandmark)
                            return@detectTapGestures
                        }

                        // Check tap against events
                        var closestEvent: EventEntity? = null
                        var minEventDist = Float.MAX_VALUE

                        for (evt in events) {
                            val parentLandmark = evt.landmarkId?.let { landmarkMap[it] } ?: continue
                            val normalizedDist = (parentLandmark.distanceMeters / radiusMeters).toFloat().coerceIn(0f, 1f)
                            val r = normalizedDist * radarRadius
                            // Offset event blip slightly from parent landmark
                            val rad = Math.toRadians((parentLandmark.bearingDegrees - 75.0)).toFloat()
                            val blipX = center.x + r * cos(rad)
                            val blipY = center.y + r * sin(rad)

                            val distToTap = hypot(tapOffset.x - blipX, tapOffset.y - blipY)
                            if (distToTap < 36.dp.toPx() && distToTap < minEventDist) {
                                minEventDist = distToTap
                                closestEvent = evt
                            }
                        }

                        if (closestEvent != null && onEventSelected != null) {
                            onEventSelected(closestEvent)
                        }
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radarRadius = min(size.width, size.height) / 2f * 0.88f

            // 1. Dark Obsidian Radar Radial Background
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF14241E),
                        Color(0xFF0F1A17),
                        Color(0xFF080F0D)
                    ),
                    center = center,
                    radius = radarRadius
                ),
                radius = radarRadius,
                center = center
            )

            // 2. Concentric Range Circles
            val ringFractions = listOf(0.25f, 0.5f, 0.75f, 1.0f)
            ringFractions.forEach { frac ->
                val r = radarRadius * frac
                val isOuter = frac == 1.0f
                drawCircle(
                    color = if (isOuter) Color(0xFFD4A359) else Color(0x33D4A359),
                    radius = r,
                    center = center,
                    style = Stroke(
                        width = if (isOuter) 2.dp.toPx() else 1.dp.toPx(),
                        pathEffect = if (!isOuter && frac == 0.5f) null else PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                )

                // Distance ring label
                val labelDist = (radiusMeters * frac).toInt()
                val labelText = if (labelDist >= 1000) "${labelDist / 1000}km" else "${labelDist}m"
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(170, 212, 163, 89)
                        textSize = 26f
                        textAlign = android.graphics.Paint.Align.LEFT
                        isAntiAlias = true
                    }
                    drawText(labelText, center.x + 8f, center.y - r + 22f, paint)
                }
            }

            // 3. Radar Crosshairs
            drawLine(
                color = Color(0x22D4A359),
                start = Offset(center.x, center.y - radarRadius),
                end = Offset(center.x, center.y + radarRadius),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color(0x22D4A359),
                start = Offset(center.x - radarRadius, center.y),
                end = Offset(center.x + radarRadius, center.y),
                strokeWidth = 1.dp.toPx()
            )

            // 4. Cardinal Directions (N, NE, E, SE, S, SW, W, NW)
            drawCardinalDirections(center, radarRadius)

            // 5. Walking Tour Route Visualizer
            if (showWalkingRoute && computedLandmarks.isNotEmpty()) {
                val routePoints = mutableListOf<Offset>()
                routePoints.add(center) // User location start point

                val sortedPlaces = computedLandmarks.take(6).sortedBy { it.distanceMeters }
                sortedPlaces.forEach { place ->
                    val normalizedDist = (place.distanceMeters / radiusMeters).toFloat().coerceIn(0f, 1f)
                    val r = normalizedDist * radarRadius
                    val rad = Math.toRadians((place.bearingDegrees - 90.0)).toFloat()
                    routePoints.add(Offset(center.x + r * cos(rad), center.y + r * sin(rad)))
                }

                // Draw path line segments
                for (i in 0 until routePoints.size - 1) {
                    val p1 = routePoints[i]
                    val p2 = routePoints[i + 1]
                    val placeObj = sortedPlaces.getOrNull(i)
                    val isVisited = placeObj?.isVisited == true
                    val pathColor = if (isVisited) Color(0xFF4CAF50) else Color(0xFF5CE1E6)

                    // Outer glow
                    drawLine(
                        color = pathColor.copy(alpha = 0.25f),
                        start = p1,
                        end = p2,
                        strokeWidth = 6.dp.toPx()
                    )

                    // Dashed animated line
                    drawLine(
                        color = pathColor,
                        start = p1,
                        end = p2,
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(14f, 10f),
                            phase = routeDashPhase
                        )
                    )
                }
            }

            // 6. Rotating Radar Sweep Beam
            val sweepRad = Math.toRadians((sweepAngle - 90.0)).toFloat()
            val sweepEnd = Offset(
                center.x + radarRadius * cos(sweepRad),
                center.y + radarRadius * sin(sweepRad)
            )
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0x00FFD56B), Color(0xCCFFD56B)),
                    start = center,
                    end = sweepEnd
                ),
                start = center,
                end = sweepEnd,
                strokeWidth = 2.dp.toPx()
            )

            drawArc(
                brush = Brush.sweepGradient(
                    0.0f to Color(0x00FFD56B),
                    0.08f to Color(0x26FFD56B),
                    1.0f to Color(0x00FFD56B),
                    center = center
                ),
                startAngle = sweepAngle - 30f,
                sweepAngle = 30f,
                useCenter = true,
                topLeft = Offset(center.x - radarRadius, center.y - radarRadius),
                size = Size(radarRadius * 2, radarRadius * 2)
            )

            // 7. Center User Beacon (Current GPS Position)
            drawCircle(
                color = Color(0x44D4A359),
                radius = 16.dp.toPx() * pulseScale,
                center = center
            )
            drawCircle(
                color = Color(0xFFD4A359),
                radius = 7.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = center
            )

            // 8. Draw Historical Landmark Blips
            val routeOrderMap = computedLandmarks.take(6).sortedBy { it.distanceMeters }
                .mapIndexed { idx, p -> p.pageId to (idx + 1) }.toMap()

            computedLandmarks.forEach { place ->
                val normalizedDist = (place.distanceMeters / radiusMeters).toFloat().coerceIn(0f, 1f)
                val r = normalizedDist * radarRadius
                val rad = Math.toRadians((place.bearingDegrees - 90.0)).toFloat()
                val blipCenter = Offset(center.x + r * cos(rad), center.y + r * sin(rad))

                val isSelected = selectedLandmarkId == place.pageId
                val blipColor = getCategoryColor(place.category)
                val routeOrder = routeOrderMap[place.pageId]

                if (isSelected) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.35f),
                        radius = 20.dp.toPx() * pulseScale,
                        center = blipCenter
                    )
                    drawCircle(
                        color = Color(0xFFFFD56B),
                        radius = 13.dp.toPx(),
                        center = blipCenter,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                drawCircle(
                    color = blipColor.copy(alpha = if (isSelected) 0.6f else 0.3f),
                    radius = if (isSelected) 10.dp.toPx() else 7.dp.toPx(),
                    center = blipCenter
                )

                drawCircle(
                    color = if (isSelected) Color.White else blipColor,
                    radius = if (isSelected) 5.dp.toPx() else 4.dp.toPx(),
                    center = blipCenter
                )

                // Route order number badge
                if (showWalkingRoute && routeOrder != null) {
                    drawCircle(
                        color = Color(0xFF5CE1E6),
                        radius = 7.5f.dp.toPx(),
                        center = Offset(blipCenter.x - 9.dp.toPx(), blipCenter.y - 9.dp.toPx())
                    )
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 18f
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        drawText(
                            routeOrder.toString(),
                            blipCenter.x - 9.dp.toPx(),
                            blipCenter.y - 3.dp.toPx(),
                            paint
                        )
                    }
                }
            }

            // 9. Draw Room Historical Event Blips (Diamond shape)
            events.forEach { evt ->
                val parentLandmark = evt.landmarkId?.let { landmarkMap[it] }
                if (parentLandmark != null) {
                    val normalizedDist = (parentLandmark.distanceMeters / radiusMeters).toFloat().coerceIn(0f, 1f)
                    val r = normalizedDist * radarRadius
                    val rad = Math.toRadians((parentLandmark.bearingDegrees - 75.0)).toFloat()
                    val eventCenter = Offset(center.x + r * cos(rad), center.y + r * sin(rad))

                    val isSelected = selectedEventId == evt.id
                    val diamondSize = if (isSelected) 12.dp.toPx() else 8.dp.toPx()

                    val diamondPath = Path().apply {
                        moveTo(eventCenter.x, eventCenter.y - diamondSize)
                        lineTo(eventCenter.x + diamondSize, eventCenter.y)
                        lineTo(eventCenter.x, eventCenter.y + diamondSize)
                        lineTo(eventCenter.x - diamondSize, eventCenter.y)
                        close()
                    }

                    drawPath(
                        path = diamondPath,
                        color = if (isSelected) Color.White else Color(0xFFFF6B6B)
                    )

                    // Draw year label if available
                    evt.year?.let { yearVal ->
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.argb(220, 255, 107, 107)
                                textSize = 20f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }
                            drawText(
                                yearVal.toString(),
                                eventCenter.x,
                                eventCenter.y + diamondSize + 16f,
                                paint
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawCardinalDirections(center: Offset, radarRadius: Float) {
    val labels = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val angles = listOf(0, 45, 90, 135, 180, 225, 270, 315)

    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            textSize = 30f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        for (i in labels.indices) {
            val label = labels[i]
            val angle = angles[i]
            val isPrimary = label == "N" || label == "E" || label == "S" || label == "W"
            paint.color = if (label == "N") {
                android.graphics.Color.rgb(255, 213, 107)
            } else if (isPrimary) {
                android.graphics.Color.argb(220, 212, 163, 89)
            } else {
                android.graphics.Color.argb(120, 212, 163, 89)
            }
            paint.isFakeBoldText = isPrimary

            val rad = Math.toRadians((angle - 90.0)).toFloat()
            val dist = radarRadius + 20.dp.toPx()
            val x = center.x + dist * cos(rad)
            val y = center.y + dist * sin(rad) + 10f
            drawText(label, x, y, paint)
        }
    }
}

private fun getCategoryColor(category: PlaceCategory): Color {
    return when (category) {
        PlaceCategory.MONUMENT -> Color(0xFFFFD56B)
        PlaceCategory.EVENT -> Color(0xFFFF6B6B)
        PlaceCategory.SACRED -> Color(0xFF5CE1E6)
        PlaceCategory.ARCHITECTURE -> Color(0xFFFFA94D)
        PlaceCategory.FIGURE -> Color(0xFFB197FC)
        PlaceCategory.GENERAL -> Color(0xFFD4C8B8)
    }
}
