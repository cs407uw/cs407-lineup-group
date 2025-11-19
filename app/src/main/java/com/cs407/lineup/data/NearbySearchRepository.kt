import com.cs407.lineup.data.Restaurant
import com.cs407.lineup.data.restaurantColors
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class NearbySearchRepository {

    suspend fun getNearbyRestaurants(lat: Double, lng: Double, apiKey: String): List<Restaurant> {
        return withContext(Dispatchers.IO) {
            val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?location=$lat,$lng" +
                    "&radius=1500&type=restaurant&key=$apiKey"

            val result = URL(url).readText()
            val json = JSONObject(result)
            val restaurantsJson = json.getJSONArray("results")

            (0 until restaurantsJson.length()).map { i ->
                val item = restaurantsJson.getJSONObject(i)
                val name = item.getString("name")
                val location = item.getJSONObject("geometry").getJSONObject("location")
                val lat2 = location.getDouble("lat")
                val lng2 = location.getDouble("lng")

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
                    } ?: ""
                )
            }
        }
    }
}
