package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.audio.AudioNarrationState
import com.example.data.model.HistoricalPlace
import com.example.location.LocationHelper
import com.example.ui.radar.getCategoryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandmarkDetailBottomSheet(
    place: HistoricalPlace?,
    narrationState: AudioNarrationState,
    onDismiss: () -> Unit,
    onToggleNarration: (HistoricalPlace) -> Unit,
    onCycleSpeechRate: () -> Float,
    onToggleBookmark: (HistoricalPlace) -> Unit,
    onToggleVisited: (HistoricalPlace) -> Unit,
    onSaveNote: (Long, String) -> Unit,
    onOpenDonationDialog: (() -> Unit)? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    if (place == null) return

    val context = LocalContext.current
    var isEditingNote by remember { mutableStateOf(false) }
    var currentNoteText by remember(place.userNote) { mutableStateOf(place.userNote ?: "") }

    val isSpeakingThis = narrationState is AudioNarrationState.Speaking && narrationState.placeId == place.pageId
    val isPaused = narrationState is AudioNarrationState.Speaking && narrationState.isPaused
    val currentRate = if (narrationState is AudioNarrationState.Speaking) narrationState.speechRate else 1.0f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("landmark_detail_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Image
            if (!place.thumbnailUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(place.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = place.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close), tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Category & Distance Chips
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val catColor = getCategoryColor(place.category)
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            "${place.category.emoji} ${stringResource(place.category.stringRes)}",
                            color = catColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    modifier = Modifier.testTag("category_chip")
                )

                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            "🧭 ${LocationHelper.formatDistance(place.distanceMeters)} • ${LocationHelper.formatBearingDirection(place.bearingDegrees)}",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("distance_chip")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = place.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("landmark_title")
            )

            if (place.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = place.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio Tour Narration Control Box
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSpeakingThis) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("audio_narration_player_box")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSpeakingThis && !isPaused) stringResource(R.string.narrating_audio_guide) else stringResource(R.string.historical_audio_narration),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Speech rate toggle button
                        FilledTonalButton(
                            onClick = { onCycleSpeechRate() },
                            modifier = Modifier.testTag("speech_rate_button")
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.speed_format, currentRate.toString()))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onToggleNarration(place) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("toggle_narration_button")
                        ) {
                            Icon(
                                imageVector = if (isSpeakingThis && !isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isSpeakingThis && !isPaused) stringResource(R.string.pause_narration) else stringResource(R.string.listen_to_story)
                            )
                        }

                        if (isSpeakingThis) {
                            OutlinedButton(
                                onClick = { onToggleNarration(place) },
                                modifier = Modifier.testTag("stop_narration_button")
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = stringResource(R.string.stop_narration))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Historical Chronicle Text
            Text(
                text = stringResource(R.string.historical_chronicle_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = place.extract,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("landmark_extract_text")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Actions: Save Offline / Visited
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { onToggleBookmark(place) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bookmark_place_button")
                ) {
                    Icon(
                        imageVector = if (place.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (place.isBookmarked) stringResource(R.string.action_saved) else stringResource(R.string.action_save))
                }

                FilledTonalButton(
                    onClick = { onToggleVisited(place) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mark_visited_button")
                ) {
                    Icon(
                        imageVector = if (place.isVisited) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (place.isVisited) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (place.isVisited) stringResource(R.string.action_visited) else stringResource(R.string.action_unvisited))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Directions
                OutlinedButton(
                    onClick = {
                        val gmmIntentUri = Uri.parse("geo:${place.lat},${place.lon}?q=${Uri.encode(place.title)}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        try {
                            context.startActivity(mapIntent)
                        } catch (_: Exception) {
                            val browserMap = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.google.com/maps/search/?api=1&query=${place.lat},${place.lon}")
                            )
                            context.startActivity(browserMap)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("navigate_map_button")
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_directions))
                }

                // Wikipedia External
                OutlinedButton(
                    onClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(place.wikiUrl))
                        context.startActivity(browserIntent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_wikipedia_button")
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_wikipedia))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Walker Notes Section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.walkers_field_notes),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = { isEditingNote = !isEditingNote }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_note))
                        }
                    }

                    if (isEditingNote) {
                        OutlinedTextField(
                            value = currentNoteText,
                            onValueChange = { currentNoteText = it },
                            placeholder = { Text(stringResource(R.string.field_notes_placeholder)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp)
                                .testTag("note_input_field")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onSaveNote(place.pageId, currentNoteText)
                                isEditingNote = false
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("save_note_button")
                        ) {
                            Text(stringResource(R.string.save_note))
                        }
                    } else {
                        if (place.userNote.isNullOrBlank()) {
                            Text(
                                text = stringResource(R.string.field_notes_empty),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = place.userNote,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Donation Value Realization Card
            if (onOpenDonationDialog != null) {
                Spacer(modifier = Modifier.height(12.dp))
                DonationValueRealizationCard(
                    onOpenDonationDialog = onOpenDonationDialog
                )
            }
        }
    }
}
