package com.cs407.lineup.data

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng

data class Restaurant(
    val id: String,
    val name: String,
    val description: String,
    val waitTimeMinutes: Int?,
    val type: String,
    val types: List<String>,
    val latLng: LatLng,
    val color: Color,
    val imageUrl: String,
    val rating: Double? = null,
    val ratingCount: Int? = null,
    val priceLevel: Int? = null,
    val isOpenNow: Boolean? = null
) {
    val waitTimeDisplay: String
        get() = waitTimeMinutes?.let { "$it min" } ?: "—"
}

// rotating colors for restaurant cards
val restaurantColors = listOf(
    Color(0xFFFFCDC9),
    Color(0xFFFFECBF),
    Color(0xFFCFEDC7),
    Color(0xFFB0DFF7),
    Color(0xFFE7D5F7),
    Color(0xFFFADCF1)
)