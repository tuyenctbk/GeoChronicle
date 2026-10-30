package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Service class utilizing Google Play Services Location (FusedLocationProviderClient)
 * to retrieve the user's current GPS coordinates and stream live location updates.
 * Essential for the historical radar view, proximity narration triggers, and walking tours.
 */
class GpsLocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    /**
     * Checks if either FINE or COARSE location permission is granted.
     */
    fun hasLocationPermission(): Boolean {
        val finePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarsePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return finePermission || coarsePermission
    }

    /**
     * Checks if device GPS or Network location provider is turned on.
     */
    fun isGpsProviderEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }

    /**
     * Retrieves the single current GPS location with high accuracy.
     * Falls back to [FusedLocationProviderClient.getLastLocation] if current reading fails.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentCoordinates(): Result<Location> {
        if (!hasLocationPermission()) {
            return Result.failure(SecurityException("Location permission not granted"))
        }

        val cancellationSource = CancellationTokenSource()

        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationSource.token
                ).addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        continuation.resume(Result.success(location))
                    } else {
                        // Fallback to last known location
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                            if (lastLoc != null) {
                                continuation.resume(Result.success(lastLoc))
                            } else {
                                continuation.resume(Result.failure(Exception("GPS location currently unavailable")))
                            }
                        }.addOnFailureListener { error ->
                            continuation.resume(Result.failure(error))
                        }
                    }
                }.addOnFailureListener { error ->
                    // Attempt last location on primary failure
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                        if (lastLoc != null) {
                            continuation.resume(Result.success(lastLoc))
                        } else {
                            continuation.resume(Result.failure(error))
                        }
                    }.addOnFailureListener {
                        continuation.resume(Result.failure(error))
                    }
                }

                continuation.invokeOnCancellation {
                    cancellationSource.cancel()
                }
            } catch (e: SecurityException) {
                continuation.resume(Result.failure(e))
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }
    }

    /**
     * Streams continuous live GPS location updates as the user walks on a historical tour.
     * Uses [callbackFlow] with lifecycle cleanup when the flow collection stops.
     */
    @SuppressLint("MissingPermission")
    fun observeLiveLocation(
        intervalMs: Long = 4000L,
        minDistanceMeters: Float = 3f
    ): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            close(SecurityException("Location permission not granted"))
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .setMinUpdateDistanceMeters(minDistanceMeters)
            .setWaitForAccurateLocation(false)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    trySend(loc)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            close(e)
            return@callbackFlow
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
