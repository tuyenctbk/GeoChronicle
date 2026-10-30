package com.example.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.example.audio.AudioNarrationState
import com.example.data.model.HistoricalPlace

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

    RadarInfoBottomSheet(
        selectedLandmark = place,
        narrationState = narrationState,
        onDismiss = onDismiss,
        onToggleNarration = onToggleNarration,
        onCycleSpeechRate = onCycleSpeechRate,
        onToggleBookmark = onToggleBookmark,
        onToggleVisited = onToggleVisited,
        onSaveNote = onSaveNote,
        onOpenDonationDialog = onOpenDonationDialog,
        sheetState = sheetState
    )
}
