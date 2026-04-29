package com.gagan.lokdiksha.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Build
import android.provider.Settings
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Device utility functions for hardware UUID, GPS, and anti-spoofing.
 */
object DeviceUtils {

    /**
     * Get a unique device identifier (ANDROID_ID).
     * This persists across app reinstalls on the same device.
     */
    @SuppressLint("HardwareIds")
    fun getHardwareUuid(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown-device"
    }

    /**
     * Check if a location is from a mock provider (anti-spoofing).
     * Returns true if the location is spoofed.
     */
    fun isMockLocation(location: Location): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            @Suppress("DEPRECATION")
            location.isFromMockProvider
        }
    }

    /**
     * Get the current GPS location using FusedLocationProviderClient.
     * Requires ACCESS_FINE_LOCATION permission.
     *
     * @return Location or null if unavailable
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        val fusedClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)
        val cancellationToken = CancellationTokenSource()

        return suspendCancellableCoroutine { continuation ->
            fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).addOnSuccessListener { location ->
                continuation.resume(location)
            }.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }

            continuation.invokeOnCancellation {
                cancellationToken.cancel()
            }
        }
    }
}
