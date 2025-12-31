package com.cs407.lineup.data

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.tasks.await

data class PlacePrediction(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String,
    val fullText: String
)

class PlacesAutocompleteHelper(context: Context, apiKey: String) {

    private val placesClient: PlacesClient
    private var sessionToken: AutocompleteSessionToken = AutocompleteSessionToken.newInstance()

    init {
        if (!Places.isInitialized()) {
            Places.initialize(context.applicationContext, apiKey)
        }
        placesClient = Places.createClient(context)
    }

    companion object {
        private const val TAG = "PlacesAutocomplete"
    }

    /**
     * Get autocomplete predictions for a query string
     */
    suspend fun getPlacePredictions(query: String): List<PlacePrediction> {
        if (query.isBlank()) return emptyList()

        return try {
            val request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(sessionToken)
                .setQuery(query)
                .build()

            val response = placesClient.findAutocompletePredictions(request).await()

            response.autocompletePredictions.map { prediction ->
                PlacePrediction(
                    placeId = prediction.placeId,
                    primaryText = prediction.getPrimaryText(null).toString(),
                    secondaryText = prediction.getSecondaryText(null).toString(),
                    fullText = prediction.getFullText(null).toString()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching predictions: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Fetch place details (coordinates) for a place ID
     */
    suspend fun getPlaceLatLng(placeId: String): LatLng? {
        return try {
            val placeFields = listOf(Place.Field.LAT_LNG)
            val request = FetchPlaceRequest.builder(placeId, placeFields)
                .setSessionToken(sessionToken)
                .build()

            val response = placesClient.fetchPlace(request).await()

            // Regenerate session token after use
            sessionToken = AutocompleteSessionToken.newInstance()

            response.place.latLng?.let { latLng ->
                LatLng(latLng.latitude, latLng.longitude)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching place details: ${e.message}", e)
            null
        }
    }
}
