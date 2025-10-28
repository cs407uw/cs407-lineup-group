package com.cs407.lineup.data

import com.google.android.gms.maps.model.LatLng

data class Restaurant(
    val name: String,
    val description: String,
    val waitTimeMinutes: Int,
    val type: String,
    val latLng: LatLng
)

val HardcodedRestaurants = listOf(
    Restaurant(
        name = "Nitty Gritty",
        description = "Industrial-style tavern with varied burgers, sandwiches & other American eats, plus many beers.",
        waitTimeMinutes = 15,
        type = "American",
        latLng = LatLng(43.07180, -89.39573)
    ),
    Restaurant(
        name = "Dotty Dumpling's Dowry",
        description = "Longtime local institution offering specialty burgers, cheese curds & tap beers in quirky digs.",
        waitTimeMinutes = 20,
        type = "American",
        latLng = LatLng(43.07293, -89.39567)
    ),
    Restaurant(
        name = "Old Fashioned",
        description = "Wisconsin-themed, retro-style tavern offering beers, brats & cheese curds (all sourced in-state).",
        waitTimeMinutes = 35,
        type = "American",
        latLng = LatLng(43.07650, -89.38377)
    ),
    Restaurant(
        name = "Great Dane Pub",
        description = "Craft brews & pub eats served in a lively venue with beer garden & pool tables.",
        waitTimeMinutes = 10,
        type = "Pub/Bar",
        latLng = LatLng(43.07459, -89.38026)
    )
)
