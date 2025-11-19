import com.cs407.lineup.data.Restaurant
import com.cs407.lineup.data.restaurantColors
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
    // fetch nearby restaurants for the given lat/long
    suspend fun getNearbyRestaurants(lat: Double, lng: Double, apiKey: String): List<Restaurant> {
        return withContext(Dispatchers.IO) {
            // build url with query for lat/lng, and our specific api key
            val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?location=$lat,$lng" +
                    "&radius=1500&type=restaurant&key=$apiKey"

            // use url to execute an http request, then parse the resulting string as a json
            val result = URL(url).readText()
            val json = JSONObject(result)
            val restaurantsJson = json.getJSONArray("results")

            // since json arrays are not iterable, we have to manually access indices from
            // 0 until the last element (length = last index), then we build a restaurant for each
            // index and add to the list (https://stackoverflow.com/questions/9151619/how-to-iterate-over-a-jsonobject)
            (0 until restaurantsJson.length()).map { i ->
                val item = restaurantsJson.getJSONObject(i)
                val name = item.getString("name")
                val location = item.getJSONObject("geometry").getJSONObject("location")
                val lat2 = location.getDouble("lat")
                val lng2 = location.getDouble("lng")

                // create a restaurant out of the api response
                Restaurant(
                    name = name,
                    description = item.optString("vicinity", "No description"),
                    waitTimeMinutes = (5..40).random(),
                    type = "Restaurant",
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
        }
    }
}
