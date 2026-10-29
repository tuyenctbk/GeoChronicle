package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.audio.AudioNarrationState
import com.example.data.model.HistoricalPlace
import com.example.location.LocationHelper
import com.example.ui.radar.getCategoryColor

@Composable
fun HistoricalCardItem(
    place: HistoricalPlace,
    narrationState: AudioNarrationState,
    onClick: () -> Unit,
    onToggleNarration: (HistoricalPlace) -> Unit,
    onToggleBookmark: (HistoricalPlace) -> Unit,
    onToggleVisited: (HistoricalPlace) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isSpeakingThis = narrationState is AudioNarrationState.Speaking && narrationState.placeId == place.pageId
    val isPaused = narrationState is AudioNarrationState.Speaking && narrationState.isPaused
    val categoryColor = getCategoryColor(place.category)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSpeakingThis) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("historical_card_${place.pageId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Thumbnail or Category Icon Box
                if (!place.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(place.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = place.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(categoryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = place.category.emoji,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title, Category, Distance
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    "${place.category.emoji} ${stringResource(place.category.stringRes)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = categoryColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = categoryColor.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.height(24.dp)
                        )

                        Text(
                            text = "${LocationHelper.formatDistance(place.distanceMeters)} • ${LocationHelper.formatBearingDirection(place.bearingDegrees)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = place.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (place.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = place.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Play audio narration, Visited status, Bookmark status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Audio narration button
                FilledIconButton(
                    onClick = { onToggleNarration(place) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isSpeakingThis) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("card_play_audio_${place.pageId}")
                ) {
                    Icon(
                        imageVector = if (isSpeakingThis && !isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.listen_to_story),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Visited Toggle
                    IconButton(
                        onClick = { onToggleVisited(place) },
                        modifier = Modifier.testTag("card_toggle_visited_${place.pageId}")
                    ) {
                        Icon(
                            imageVector = if (place.isVisited) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = stringResource(if (place.isVisited) R.string.action_visited else R.string.action_unvisited),
                            tint = if (place.isVisited) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Bookmark Toggle
                    IconButton(
                        onClick = { onToggleBookmark(place) },
                        modifier = Modifier.testTag("card_toggle_bookmark_${place.pageId}")
                    ) {
                        Icon(
                            imageVector = if (place.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = stringResource(if (place.isBookmarked) R.string.action_saved else R.string.action_save),
                            tint = if (place.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
