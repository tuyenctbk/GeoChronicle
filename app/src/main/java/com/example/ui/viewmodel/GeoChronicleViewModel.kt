package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioNarrationState
import com.example.audio.HistoryAudioNarrator
import com.example.data.db.GeoChronicleDatabase
import com.example.data.model.HistoricalPlace
import com.example.data.model.PlaceCategory
import com.example.data.model.SupportedLanguages
import com.example.data.model.TourLanguage
import com.example.data.repository.HistoryRepository
import com.example.location.HistoricalPresets
import com.example.location.HistoricalTourLocation
import com.example.location.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

sealed interface PlacesUiState {
    data object Loading : PlacesUiState
    data class Success(val places: List<HistoricalPlace>) : PlacesUiState
    data class Error(val message: String, val cachedPlaces: List<HistoricalPlace>) : PlacesUiState
}

enum class NavigationTab {
    RADAR,
    LIST,
    WALK_TOUR,
    JOURNAL,
    DASHBOARD
}

class GeoChronicleViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GeoChronicleDatabase.getInstance(application)
    private val repository = HistoryRepository(db.historicalPlaceDao())
    val narrator = HistoryAudioNarrator(application)
    private val donationManager = com.example.data.repository.DonationManager(application)

    val isSupporter: StateFlow<Boolean> = donationManager.isSupporter
    val supporterTier: StateFlow<String> = donationManager.supporterTier

    private val _showHomeBanner = MutableStateFlow(false)
    val showHomeBanner: StateFlow<Boolean> = _showHomeBanner.asStateFlow()

    private val _showDonationDialog = MutableStateFlow(false)
    val showDonationDialog: StateFlow<Boolean> = _showDonationDialog.asStateFlow()

    // Current coordinates and name
    private val _currentLocation = MutableStateFlow(HistoricalPresets.DEFAULT)
    val currentLocation: StateFlow<HistoricalTourLocation> = _currentLocation.asStateFlow()

    private val _radiusMeters = MutableStateFlow(1000)
    val radiusMeters: StateFlow<Int> = _radiusMeters.asStateFlow()

    private val _selectedCategory = MutableStateFlow<PlaceCategory?>(null)
    val selectedCategory: StateFlow<PlaceCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _placesUiState = MutableStateFlow<PlacesUiState>(PlacesUiState.Loading)
    val placesUiState: StateFlow<PlacesUiState> = _placesUiState.asStateFlow()

    private val _selectedPlaceForDetail = MutableStateFlow<HistoricalPlace?>(null)
    val selectedPlaceForDetail: StateFlow<HistoricalPlace?> = _selectedPlaceForDetail.asStateFlow()

    private val _currentNavTab = MutableStateFlow(NavigationTab.RADAR)
    val currentNavTab: StateFlow<NavigationTab> = _currentNavTab.asStateFlow()

    private val _isAutoNarrateEnabled = MutableStateFlow(false)
    val isAutoNarrateEnabled: StateFlow<Boolean> = _isAutoNarrateEnabled.asStateFlow()

    // Tour Language (defaulting to device language if supported)
    private val initialLang = detectDefaultLanguage()
    private val _tourLanguage = MutableStateFlow(initialLang)
    val tourLanguage: StateFlow<TourLanguage> = _tourLanguage.asStateFlow()

    val narrationState: StateFlow<AudioNarrationState> = narrator.state

    val bookmarkedPlaces: StateFlow<List<HistoricalPlace>> = repository.bookmarkedPlaces
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val visitedPlaces: StateFlow<List<HistoricalPlace>> = repository.visitedPlaces
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        donationManager.recordAppLaunch()
        narrator.setLanguage(initialLang.code)
        loadPlaces()
        checkDonationTriggers()
    }

    private fun checkDonationTriggers() {
        _showHomeBanner.value = donationManager.shouldShowHomeBanner()
        if (donationManager.shouldShowAutoPrompt()) {
            _showDonationDialog.value = true
            donationManager.recordAutoPromptShown()
        }
    }

    fun recordUserOperation() {
        donationManager.recordOperationCompleted()
        checkDonationTriggers()
    }

    fun dismissHomeBanner() {
        donationManager.dismissHomeBanner()
        _showHomeBanner.value = false
    }

    fun openDonationDialog() {
        _showDonationDialog.value = true
    }

    fun closeDonationDialog() {
        _showDonationDialog.value = false
    }

    fun confirmDonation(tier: String) {
        donationManager.markAsSupporter(tier)
        _showHomeBanner.value = false
    }

    private fun detectDefaultLanguage(): TourLanguage {
        val sysLang = Locale.getDefault().language
        return SupportedLanguages.LANGUAGES.find { it.code.equals(sysLang, ignoreCase = true) }
            ?: SupportedLanguages.DEFAULT
    }

    fun setTourLanguage(lang: TourLanguage) {
        if (_tourLanguage.value != lang) {
            _tourLanguage.value = lang
            narrator.setLanguage(lang.code)
            loadPlaces()
        }
    }

    fun setNavTab(tab: NavigationTab) {
        _currentNavTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setRadius(radius: Int) {
        if (_radiusMeters.value != radius) {
            _radiusMeters.value = radius
            loadPlaces()
        }
    }

    fun selectCategory(category: PlaceCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun selectPlaceForDetail(place: HistoricalPlace?) {
        _selectedPlaceForDetail.value = place
    }

    fun setLocationPreset(preset: HistoricalTourLocation) {
        _currentLocation.value = preset
        loadPlaces()
    }

    fun setCustomLocation(lat: Double, lon: Double, name: String) {
        _currentLocation.value = HistoricalTourLocation(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            region = "Custom Coordinates",
            lat = lat,
            lon = lon,
            iconEmoji = "📍",
            description = "Custom coordinates ($lat, $lon)"
        )
        loadPlaces()
        viewModelScope.launch {
            val geocoded = repository.reverseGeocode(lat, lon, _tourLanguage.value.code)
            if (geocoded.isNotBlank() && geocoded != "Historic District") {
                _currentLocation.value = _currentLocation.value.copy(name = geocoded)
            }
        }
    }

    fun useDeviceGps(context: Context) {
        viewModelScope.launch {
            _placesUiState.value = PlacesUiState.Loading
            val loc = LocationHelper.getCurrentLocation(context)
            if (loc != null) {
                val geocoded = repository.reverseGeocode(loc.latitude, loc.longitude, _tourLanguage.value.code)
                _currentLocation.value = HistoricalTourLocation(
                    id = "gps",
                    name = geocoded.ifBlank { "Current GPS Location" },
                    region = "Device GPS",
                    lat = loc.latitude,
                    lon = loc.longitude,
                    iconEmoji = "📍",
                    description = "Live walker coordinates"
                )
                loadPlaces()
            } else {
                loadPlaces()
            }
        }
    }

    fun loadPlaces() {
        viewModelScope.launch {
            _placesUiState.value = PlacesUiState.Loading
            val loc = _currentLocation.value
            val radius = _radiusMeters.value
            val langCode = _tourLanguage.value.code

            val result = repository.fetchNearbyHistory(loc.lat, loc.lon, radius, langCode)
            result.onSuccess { places ->
                _placesUiState.value = PlacesUiState.Success(places)
                if (_isAutoNarrateEnabled.value && places.isNotEmpty()) {
                    val nearest = places.firstOrNull { !it.isVisited }
                    if (nearest != null && narrator.state.value is AudioNarrationState.Idle) {
                        narrator.speak(nearest)
                    }
                }
            }.onFailure { err ->
                _placesUiState.value = PlacesUiState.Error(
                    message = err.message ?: "Failed to fetch historical landmarks.",
                    cachedPlaces = emptyList()
                )
            }
        }
    }

    fun toggleAutoNarrate() {
        val next = !_isAutoNarrateEnabled.value
        _isAutoNarrateEnabled.value = next
        if (next) {
            val state = _placesUiState.value
            if (state is PlacesUiState.Success) {
                val nearest = state.places.firstOrNull { !it.isVisited }
                if (nearest != null) {
                    narrator.speak(nearest)
                }
            }
        }
    }

    fun toggleNarration(place: HistoricalPlace) {
        narrator.togglePlayPause(place)
    }

    fun togglePlayPauseCurrent() {
        val current = narrator.state.value
        if (current is AudioNarrationState.Speaking) {
            val state = _placesUiState.value
            if (state is PlacesUiState.Success) {
                val place = state.places.find { it.pageId == current.placeId }
                if (place != null) {
                    narrator.togglePlayPause(place)
                    return
                }
            }
            if (current.isPaused) {
                val dummyPlace = HistoricalPlace(
                    pageId = current.placeId,
                    title = current.title,
                    description = "",
                    extract = current.text,
                    lat = 0.0,
                    lon = 0.0,
                    distanceMeters = 0.0,
                    bearingDegrees = 0f,
                    thumbnailUrl = null,
                    category = PlaceCategory.GENERAL
                )
                narrator.speak(dummyPlace)
            } else {
                narrator.stop()
            }
        }
    }

    fun stopNarration() {
        narrator.stop()
    }

    fun cycleSpeechRate(): Float {
        return narrator.cycleSpeechRate()
    }

    fun toggleBookmark(place: HistoricalPlace) {
        viewModelScope.launch {
            repository.toggleBookmark(place)
            updatePlaceInMemory(place.copy(isBookmarked = !place.isBookmarked))
            recordUserOperation()
        }
    }

    fun toggleVisited(place: HistoricalPlace) {
        viewModelScope.launch {
            val nextVisited = !place.isVisited
            repository.toggleVisited(place)
            updatePlaceInMemory(
                place.copy(
                    isVisited = nextVisited,
                    visitedTimestamp = if (nextVisited) System.currentTimeMillis() else null
                )
            )
            recordUserOperation()
        }
    }

    fun saveNote(pageId: Long, note: String) {
        viewModelScope.launch {
            repository.saveNote(pageId, note)
            val state = _placesUiState.value
            if (state is PlacesUiState.Success) {
                val updated = state.places.map {
                    if (it.pageId == pageId) it.copy(userNote = note) else it
                }
                _placesUiState.value = PlacesUiState.Success(updated)
            }
            if (_selectedPlaceForDetail.value?.pageId == pageId) {
                _selectedPlaceForDetail.value = _selectedPlaceForDetail.value?.copy(userNote = note)
            }
            recordUserOperation()
        }
    }

    private fun updatePlaceInMemory(updated: HistoricalPlace) {
        val state = _placesUiState.value
        if (state is PlacesUiState.Success) {
            val newPlaces = state.places.map {
                if (it.pageId == updated.pageId) updated else it
            }
            _placesUiState.value = PlacesUiState.Success(newPlaces)
        }
        if (_selectedPlaceForDetail.value?.pageId == updated.pageId) {
            _selectedPlaceForDetail.value = updated
        }
    }

    override fun onCleared() {
        super.onCleared()
        narrator.release()
    }
}
