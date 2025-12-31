package com.cs407.lineup.data

import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.lineup.BuildConfig
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel to manage manual location input with autocomplete
 * Activity-scoped to persist location across navigation
 */
class LocationHelper(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val autocompleteHelper = PlacesAutocompleteHelper(context, BuildConfig.MAPS_API_KEY)

    private val _manualLocation = MutableStateFlow<LatLng?>(null)
    val manualLocation: StateFlow<LatLng?> = _manualLocation

    private val _isManualMode = MutableStateFlow(false)
    val isManualMode: StateFlow<Boolean> = _isManualMode

    private val _geocodingError = MutableStateFlow<String?>(null)
    val geocodingError: StateFlow<String?> = _geocodingError

    private val _isGeocoding = MutableStateFlow(false)
    val isGeocoding: StateFlow<Boolean> = _isGeocoding

    // Autocomplete predictions
    private val _predictions = MutableStateFlow<List<PlacePrediction>>(emptyList())
    val predictions: StateFlow<List<PlacePrediction>> = _predictions

    private var searchJob: Job? = null

    /**
     * Set location manually by geocoding an address string
     */
    fun setManualLocation(address: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isGeocoding.value = true
            _geocodingError.value = null

            try {
                val geocoder = Geocoder(context)

                geocoder.getFromLocationName(address, 1) { addresses ->
                    viewModelScope.launch {
                        if (addresses.isNotEmpty()) {
                            val location = addresses[0]
                            val latLng = LatLng(location.latitude, location.longitude)
                            _manualLocation.value = latLng
                            _isManualMode.value = true
                            _isGeocoding.value = false
                        } else {
                            _geocodingError.value = "Location not found. Try a different address."
                            _isGeocoding.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                viewModelScope.launch {
                    _geocodingError.value = "Error: ${e.localizedMessage ?: "Unable to search"}"
                    _isGeocoding.value = false
                }
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

    fun clearGeocodingError() {
        _geocodingError.value = null
    }

    /**
     * Search for autocomplete predictions with debounce
     */
    fun searchPlacePredictions(query: String) {
        // Cancel previous search
        searchJob?.cancel()

        if (query.isBlank()) {
            _predictions.value = emptyList()
            return
        }

        // Debounce: wait 300ms before searching
        searchJob = viewModelScope.launch {
            delay(300)
            val results = autocompleteHelper.getPlacePredictions(query)
            _predictions.value = results
        }
    }

    /**
     * Select a prediction from autocomplete and set location
     */
    fun selectPrediction(prediction: PlacePrediction) {
        _isGeocoding.value = true
        _predictions.value = emptyList()

        viewModelScope.launch {
            val latLng = autocompleteHelper.getPlaceLatLng(prediction.placeId)
            if (latLng != null) {
                _manualLocation.value = latLng
                _isManualMode.value = true
                _geocodingError.value = null
            } else {
                _geocodingError.value = "Unable to get location for this place"
            }
            _isGeocoding.value = false
        }
    }

    /**
     * Clear predictions list
     */
    fun clearPredictions() {
        _predictions.value = emptyList()
    }
}