package com.cs407.lineup.data

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * View model responsible for handling live user location updates; uses google fused location
 * provider.
 */
class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _location = MutableStateFlow<LatLng?>(null)
    val location: StateFlow<LatLng?> = _location.asStateFlow()

    // utilize high accuracy when available, and checks at an interval of
    // 3000ms with a min of 2000ms between updates
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 3000
    ).setMinUpdateIntervalMillis(2000).build()

    // the callback gets triggered whenever the device location changes; it updates
    // the stateflow with the latest location retrieved.
    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            _location.value = LatLng(loc.latitude, loc.longitude)
        }
    }

    // launch the location udpate request process; utilizes the callback above
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        fusedClient.requestLocationUpdates(locationRequest, callback, null)
    }
}