package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.annotation.StringRes
import com.example.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class HistoricalTourLocation(
    val id: String,
    val name: String,
    val region: String,
    val lat: Double,
    val lon: Double,
    val iconEmoji: String,
    val description: String,
    @StringRes val nameRes: Int? = null,
    @StringRes val descRes: Int? = null
)

object HistoricalPresets {
    val PRESETS = listOf(
        HistoricalTourLocation(
            id = "rome",
            name = "Rome: Colosseum & Imperial Fora",
            region = "Italy",
            lat = 41.8902,
            lon = 12.4922,
            iconEmoji = "🏛️",
            description = "Heart of the Roman Empire, ancient temples, arches, and gladiator arenas.",
            nameRes = R.string.preset_rome_name,
            descRes = R.string.preset_rome_desc
        ),
        HistoricalTourLocation(
            id = "london",
            name = "London: Tower & Ancient City",
            region = "United Kingdom",
            lat = 51.5081,
            lon = -0.0759,
            iconEmoji = "🏰",
            description = "Medieval fortresses, historic Roman wall remnants, and Thames heritage.",
            nameRes = R.string.preset_london_name,
            descRes = R.string.preset_london_desc
        ),
        HistoricalTourLocation(
            id = "boston",
            name = "Boston: Freedom Trail",
            region = "United States",
            lat = 42.3601,
            lon = -71.0589,
            iconEmoji = "🔔",
            description = "Cradle of the American Revolution, historic meetinghouses, and old burial grounds.",
            nameRes = R.string.preset_boston_name,
            descRes = R.string.preset_boston_desc
        ),
        HistoricalTourLocation(
            id = "paris",
            name = "Paris: Latin Quarter & Notre-Dame",
            region = "France",
            lat = 48.8530,
            lon = 2.3499,
            iconEmoji = "🥖",
            description = "Roman thermals, medieval universities, Pantheon, and ancient Île de la Cité.",
            nameRes = R.string.preset_paris_name,
            descRes = R.string.preset_paris_desc
        ),
        HistoricalTourLocation(
            id = "kyoto",
            name = "Kyoto: Gion & Higashiyama",
            region = "Japan",
            lat = 34.9961,
            lon = 135.7797,
            iconEmoji = "⛩️",
            description = "Centuries-old wooden pagodas, imperial shrines, and historic samurai districts.",
            nameRes = R.string.preset_kyoto_name,
            descRes = R.string.preset_kyoto_desc
        ),
        HistoricalTourLocation(
            id = "athens",
            name = "Athens: Acropolis & Plaka",
            region = "Greece",
            lat = 37.9715,
            lon = 23.7257,
            iconEmoji = "🏺",
            description = "Birthplace of democracy, Parthenon, ancient Agora, and historic alleys.",
            nameRes = R.string.preset_athens_name,
            descRes = R.string.preset_athens_desc
        ),
        HistoricalTourLocation(
            id = "philly",
            name = "Philadelphia: Independence Mall",
            region = "United States",
            lat = 39.9489,
            lon = -75.1500,
            iconEmoji = "📜",
            description = "Independence Hall, Liberty Bell, and colonial cobblestone streets.",
            nameRes = R.string.preset_philly_name,
            descRes = R.string.preset_philly_desc
        ),
        HistoricalTourLocation(
            id = "cairo",
            name = "Cairo: Islamic Citadel & Old Gates",
            region = "Egypt",
            lat = 30.0299,
            lon = 31.2611,
            iconEmoji = "🕌",
            description = "Medieval Islamic architecture, Saladin Citadel, and thousand-year-old minarets.",
            nameRes = R.string.preset_cairo_name,
            descRes = R.string.preset_cairo_desc
        )
    )

    val DEFAULT = PRESETS[0]
}

object LocationHelper {

    fun calculateDistanceMeters(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): Double {
        val results = FloatArray(1)
        Location.distanceBetween(startLat, startLon, endLat, endLon, results)
        return results[0].toDouble()
    }

    /**
     * Calculates bearing from starting coordinate to target coordinate in degrees (0..360).
     */
    fun calculateBearing(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): Float {
        val lat1 = Math.toRadians(startLat)
        val lon1 = Math.toRadians(startLon)
        val lat2 = Math.toRadians(endLat)
        val lon2 = Math.toRadians(endLon)

        val dLon = lon2 - lon1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)

        val initialBearing = Math.toDegrees(atan2(y, x))
        val normalizedBearing = (initialBearing + 360) % 360
        return normalizedBearing.toFloat()
    }

    fun formatBearingDirection(bearing: Float): String {
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")
        val index = ((bearing + 22.5f) / 45).toInt() % 8
        return directions[index]
    }

    fun formatDistance(meters: Double): String {
        return if (meters < 1000) {
            "${meters.toInt()} m"
        } else {
            String.format(java.util.Locale.US, "%.1f km", meters / 1000.0)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationSource = CancellationTokenSource()

        return suspendCancellableCoroutine { continuation ->
            try {
                fusedClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationSource.token
                ).addOnSuccessListener { loc ->
                    continuation.resume(loc)
                }.addOnFailureListener {
                    fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                        continuation.resume(lastLoc)
                    }.addOnFailureListener {
                        continuation.resume(null)
                    }
                }
            } catch (_: SecurityException) {
                continuation.resume(null)
            } catch (_: Exception) {
                continuation.resume(null)
            }
        }
    }
}
