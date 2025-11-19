package com.cs407.lineup.data

import android.content.Context

/**
 * data class to represent a restaurant that the user last viewed (used for widget functionality)
 */
data class SavedRestaurant(
    val name: String?,
    val wait: Int?,
    val color: Int?
)

/**
 * SharedPreferences helper object to store and retrieve the user's last viewed restaurant
 * the object is a singleton and is used to create the home screen widget functionality so that
 * we can track the most recent interacted with restaurant even if you close the app
 */
object RestaurantPrefs {
    // shared prefs folder that we will open and write to
    private val PREFS = "restaurant_prefs"

    fun saveRestaurant(context: Context, name: String, wait: Int, color: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        // write some basic info for the widget to our prefs folder so that we can access it even when
        // app closed; the .apply() makes it save asynch
        prefs.edit().putString("name", name).putInt("wait", wait).putInt("color", color).apply()
    }

    fun loadRestaurant(context: Context): SavedRestaurant {
        // access the prefs folder we wrote to and return the saved data so we can load it and display it to
        // the home screen widget
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return SavedRestaurant(
            prefs.getString("name", null),
            if (prefs.contains("wait")) prefs.getInt("wait", 0) else null,
            if (prefs.contains("color")) prefs.getInt("color", 0) else null
        )
    }
}
