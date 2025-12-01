package com.cs407.lineup.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cs407.lineup.data.Restaurant
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
fun RestaurantMap(
    showSheet: Boolean,
    cameraPositionState: CameraPositionState,
    userLocation: LatLng?,
    nearbyRestaurants: List<Restaurant>,
    selected: Restaurant?
){
    // main google map UI; bottom padding shrinks the map when the sheet is open
    GoogleMap(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = if (showSheet) 220.dp else 0.dp),
        cameraPositionState = cameraPositionState
    ) {
        // create marker for user's current location (blue)
        userLocation?.let { loc ->
            Marker(
                state = MarkerState(position = loc),
                title = "You",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
            )
        }
        // draw all nearby restaurants w/ red marker
        nearbyRestaurants.forEach { restaurant ->
            Marker(
                state = MarkerState(position = restaurant.latLng),
                title = restaurant.name,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
        }
        // highlight marker for the currently selected restaurant
        selected?.let { restaurant ->
            Marker(
                state = MarkerState(position = restaurant.latLng), title = restaurant.name
            )
        }
    }
}