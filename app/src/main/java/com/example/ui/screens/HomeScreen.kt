package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.audio.AudioNarrationState
import com.example.data.model.HistoricalPlace
import com.example.data.model.PlaceCategory
import com.example.location.LocationHelper
import com.example.ui.components.AudioPlayerBar
import com.example.ui.components.HistoricalCardItem
import com.example.ui.components.LandmarkDetailBottomSheet
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.components.LocationPresetDialog
import com.example.ui.components.ShareProgressDialog
import com.example.ui.radar.HistoryRadarView
import com.example.ui.radar.getCategoryColor
import com.example.ui.share.TourSummaryData
import com.example.ui.viewmodel.GeoChronicleViewModel
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.PlacesUiState
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import com.example.ui.components.DonationDialog
import com.example.ui.components.DonationHomeBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: GeoChronicleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val radiusMeters by viewModel.radiusMeters.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val placesUiState by viewModel.placesUiState.collectAsStateWithLifecycle()
    val selectedPlaceForDetail by viewModel.selectedPlaceForDetail.collectAsStateWithLifecycle()
    val currentNavTab by viewModel.currentNavTab.collectAsStateWithLifecycle()
    val narrationState by viewModel.narrationState.collectAsStateWithLifecycle()
    val bookmarkedPlaces by viewModel.bookmarkedPlaces.collectAsStateWithLifecycle()
    val visitedPlaces by viewModel.visitedPlaces.collectAsStateWithLifecycle()
    val isAutoNarrateEnabled by viewModel.isAutoNarrateEnabled.collectAsStateWithLifecycle()
    val tourLanguage by viewModel.tourLanguage.collectAsStateWithLifecycle()

    val isSupporter by viewModel.isSupporter.collectAsStateWithLifecycle()
    val supporterTier by viewModel.supporterTier.collectAsStateWithLifecycle()
    val showHomeBanner by viewModel.showHomeBanner.collectAsStateWithLifecycle()
    val showDonationDialog by viewModel.showDonationDialog.collectAsStateWithLifecycle()

    var showLocationDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var selectedRadarPlace by remember { mutableStateOf<HistoricalPlace?>(null) }

    // Location Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.useDeviceGps(context)
        }
    }

    val requestLocationPermission = {
        val fineCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fineCheck == PackageManager.PERMISSION_GRANTED || coarseCheck == PackageManager.PERMISSION_GRANTED) {
            viewModel.useDeviceGps(context)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Filter places
    val allPlaces = when (val state = placesUiState) {
        is PlacesUiState.Success -> state.places
        is PlacesUiState.Error -> state.cachedPlaces
        else -> emptyList()
    }

    val filteredPlaces = remember(allPlaces, selectedCategory, searchQuery) {
        allPlaces.filter { place ->
            val matchesCategory = selectedCategory == null || place.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    place.title.contains(searchQuery, ignoreCase = true) ||
                    place.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val activeRadarPlace = selectedRadarPlace ?: filteredPlaces.firstOrNull()
    val locationDisplayName = if (currentLocation.nameRes != null) {
        stringResource(currentLocation.nameRes!!)
    } else {
        currentLocation.name
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showLocationDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("top_location_selector")
                    ) {
                        Text(
                            text = currentLocation.iconEmoji,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = locationDisplayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.NearMe,
                                    contentDescription = stringResource(R.string.change_location),
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = stringResource(
                                    R.string.radius_and_stops_format,
                                    LocationHelper.formatDistance(radiusMeters.toDouble()),
                                    filteredPlaces.size
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    // Language Selector Button
                    IconButton(
                        onClick = { showLanguageDialog = true },
                        modifier = Modifier.testTag("language_selector_button")
                    ) {
                        Text(tourLanguage.flagEmoji, style = MaterialTheme.typography.titleMedium)
                    }

                    // GPS Button
                    IconButton(
                        onClick = { requestLocationPermission() },
                        modifier = Modifier.testTag("gps_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = stringResource(R.string.my_gps_location),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Refresh Button
                    IconButton(
                        onClick = { viewModel.loadPlaces() },
                        modifier = Modifier.testTag("refresh_places_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh_history_walk)
                        )
                    }

                    // Support Dev Button
                    IconButton(
                        onClick = { viewModel.openDonationDialog() },
                        modifier = Modifier.testTag("support_dev_header_button")
                    ) {
                        if (isSupporter) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = stringResource(R.string.donation_dialog_title),
                                    tint = Color(0xFFE11D48)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD97706))
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = stringResource(R.string.donation_dialog_title),
                                tint = Color(0xFFE11D48)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                val speakingPlace = allPlaces.find {
                    narrationState is AudioNarrationState.Speaking &&
                            (narrationState as AudioNarrationState.Speaking).placeId == it.pageId
                }
                AudioPlayerBar(
                    narrationState = narrationState,
                    currentSpeakingPlace = speakingPlace,
                    onTogglePlayPause = { viewModel.togglePlayPauseCurrent() },
                    onStop = { viewModel.stopNarration() },
                    onCycleSpeed = { viewModel.cycleSpeechRate() },
                    onClickBar = {
                        speakingPlace?.let { viewModel.selectPlaceForDetail(it) }
                    }
                )

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = currentNavTab == NavigationTab.RADAR,
                        onClick = { viewModel.setNavTab(NavigationTab.RADAR) },
                        icon = { Icon(Icons.Default.Radar, contentDescription = stringResource(R.string.nav_radar)) },
                        label = { Text(stringResource(R.string.nav_radar)) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_radar")
                    )
                    NavigationBarItem(
                        selected = currentNavTab == NavigationTab.LIST,
                        onClick = { viewModel.setNavTab(NavigationTab.LIST) },
                        icon = { Icon(Icons.Default.Explore, contentDescription = stringResource(R.string.nav_discover)) },
                        label = { Text(stringResource(R.string.nav_discover)) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_discover")
                    )
                    NavigationBarItem(
                        selected = currentNavTab == NavigationTab.WALK_TOUR,
                        onClick = { viewModel.setNavTab(NavigationTab.WALK_TOUR) },
                        icon = { Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = stringResource(R.string.nav_walk_tour)) },
                        label = { Text(stringResource(R.string.nav_walk_tour)) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_walk_tour")
                    )
                    NavigationBarItem(
                        selected = currentNavTab == NavigationTab.JOURNAL,
                        onClick = { viewModel.setNavTab(NavigationTab.JOURNAL) },
                        icon = { Icon(Icons.Default.Book, contentDescription = stringResource(R.string.nav_journal)) },
                        label = { Text(stringResource(R.string.nav_journal)) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_journal")
                    )
                    NavigationBarItem(
                        selected = currentNavTab == NavigationTab.DASHBOARD,
                        onClick = { viewModel.setNavTab(NavigationTab.DASHBOARD) },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = stringResource(R.string.nav_dashboard)) },
                        label = { Text(stringResource(R.string.nav_dashboard)) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_dashboard")
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (showHomeBanner) {
                    DonationHomeBanner(
                        onOpenDonationDialog = { viewModel.openDonationDialog() },
                        onDismissBanner = { viewModel.dismissHomeBanner() }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentNavTab) {
                        NavigationTab.RADAR -> {
                            RadarTabContent(
                                places = filteredPlaces,
                                selectedPlace = activeRadarPlace,
                                radiusMeters = radiusMeters,
                                selectedCategory = selectedCategory,
                                uiState = placesUiState,
                                narrationState = narrationState,
                                onSelectPlace = { place ->
                                    selectedRadarPlace = place
                                    viewModel.selectPlaceForDetail(place)
                                },
                                onOpenDetail = { viewModel.selectPlaceForDetail(it) },
                                onToggleNarration = { viewModel.toggleNarration(it) },
                                onSelectCategory = { viewModel.selectCategory(it) },
                                onSelectRadius = { viewModel.setRadius(it) },
                                onRetry = { viewModel.loadPlaces() }
                            )
                        }
                        NavigationTab.LIST -> {
                            DiscoverListTabContent(
                                places = filteredPlaces,
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                uiState = placesUiState,
                                narrationState = narrationState,
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onSelectCategory = { viewModel.selectCategory(it) },
                                onCardClick = { viewModel.selectPlaceForDetail(it) },
                                onToggleNarration = { viewModel.toggleNarration(it) },
                                onToggleBookmark = { viewModel.toggleBookmark(it) },
                                onToggleVisited = { viewModel.toggleVisited(it) },
                                onRetry = { viewModel.loadPlaces() }
                            )
                        }
                        NavigationTab.WALK_TOUR -> {
                            WalkTourTabContent(
                                places = filteredPlaces,
                                locationName = locationDisplayName,
                                narrationState = narrationState,
                                isAutoNarrateEnabled = isAutoNarrateEnabled,
                                onToggleAutoNarrate = { viewModel.toggleAutoNarrate() },
                                onCardClick = { viewModel.selectPlaceForDetail(it) },
                                onToggleNarration = { viewModel.toggleNarration(it) },
                                onToggleBookmark = { viewModel.toggleBookmark(it) },
                                onToggleVisited = { viewModel.toggleVisited(it) }
                            )
                        }
                        NavigationTab.JOURNAL -> {
                            JournalTabContent(
                                bookmarkedPlaces = bookmarkedPlaces,
                                visitedPlaces = visitedPlaces,
                                narrationState = narrationState,
                                onCardClick = { viewModel.selectPlaceForDetail(it) },
                                onToggleNarration = { viewModel.toggleNarration(it) },
                                onToggleBookmark = { viewModel.toggleBookmark(it) },
                                onToggleVisited = { viewModel.toggleVisited(it) }
                            )
                        }
                        NavigationTab.DASHBOARD -> {
                            DashboardTabContent(
                                bookmarkedPlaces = bookmarkedPlaces,
                                visitedPlaces = visitedPlaces,
                                allPlaces = filteredPlaces,
                                onCardClick = { viewModel.selectPlaceForDetail(it) },
                                onOpenDonationDialog = { viewModel.openDonationDialog() }
                            )
                        }
                    }
                }
            }

            if (placesUiState is PlacesUiState.Loading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .testTag("loading_progress_bar")
                )
            }
        }
    }

    // Landmark Detail Bottom Sheet
    if (selectedPlaceForDetail != null) {
        LandmarkDetailBottomSheet(
            place = selectedPlaceForDetail,
            narrationState = narrationState,
            onDismiss = { viewModel.selectPlaceForDetail(null) },
            onToggleNarration = { viewModel.toggleNarration(it) },
            onCycleSpeechRate = { viewModel.cycleSpeechRate() },
            onToggleBookmark = { viewModel.toggleBookmark(it) },
            onToggleVisited = { viewModel.toggleVisited(it) },
            onSaveNote = { id, note -> viewModel.saveNote(id, note) },
            onOpenDonationDialog = { viewModel.openDonationDialog() }
        )
    }

    // Location Presets Dialog
    if (showLocationDialog) {
        LocationPresetDialog(
            currentLocationName = locationDisplayName,
            onDismiss = { showLocationDialog = false },
            onSelectPreset = { preset -> viewModel.setLocationPreset(preset) },
            onUseCurrentGps = { requestLocationPermission() },
            onCustomCoordinates = { lat, lon, name -> viewModel.setCustomLocation(lat, lon, name) }
        )
    }

    // Language Selector Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = tourLanguage,
            onDismiss = { showLanguageDialog = false },
            onSelectLanguage = { lang -> viewModel.setTourLanguage(lang) }
        )
    }

    // Donation Multi-Method Dialog
    if (showDonationDialog) {
        DonationDialog(
            isSupporter = isSupporter,
            supporterTier = supporterTier,
            onDismissRequest = { viewModel.closeDonationDialog() },
            onConfirmDonation = { tier -> viewModel.confirmDonation(tier) }
        )
    }
}

@Composable
private fun RadarTabContent(
    places: List<HistoricalPlace>,
    selectedPlace: HistoricalPlace?,
    radiusMeters: Int,
    selectedCategory: PlaceCategory?,
    uiState: PlacesUiState,
    narrationState: AudioNarrationState,
    onSelectPlace: (HistoricalPlace) -> Unit,
    onOpenDetail: (HistoricalPlace) -> Unit,
    onToggleNarration: (HistoricalPlace) -> Unit,
    onSelectCategory: (PlaceCategory?) -> Unit,
    onSelectRadius: (Int) -> Unit,
    onRetry: () -> Unit
) {
    var showSuggestedRoute by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("radar_tab_content")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.radar_scope_label),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val radiusOptions = listOf(500, 1000, 2000, 5000)
                    radiusOptions.forEach { r ->
                        val isSelected = r == radiusMeters
                        val label = if (r >= 1000) "${r / 1000}km" else "${r}m"
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectRadius(r) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Category filter chips + Route Trail toggle chip
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = showSuggestedRoute,
                        onClick = { showSuggestedRoute = !showSuggestedRoute },
                        label = { Text("🗺️ ${stringResource(R.string.route_trail)}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("toggle_route_trail_chip")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onSelectCategory(null) },
                        label = { Text(stringResource(R.string.all_stops)) },
                        modifier = Modifier.height(30.dp)
                    )
                }
                items(PlaceCategory.entries.toTypedArray()) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { onSelectCategory(cat) },
                        label = { Text("${cat.emoji} ${stringResource(cat.stringRes)}") },
                        modifier = Modifier.height(30.dp)
                    )
                }
            }
        }

        // Radar Canvas Display
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            HistoryRadarView(
                places = places,
                selectedPlace = selectedPlace,
                radiusMeters = radiusMeters,
                onPlaceSelected = onSelectPlace,
                showSuggestedRoute = showSuggestedRoute,
                modifier = Modifier.fillMaxSize()
            )

            if (uiState is PlacesUiState.Error && places.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.connection_error),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = onRetry) {
                            Text(stringResource(R.string.retry_discovery))
                        }
                    }
                }
            }
        }

        // Bottom Selected Landmark Preview Card
        if (selectedPlace != null) {
            val context = LocalContext.current
            val isSpeaking = narrationState is AudioNarrationState.Speaking && narrationState.placeId == selectedPlace.pageId
            val isPaused = narrationState is AudioNarrationState.Speaking && narrationState.isPaused
            val catColor = getCategoryColor(selectedPlace.category)

            Card(
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDetail(selectedPlace) }
                    .testTag("radar_preview_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!selectedPlace.thumbnailUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(selectedPlace.thumbnailUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = selectedPlace.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(catColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(selectedPlace.category.emoji, style = MaterialTheme.typography.headlineSmall)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${LocationHelper.formatDistance(selectedPlace.distanceMeters)} • ${LocationHelper.formatBearingDirection(selectedPlace.bearingDegrees)} 🧭",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (selectedPlace.isVisited) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = stringResource(R.string.action_visited),
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Text(
                            text = selectedPlace.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = selectedPlace.description.ifBlank { selectedPlace.extract },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledIconButton(
                        onClick = { onToggleNarration(selectedPlace) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("radar_preview_audio_button")
                    ) {
                        Icon(
                            imageVector = if (isSpeaking && !isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.narrate_story)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoverListTabContent(
    places: List<HistoricalPlace>,
    searchQuery: String,
    selectedCategory: PlaceCategory?,
    uiState: PlacesUiState,
    narrationState: AudioNarrationState,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (PlaceCategory?) -> Unit,
    onCardClick: (HistoricalPlace) -> Unit,
    onToggleNarration: (HistoricalPlace) -> Unit,
    onToggleBookmark: (HistoricalPlace) -> Unit,
    onToggleVisited: (HistoricalPlace) -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("discover_list_tab_content")
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_search))
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("discover_search_input")
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onSelectCategory(null) },
                    label = { Text(stringResource(R.string.all_filter_format, places.size)) }
                )
            }
            items(PlaceCategory.entries.toTypedArray()) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onSelectCategory(cat) },
                    label = { Text("${cat.emoji} ${stringResource(cat.stringRes)}") }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (places.isEmpty() && uiState !is PlacesUiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏛️", style = MaterialTheme.typography.displayMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.no_landmarks_found_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.no_landmarks_found_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(places, key = { it.pageId }) { place ->
                    HistoricalCardItem(
                        place = place,
                        narrationState = narrationState,
                        onClick = { onCardClick(place) },
                        onToggleNarration = onToggleNarration,
                        onToggleBookmark = onToggleBookmark,
                        onToggleVisited = onToggleVisited
                    )
                }
            }
        }
    }
}

