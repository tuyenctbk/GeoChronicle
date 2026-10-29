package com.example.ui.radar

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.HistoricalPlace
import com.example.data.model.PlaceCategory
import com.example.location.LocationHelper
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

@Composable
fun HistoryRadarView(
    places: List<HistoricalPlace>,
    selectedPlace: HistoricalPlace?,
    radiusMeters: Int,
    onPlaceSelected: (HistoricalPlace) -> Unit,
    showSuggestedRoute: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweepTransition")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseScale"
    )

    val routeDashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RouteDashPhase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
            .testTag("radar_view_canvas_container")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(places, radiusMeters) {
                    detectTapGestures { tapOffset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radarRadius = min(size.width, size.height) / 2f * 0.90f

                        var closestPlace: HistoricalPlace? = null
                        var minDistanceToTap = Float.MAX_VALUE

                        for (place in places) {
                            val normalizedDist = (place.distanceMeters / radiusMeters).toFloat().coerceIn(0f, 1f)
                            val r = normalizedDist * radarRadius
                            val rad = Math.toRadians((place.bearingDegrees - 90.0)).toFloat()
                            val blipX = center.x + r * cos(rad)
                            val blipY = center.y + r * sin(rad)

                            val distToTap = hypot(tapOffset.x - blipX, tapOffset.y - blipY)
                            val threshold = 36.dp.toPx()
                            if (distToTap < threshold && distToTap < minDistanceToTap) {
                                minDistanceToTap = distToTap
                                closestPlace = place
                            }
                        }

                        if (closestPlace != null) {
                            onPlaceSelected(closestPlace)
                        }
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radarRadius = min(size.width, size.height) / 2f * 0.90f

            // 1. Radar background gradient
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

            // 2. Concentric range rings
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

                // Distance label
                val labelDistance = (radiusMeters * frac).toInt()
                val labelText = if (labelDistance >= 1000) "${labelDistance / 1000}km" else "${labelDistance}m"
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(160, 212, 163, 89)
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.LEFT
                        isAntiAlias = true
                    }
                    drawText(labelText, center.x + 8f, center.y - r + 24f, paint)
                }
            }

            // 3. Crosshairs and Cardinal Lines
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

            // 4. Cardinal Direction Labels (N, E, S, W)
            drawCardinalLabels(center, radarRadius)

            // 5. Suggested Walking Tour Route Path Visualizer
            if (showSuggestedRoute && places.isNotEmpty()) {
                val routePoints = mutableListOf<Offset>()
                routePoints.add(center) // Start at user location

                // Order top 6 places by proximity to form a walking tour path
                val tourPlaces = places.take(6).sortedBy { it.distanceMeters }
                tourPlaces.forEach { place ->
                    val normalizedDist = (place.distanceMeters / radiusMeters).toFloat().coerceIn(0f, 1f)
                    val r = normalizedDist * radarRadius
                    val rad = Math.toRadians((place.bearingDegrees - 90.0)).toFloat()
                    routePoints.add(Offset(center.x + r * cos(rad), center.y + r * sin(rad)))
                }

                // Draw path line segments
                for (i in 0 until routePoints.size - 1) {
                    val p1 = routePoints[i]
                    val p2 = routePoints[i + 1]
                    val placeObj = tourPlaces.getOrNull(i)
                    val isSegmentVisited = placeObj?.isVisited == true
                    val segmentColor = if (isSegmentVisited) Color(0xFF4CAF50) else Color(0xFF5CE1E6)
                    val glowColor = if (isSegmentVisited) Color(0x444CAF50) else Color(0x445CE1E6)

                    // Glowing outer path line
                    drawLine(
                        color = glowColor,
                        start = p1,
                        end = p2,
                        strokeWidth = 5.dp.toPx()
                    )

                    // Dashed animated path line
                    drawLine(
                        color = segmentColor,
                        start = p1,
                        end = p2,
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(14f, 10f),
                            phase = routeDashPhase
                        )
                    )

                    // Segment distance badge at midpoint
                    val midX = (p1.x + p2.x) / 2f
                    val midY = (p1.y + p2.y) / 2f
                    if (placeObj != null && i > 0) {
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = if (isSegmentVisited) android.graphics.Color.argb(220, 76, 175, 80) else android.graphics.Color.argb(200, 92, 225, 230)
                                textSize = 22f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }
                            val segDist = LocationHelper.formatDistance(placeObj.distanceMeters)
                            drawText(segDist, midX, midY - 6f, paint)
                        }
                    }
                }
            }

            // 6. Radar Sweep Beam
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

            // Faded sweep trailing arc
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
                size = androidx.compose.ui.geometry.Size(radarRadius * 2, radarRadius * 2)
            )

            // 7. Draw User Center Marker (Walker)
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

            // 8. Draw Historical Place Blips & Route Badges
            val tourPlacesMap = places.take(6).sortedBy { it.distanceMeters }.mapIndexed { idx, p -> p.pageId to (idx + 1) }.toMap()

            places.forEach { place ->
                val normalizedDist = (place.distanceMeters / radiusMeters).toFloat().coerceIn(0f, 1f)
                val r = normalizedDist * radarRadius
                val rad = Math.toRadians((place.bearingDegrees - 90.0)).toFloat()
                val blipCenter = Offset(
                    center.x + r * cos(rad),
                    center.y + r * sin(rad)
                )

                val isSelected = selectedPlace?.pageId == place.pageId
                val blipColor = getCategoryColor(place.category)
                val routeOrder = tourPlacesMap[place.pageId]

                // Selection glow / target reticle
                if (isSelected) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.35f),
                        radius = 22.dp.toPx() * pulseScale,
                        center = blipCenter
                    )
                    drawCircle(
                        color = Color(0xFFFFD56B),
                        radius = 14.dp.toPx(),
                        center = blipCenter,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Outer halo
                drawCircle(
                    color = blipColor.copy(alpha = if (isSelected) 0.5f else 0.25f),
                    radius = if (isSelected) 10.dp.toPx() else 7.dp.toPx(),
                    center = blipCenter
                )

                // Inner core dot
                drawCircle(
                    color = if (isSelected) Color.White else blipColor,
                    radius = if (isSelected) 5.dp.toPx() else 4.dp.toPx(),
                    center = blipCenter
                )

                // Route Step Number Badge
                if (showSuggestedRoute && routeOrder != null) {
                    drawCircle(
                        color = Color(0xFF5CE1E6),
                        radius = 8.dp.toPx(),
                        center = Offset(blipCenter.x - 10.dp.toPx(), blipCenter.y - 10.dp.toPx())
                    )
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 20f
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        drawText(
                            routeOrder.toString(),
                            blipCenter.x - 10.dp.toPx(),
                            blipCenter.y - 4.dp.toPx(),
                            paint
                        )
                    }
                }

                // Visited indicator check dot
                if (place.isVisited) {
                    drawCircle(
                        color = Color(0xFF4CAF50),
                        radius = 3.dp.toPx(),
                        center = Offset(blipCenter.x + 6.dp.toPx(), blipCenter.y - 6.dp.toPx())
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawCardinalLabels(center: Offset, radarRadius: Float) {
    val labels = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val angles = listOf(0, 45, 90, 135, 180, 225, 270, 315)

    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            textSize = 32f
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
            val dist = radarRadius + 22.dp.toPx()
            val x = center.x + dist * cos(rad)
            val y = center.y + dist * sin(rad) + 10f
            drawText(label, x, y, paint)
        }
    }
}

fun getCategoryColor(category: PlaceCategory): Color {
    return when (category) {
        PlaceCategory.MONUMENT -> Color(0xFFFFD56B) // Gold
        PlaceCategory.EVENT -> Color(0xFFFF6B6B) // Historic event red
        PlaceCategory.SACRED -> Color(0xFF5CE1E6) // Cyan jade
        PlaceCategory.ARCHITECTURE -> Color(0xFFFFA94D) // Amber fortress
        PlaceCategory.FIGURE -> Color(0xFFB197FC) // Royal purple
        PlaceCategory.GENERAL -> Color(0xFFD4C8B8) // Parchment
    }
}
