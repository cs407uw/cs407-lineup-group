package com.cs407.lineup.data

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Helper class to manage manual location input
 * Works alongside LocationViewModel to provide manual location override functionality
 */
class LocationHelper(private val context: Context) {

    private val _manualLocation = MutableStateFlow<LatLng?>(null)
    val manualLocation: StateFlow<LatLng?> = _manualLocation

    private val _isManualMode = MutableStateFlow(false)
    val isManualMode: StateFlow<Boolean> = _isManualMode

    /**
     * Set location manually by geocoding an address string
     */
    fun setManualLocation(address: String, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context)

                geocoder.getFromLocationName(address, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val location = addresses[0]
                        val latLng = LatLng(location.latitude, location.longitude)
                        scope.launch {
                            _manualLocation.value = latLng
                            _isManualMode.value = true
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun useCurrentLocation() {
        _isManualMode.value = false
        _manualLocation.value = null
    }

    fun getEffectiveLocation(gpsLocation: LatLng?): LatLng? {
        return if (_isManualMode.value && _manualLocation.value != null) {
            _manualLocation.value
        } else {
            gpsLocation
        }
    }
}