package com.cs407.lineup.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.lineup.BuildConfig
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for HomeScreen that caches restaurant data across navigation.
 * This prevents re-fetching when returning from RestaurantDetailScreen.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    companion object {
        private const val TAG = "HomeViewModel"
    }
    
    private val repository = NearbySearchRepository()
    
    // Cached restaurant data
    private val _nearbyRestaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val nearbyRestaurants: StateFlow<List<Restaurant>> = _nearbyRestaurants.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Track last fetched location to avoid redundant fetches
    private var lastFetchedLocation: LatLng? = null

    // Distance threshold (in meters) before refetching - ~100 meters
    private val refetchDistanceThreshold = 100.0

    // Flag to trigger refresh when returning to HomeScreen
    private val _needsRefresh = MutableStateFlow(false)
    val needsRefresh: StateFlow<Boolean> = _needsRefresh.asStateFlow()

    /**
     * Mark that wait times need to be refreshed (called after saving a wait time)
     */
    fun markNeedsRefresh() {
        Log.d(TAG, "Marked for refresh - will re-fetch when returning to HomeScreen")
        _needsRefresh.value = true
    }

    /**
     * Clear the needs refresh flag
     */
    fun clearNeedsRefresh() {
        _needsRefresh.value = false
    }
    
    /**
     * Fetch nearby restaurants if needed.
     * Will skip fetching if we already have data for a nearby location.
     */
    fun fetchNearbyRestaurants(location: LatLng, forceRefresh: Boolean = false) {
        // Skip if already loading
        if (_isLoading.value && !forceRefresh) {
            Log.d(TAG, "Already loading, skipping fetch")
            return
        }
        
        // Skip if we already have data and haven't moved significantly
        if (!forceRefresh && _nearbyRestaurants.value.isNotEmpty() && lastFetchedLocation != null) {
            val distance = calculateDistance(lastFetchedLocation!!, location)
            if (distance < refetchDistanceThreshold) {
                Log.d(TAG, "Location hasn't changed significantly (${distance.toInt()}m), using cached data")
                return
            }
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "Fetching restaurants for location: ${location.latitude}, ${location.longitude}")
            
            try {
                val results = repository.getNearbyRestaurants(
                    lat = location.latitude,
                    lng = location.longitude,
                    apiKey = BuildConfig.MAPS_API_KEY
                )
                
                _nearbyRestaurants.value = results
                lastFetchedLocation = location
                Log.d(TAG, "Fetched ${results.size} restaurants")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching restaurants", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Force refresh restaurant data (e.g., after submitting a wait time)
     */
    fun refreshWaitTimes() {
        lastFetchedLocation?.let { location ->
            Log.d(TAG, "Force refreshing wait times...")
            fetchNearbyRestaurants(location, forceRefresh = true)
        }
    }
    
    /**
     * Calculate distance between two LatLng points in meters
     */
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

