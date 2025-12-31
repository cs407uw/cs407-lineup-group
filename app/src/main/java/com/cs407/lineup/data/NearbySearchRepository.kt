package com.cs407.lineup.data

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

// fetches nearby restaurants from google places api
class NearbySearchRepository {

    companion object {
        private const val TAG = "NearbySearchRepository"
    }

    private val firebaseRepository = FirebaseRepository()

    suspend fun getNearbyRestaurants(lat: Double, lng: Double, apiKey: String): List<Restaurant> {
        return withContext(Dispatchers.IO) {
            try {
                if (apiKey.isBlank()) {
                    Log.e(TAG, "api key missing")
                    return@withContext emptyList()
                }

                // Fetch multiple types of establishments in parallel
                val types = listOf("restaurant", "cafe", "bar", "supermarket")

                // Launch all API calls in parallel using async
                val deferredResults = types.map { type ->
                    async(Dispatchers.IO) {
                        try {
                            val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                                    "?location=$lat,$lng" +
                                    "&radius=3000&type=$type&key=$apiKey"

                            val result = URL(url).readText()
                            val json = JSONObject(result)

                            val status = json.optString("status", "UNKNOWN")
                            if (status != "OK" && status != "ZERO_RESULTS") {
                                Log.e(TAG, "places api error for $type: $status")
                                return@async emptyList()
                            }

                            val restaurantsJson = json.getJSONArray("results")

                            (0 until restaurantsJson.length()).map { i ->
                    val item = restaurantsJson.getJSONObject(i)
                    val placeId = item.getString("place_id")
                    val name = item.getString("name")
                    val location = item.getJSONObject("geometry").getJSONObject("location")
                    val lat2 = location.getDouble("lat")
                    val lng2 = location.getDouble("lng")

                    val googleTypesJson = item.optJSONArray("types")
                    val googleTypes: List<String> =
                        if (googleTypesJson != null) {
                            (0 until googleTypesJson.length()).map { index ->
                                googleTypesJson.getString(index)
                            }
                        } else {
                            emptyList()
                        }
                    val prettyTypes = mapPlaceTypesToFormattedCategory(googleTypes)

                                Restaurant(
                                    id = placeId,
                                    name = name,
                                    description = item.optString("vicinity", "No description"),
                                    waitTimeMinutes = null,
                                    type = mapPlaceTypesToCategory(googleTypes),
                                    types = prettyTypes,
                                    latLng = LatLng(lat2, lng2),
                                    color = restaurantColors[i % restaurantColors.size],
                                    imageUrl = item.optJSONArray("photos")?.let {
                                        val photoRef = it.getJSONObject(0).getString("photo_reference")
                                        "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference=$photoRef&key=$apiKey"
                                    } ?: "",
                                    rating = item.optDouble("rating", Double.NaN).takeIf { !it.isNaN() },
                                    ratingCount = item.optInt("user_ratings_total").takeIf { it != 0 },
                                    priceLevel = item.optInt("price_level").takeIf { it != 0 },
                                    isOpenNow = item.optJSONObject("opening_hours")?.optBoolean("open_now")
                                )
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "fetch error for $type: ${e.message}", e)
                            emptyList()
                        }
                    }
                }

                // Wait for all parallel API calls to complete
                val allResults = deferredResults.awaitAll().flatten()

                // Remove duplicates by place ID
                val uniqueRestaurants = allResults.distinctBy { it.id }

                // get wait times from firebase with timestamps
                val placeIds = uniqueRestaurants.map { it.id }
                val waitTimeData = firebaseRepository.getWaitTimeDataForVenues(placeIds)

                // merge wait times, timestamps, and report counts
                uniqueRestaurants.map { restaurant ->
                    val data = waitTimeData[restaurant.id]
                    restaurant.copy(
                        waitTimeMinutes = data?.waitMinutes,
                        waitTimeReportedAt = data?.reportedAt,
                        waitTimeReportCount = data?.reportCount ?: 1
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "fetch error: ${e.message}", e)
                emptyList()
            }
        }
    }

    // maps google types to our categories
    private fun mapPlaceTypesToCategory(types: List<String>): String {
        return when {
            types.any { it.contains("bar") } -> "Bar"
            types.any { it.contains("cafe") } -> "Cafe"
            types.any { it.contains("grocery") || it.contains("supermarket") } -> "Grocery"
            else -> "Restaurant"
        }
    }

    private fun mapPlaceTypesToFormattedCategory(types: List<String>): List<String> {
        val reformattedTypes = mutableListOf<String>()
        if (types.any { it.contains("bar") }) reformattedTypes.add("Bar")
        if (types.any { it.contains("cafe") }) reformattedTypes.add("Cafe")
        if (types.any { it.contains("grocery") || it.contains("supermarket") }) reformattedTypes.add("Grocery")
        if (types.any { it.contains("restaurant") }) reformattedTypes.add("Restaurant")
        return reformattedTypes
    }
}
