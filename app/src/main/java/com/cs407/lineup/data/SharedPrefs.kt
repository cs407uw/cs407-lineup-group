package com.cs407.lineup.data

import android.content.Context

data class SavedRestaurant(
    val name: String?,
    val wait: Int?,
    val color: Int?
)

object RestaurantPrefs {

    private val PREFS = "restaurant_prefs"

    fun saveRestaurant(context: Context, name: String, wait: Int, color: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("name", name)
            .putInt("wait", wait)
            .putInt("color", color)
            .apply()
    }

    fun loadRestaurant(context: Context): SavedRestaurant {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return SavedRestaurant(
            prefs.getString("name", null),
            if (prefs.contains("wait")) prefs.getInt("wait", 0) else null,
            if (prefs.contains("color")) prefs.getInt("color", 0) else null
        )
    }
}
