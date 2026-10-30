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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.History
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
import com.example.data.db.EventEntity
import com.example.data.model.HistoricalPlace
import com.example.location.LocationHelper
import com.example.ui.radar.getCategoryColor

/**
 * Rich Modal Bottom Sheet component that displays detailed historical information, facts,
 * images, and narration controls when a landmark or event is tapped on the RadarView.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarInfoBottomSheet(
    selectedLandmark: HistoricalPlace? = null,
    selectedEvent: EventEntity? = null,
    parentLandmarkForEvent: HistoricalPlace? = null,
    narrationState: AudioNarrationState = AudioNarrationState.Idle,
    onDismiss: () -> Unit,
    onToggleNarration: (HistoricalPlace) -> Unit = {},
    onCycleSpeechRate: () -> Float = { 1.0f },
    onToggleBookmark: (HistoricalPlace) -> Unit = {},
    onToggleVisited: (HistoricalPlace) -> Unit = {},
    onSaveNote: (Long, String) -> Unit = { _, _ -> },
    onOpenDonationDialog: (() -> Unit)? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    if (selectedLandmark == null && selectedEvent == null) return

    val context = LocalContext.current
    var isEditingNote by remember { mutableStateOf(false) }

    val placeToDisplay = selectedLandmark ?: parentLandmarkForEvent

    var currentNoteText by remember(placeToDisplay?.userNote) {
        mutableStateOf(placeToDisplay?.userNote ?: "")
    }

    val isSpeakingThis = placeToDisplay != null &&
            narrationState is AudioNarrationState.Speaking &&
            narrationState.placeId == placeToDisplay.pageId

    val isPaused = narrationState is AudioNarrationState.Speaking && narrationState.isPaused
    val currentRate = if (narrationState is AudioNarrationState.Speaking) narrationState.speechRate else 1.0f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("radar_info_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Header Image Banner (if available)
            val imageUrl = selectedLandmark?.thumbnailUrl ?: parentLandmarkForEvent?.thumbnailUrl
            if (!imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = selectedLandmark?.title ?: selectedEvent?.title,
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
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2. Event or Landmark Type Badge & Distance
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (selectedEvent != null) {
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                "📜 ${selectedEvent.era ?: "Historic Event"}",
                                color = Color(0xFFFF6B6B),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.testTag("event_era_chip")
                    )
                    if (selectedEvent.year != null) {
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    "📅 ${selectedEvent.year}",
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            modifier = Modifier.testTag("event_year_chip")
                        )
                    }
                } else if (selectedLandmark != null) {
                    val catColor = getCategoryColor(selectedLandmark.category)
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                "${selectedLandmark.category.emoji} ${stringResource(selectedLandmark.category.stringRes)}",
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
                                "🧭 ${LocationHelper.formatDistance(selectedLandmark.distanceMeters)} • ${LocationHelper.formatBearingDirection(selectedLandmark.bearingDegrees)}",
                                fontWeight = FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("distance_chip")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Title
            val titleText = selectedEvent?.title ?: selectedLandmark?.title.orEmpty()
            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("radar_info_title")
            )

            // Subtitle / Date
            val subtitleText = selectedEvent?.date ?: selectedLandmark?.description.orEmpty()
            if (subtitleText.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Audio Narration Player (if landmark present)
            if (placeToDisplay != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSpeakingThis) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("radar_info_audio_player")
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
                                onClick = { onToggleNarration(placeToDisplay) },
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
                                    onClick = { onToggleNarration(placeToDisplay) },
                                    modifier = Modifier.testTag("stop_narration_button")
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = stringResource(R.string.stop_narration))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 5. Historical Chronicle & Facts Section
            Text(
                text = stringResource(R.string.historical_chronicle_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))

            val extractText = selectedEvent?.description ?: selectedLandmark?.extract.orEmpty()
            Text(
                text = extractText,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("radar_info_extract_text")
            )

            // Significance Card (if event)
            if (selectedEvent?.significance != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Historical Significance",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = selectedEvent.significance,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. Action Buttons: Directions & External Web Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (placeToDisplay != null) {
                    OutlinedButton(
                        onClick = {
                            val gmmIntentUri = Uri.parse("geo:${placeToDisplay.lat},${placeToDisplay.lon}?q=${Uri.encode(titleText)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            try {
                                context.startActivity(mapIntent)
                            } catch (_: Exception) {
                                val browserMap = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.google.com/maps/search/?api=1&query=${placeToDisplay.lat},${placeToDisplay.lon}")
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
                }

                val webUrl = selectedEvent?.sourceUrl ?: selectedLandmark?.wikiUrl ?: placeToDisplay?.wikiUrl
                if (!webUrl.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
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
            }

            // 7. Save / Visited Toggles (if Landmark present)
            if (selectedLandmark != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onToggleBookmark(selectedLandmark) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bookmark_place_button")
                    ) {
                        Icon(
                            imageVector = if (selectedLandmark.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (selectedLandmark.isBookmarked) stringResource(R.string.action_saved) else stringResource(R.string.action_save))
                    }

                    FilledTonalButton(
                        onClick = { onToggleVisited(selectedLandmark) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mark_visited_button")
                    ) {
                        Icon(
                            imageVector = if (selectedLandmark.isVisited) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (selectedLandmark.isVisited) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (selectedLandmark.isVisited) stringResource(R.string.action_visited) else stringResource(R.string.action_unvisited))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 8. Walker Notes Section
            if (placeToDisplay != null) {
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
                                    onSaveNote(placeToDisplay.pageId, currentNoteText)
                                    isEditingNote = false
                                },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .testTag("save_note_button")
                            ) {
                                Text(stringResource(R.string.save_note))
                            }
                        } else {
                            if (placeToDisplay.userNote.isNullOrBlank()) {
                                Text(
                                    text = stringResource(R.string.field_notes_empty),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = placeToDisplay.userNote,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 9. Strategic Donation Card
            if (onOpenDonationDialog != null) {
                Spacer(modifier = Modifier.height(12.dp))
                DonationValueRealizationCard(
                    onOpenDonationDialog = onOpenDonationDialog
                )
            }
        }
    }
}
