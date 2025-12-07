package com.cs407.lineup.data

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.lineup.BuildConfig
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// caches restaurant data so back button doesnt reload everything
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val repository = NearbySearchRepository()

    private val _nearbyRestaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val nearbyRestaurants: StateFlow<List<Restaurant>> = _nearbyRestaurants.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var lastFetchedLocation: LatLng? = null
    private val refetchDistanceThreshold = 100.0 // meters

    private val _needsRefresh = MutableStateFlow(false)
    val needsRefresh: StateFlow<Boolean> = _needsRefresh.asStateFlow()

    val searchQuery = mutableStateOf("")
    val selectedCategories = mutableStateOf(setOf("All Establishments"))
    val showOnlyFavorites = mutableStateOf(false)
    val sortOption = mutableStateOf("Distance")


    fun markNeedsRefresh() {
        Log.d(TAG, "marked for refresh")
        _needsRefresh.value = true
    }

    fun clearNeedsRefresh() {
        _needsRefresh.value = false
    }

    // fetches restaurants, skips if we have cached data nearby
    fun fetchNearbyRestaurants(location: LatLng, forceRefresh: Boolean = false) {
        if (_isLoading.value && !forceRefresh) {
            Log.d(TAG, "already loading, skip")
            return
        }

        // use cache if location hasnt changed much
        if (!forceRefresh && _nearbyRestaurants.value.isNotEmpty() && lastFetchedLocation != null) {
            val distance = calculateDistance(lastFetchedLocation!!, location)
            if (distance < refetchDistanceThreshold) {
                Log.d(TAG, "using cached data (${distance.toInt()}m)")
                return
            }
        }

        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "fetching for ${location.latitude}, ${location.longitude}")

            try {
                val results = repository.getNearbyRestaurants(
                    lat = location.latitude,
                    lng = location.longitude,
                    apiKey = BuildConfig.MAPS_API_KEY
                )
                _nearbyRestaurants.value = results
                lastFetchedLocation = location
                Log.d(TAG, "got ${results.size} restaurants")
            } catch (e: Exception) {
                Log.e(TAG, "fetch error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // force refresh after submitting wait time
    fun refreshWaitTimes() {
        lastFetchedLocation?.let { location ->
            Log.d(TAG, "force refresh")
            fetchNearbyRestaurants(location, forceRefresh = true)
        }
    }

    private fun calculateDistance(from: LatLng, to: LatLng): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            from.latitude, from.longitude,
            to.latitude, to.longitude,
            results
        )
        return results[0].toDouble()
    }
}

