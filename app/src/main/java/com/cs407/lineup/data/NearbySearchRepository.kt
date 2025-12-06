package com.cs407.lineup.data

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

/**
 * A repository layer (in MVVM) that is responsible for fetching data regarding nearby restaurants
 * using the google places nearby API (https://developers.google.com/maps/documentation/places/web-service/legacy/search-nearby)
 * legacy nearby is simpler, but we can migrate to new nearby API if necessary
 *
 * @returns list of restaurants, each of which include: name, vicinity, coordinates, ratings,
 * rating counts, price levels, open status, and image urls
 */
class NearbySearchRepository {

    companion object {
        private const val TAG = "NearbySearchRepository"
    }

    private val firebaseRepository = FirebaseRepository()

    /**
     * Fetch nearby restaurants and their wait times from Firebase
     */
    suspend fun getNearbyRestaurants(lat: Double, lng: Double, apiKey: String): List<Restaurant> {
        return withContext(Dispatchers.IO) {
            try {
                // Check if API key is present
                if (apiKey.isBlank()) {
                    Log.e(TAG, "Google Places API key is missing!")
                    return@withContext emptyList()
                }

                // build url with query for lat/lng, and our specific api key
                val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=$lat,$lng" +
                        "&radius=1500&type=restaurant&key=$apiKey"

                // use url to execute an http request, then parse the resulting string as a json
                val result = URL(url).readText()
                val json = JSONObject(result)

                // Check API response status
                val status = json.optString("status", "UNKNOWN")
                if (status != "OK" && status != "ZERO_RESULTS") {
                    Log.e(TAG, "Places API error: $status - ${json.optString("error_message", "No error message")}")
                    return@withContext emptyList()
                }

                val restaurantsJson = json.getJSONArray("results")

                // First, parse all restaurants from Google
                val restaurants = (0 until restaurantsJson.length()).map { i ->
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
                        waitTimeMinutes = null,  // Will be filled in from Firebase
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

                // Fetch wait times from Firebase for all restaurants
                val placeIds = restaurants.map { it.id }
                val waitTimes = firebaseRepository.getWaitTimesForVenues(placeIds)

                // Merge wait times into restaurants
                restaurants.map { restaurant ->
                    restaurant.copy(waitTimeMinutes = waitTimes[restaurant.id])
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching restaurants: ${e.message}", e)
                emptyList()
            }
        }
    }

    /**
     * convert google place types from the api to our internal category labels;
     * defaults to "Restaurant" if no better match is found (can change this to establishment
     * or something broad like that if we want)
     */
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
        // the place types are in underscore variable format, so we reformat:
        if (types.any { it.contains("bar") }) reformattedTypes.add("Bar")
        if (types.any { it.contains("cafe") }) reformattedTypes.add("Cafe")
        if (types.any { it.contains("grocery") || it.contains("supermarket") }) reformattedTypes.add("Grocery")
        if (types.any { it.contains("restaurant") }) reformattedTypes.add("Restaurant")

        return reformattedTypes
    }



}