@Composable
private fun WalkTourTabContent(
    places: List<HistoricalPlace>,
    locationName: String,
    narrationState: AudioNarrationState,
    isAutoNarrateEnabled: Boolean,
    onToggleAutoNarrate: () -> Unit,
    onCardClick: (HistoricalPlace) -> Unit,
    onToggleNarration: (HistoricalPlace) -> Unit,
    onToggleBookmark: (HistoricalPlace) -> Unit,
    onToggleVisited: (HistoricalPlace) -> Unit
) {
    var showShareDialog by remember { mutableStateOf(false) }
    val visitedCount = places.count { it.isVisited }
    val progress = if (places.isNotEmpty()) visitedCount.toFloat() / places.size else 0f
    val visitedPlaces = places.filter { it.isVisited }
    val totalTourDistanceKm = (visitedPlaces.sumOf { it.distanceMeters } + (visitedPlaces.size * 350.0)) / 1000.0
    val tourMinutes = visitedCount * 18

    if (showShareDialog) {
        ShareProgressDialog(
            data = TourSummaryData(
                tourTitle = locationName,
                totalDistanceKm = totalTourDistanceKm,
                visitedCount = visitedCount,
                totalStops = places.size.coerceAtLeast(1),
                timeSpentMinutes = tourMinutes,
                visitedLandmarks = visitedPlaces
            ),
            onDismiss = { showShareDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("walk_tour_tab_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.historical_walking_tour),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = locationName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text("🚶‍♂️", style = MaterialTheme.typography.headlineMedium)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.tour_progress_format, visitedCount, places.size),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Headphones,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.hands_free_walking_guide),
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = stringResource(R.string.auto_narrate_subtitle),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = isAutoNarrateEnabled,
                                onCheckedChange = { onToggleAutoNarrate() },
                                modifier = Modifier.testTag("auto_narrate_switch")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FilledTonalButton(
                        onClick = { showShareDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_tour_progress_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.share_progress),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.chronological_itinerary),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        itemsIndexed(places, key = { _, item -> item.pageId }) { index, place ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (place.isVisited) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (place.isVisited) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (index < places.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(90.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(modifier = Modifier.weight(1f)) {
                    HistoricalCardItem(
                        place = place,
                        narrationState = narrationState,
                        onClick = { onCardClick(place) },
                        onToggleNarration = onToggleNarration,
                        onToggleBookmark = onToggleBookmark,
                        onToggleVisited = onToggleVisited
                    )
                }
            }
        }
    }
}

@Composable
private fun JournalTabContent(
    bookmarkedPlaces: List<HistoricalPlace>,
    visitedPlaces: List<HistoricalPlace>,
    narrationState: AudioNarrationState,
    onCardClick: (HistoricalPlace) -> Unit,
    onToggleNarration: (HistoricalPlace) -> Unit,
    onToggleBookmark: (HistoricalPlace) -> Unit,
    onToggleVisited: (HistoricalPlace) -> Unit
) {
    var selectedSection by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("journal_tab_content")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { selectedSection = 0 },
                colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (selectedSection == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("journal_offline_saved_tab")
            ) {
                Text(stringResource(R.string.saved_offline_format, bookmarkedPlaces.size))
            }

            FilledTonalButton(
                onClick = { selectedSection = 1 },
                colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (selectedSection == 1) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("journal_visited_tab")
            ) {
                Text(stringResource(R.string.visited_journal_format, visitedPlaces.size))
            }
        }

        val activeList = if (selectedSection == 0) bookmarkedPlaces else visitedPlaces

        if (activeList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (selectedSection == 0) "💾" else "📜",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(if (selectedSection == 0) R.string.no_saved_walks_title else R.string.no_visited_places_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(if (selectedSection == 0) R.string.no_saved_walks_desc else R.string.no_visited_places_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeList, key = { it.pageId }) { place ->
                    HistoricalCardItem(
                        place = place,
                        narrationState = narrationState,
                        onClick = { onCardClick(place) },
                        onToggleNarration = onToggleNarration,
                        onToggleBookmark = onToggleBookmark,
                        onToggleVisited = onToggleVisited
                    )
                }
            }
        }
    }
}
