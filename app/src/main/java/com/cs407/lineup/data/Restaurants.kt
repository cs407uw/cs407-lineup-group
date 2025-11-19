package com.cs407.lineup.data

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng

/**
 * restaurant data class to represent restaurants fetched from the google places api
 */
data class Restaurant(
    val name: String,
    val description: String,
    val waitTimeMinutes: Int,
    val type: String,
    val types: List<String>,
    val latLng: LatLng,
    val color: Color,
    val imageUrl: String,
    val rating: Double? = null,
    val ratingCount: Int? = null,
    val priceLevel: Int? = null,
    val isOpenNow: Boolean? = null
)

/**
 * a set of colors associated with a restaurant card. these are rotating which allows
 * for each restaurant to get a different color in a
 */
val restaurantColors = listOf(
    Color(0xFFFFCDC9),
    Color(0xFFFFECBF),
    Color(0xFFCFEDC7),
    Color(0xFFB0DFF7),
    Color(0xFFE7D5F7),
    Color(0xFFFADCF1)
)
