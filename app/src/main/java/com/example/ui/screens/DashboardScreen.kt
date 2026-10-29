package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.audio.AudioNarrationState
import com.example.data.model.HistoricalPlace
import com.example.data.model.PlaceCategory
import com.example.ui.components.HistoricalCardItem
import com.example.ui.radar.getCategoryColor

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.testTag
import com.example.ui.components.ShareProgressDialog
import com.example.ui.share.TourSummaryData

import com.example.ui.components.DonationValueRealizationCard

@Composable
fun DashboardTabContent(
    bookmarkedPlaces: List<HistoricalPlace>,
    visitedPlaces: List<HistoricalPlace>,
    allPlaces: List<HistoricalPlace>,
    onCardClick: (HistoricalPlace) -> Unit,
    onOpenDonationDialog: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showShareDialog by remember { mutableStateOf(false) }

    val totalVisited = visitedPlaces.size
    val totalDistanceMeters = visitedPlaces.sumOf { it.distanceMeters } + (visitedPlaces.size * 350.0)
    val totalDistanceKm = totalDistanceMeters / 1000.0

    val totalMinutes = visitedPlaces.size * 18
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60

    if (showShareDialog) {
        ShareProgressDialog(
            data = TourSummaryData(
                tourTitle = stringResource(R.string.dashboard_title),
                totalDistanceKm = totalDistanceKm,
                visitedCount = totalVisited,
                totalStops = (allPlaces.size).coerceAtLeast(totalVisited).coerceAtLeast(1),
                timeSpentMinutes = totalMinutes,
                visitedLandmarks = visitedPlaces
            ),
            onDismiss = { showShareDialog = false },
            onOpenDonationDialog = onOpenDonationDialog
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = { showShareDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("dashboard_share_progress_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.share_progress),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Summary Stat Cards Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = stringResource(R.string.total_distance_covered),
                    value = stringResource(R.string.distance_format_km, totalDistanceKm),
                    icon = "🗺️",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = stringResource(R.string.landmarks_visited_count),
                    value = "$totalVisited",
                    icon = "🏛️",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = stringResource(R.string.exploration_time),
                    value = if (hours > 0) stringResource(R.string.time_format_hours_mins, hours, mins) else stringResource(R.string.time_format_mins, mins),
                    icon = "⏱️",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Category Breakdown Chart Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.category_breakdown),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val categoryCounts = PlaceCategory.entries.associateWith { cat ->
                        visitedPlaces.count { it.category == cat }
                    }
                    val maxCount = categoryCounts.values.maxOrNull()?.coerceAtLeast(1) ?: 1

                    if (visitedPlaces.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_activity_dashboard),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PlaceCategory.entries.forEach { cat ->
                                val count = categoryCounts[cat] ?: 0
                                val color = getCategoryColor(cat)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${cat.emoji} ${stringResource(cat.stringRes)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.width(130.dp),
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(12.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    ) {
                                        val fraction = (count.toFloat() / maxCount).coerceIn(0f, 1f)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .height(12.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(color)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (onOpenDonationDialog != null) {
            item {
                DonationValueRealizationCard(
                    onOpenDonationDialog = onOpenDonationDialog
                )
            }
        }

        // Recent Activity Header
        item {
            Text(
                text = stringResource(R.string.recent_activity),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (visitedPlaces.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🧭", style = MaterialTheme.typography.displaySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_visited_places_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.no_activity_dashboard),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(visitedPlaces.take(10), key = { it.pageId }) { place ->
                HistoricalCardItem(
                    place = place,
                    narrationState = AudioNarrationState.Idle,
                    onClick = { onCardClick(place) },
                    onToggleNarration = {},
                    onToggleBookmark = {},
                    onToggleVisited = {}
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(icon, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}
