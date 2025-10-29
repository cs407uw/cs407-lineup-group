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
    ),
    Restaurant(
        name = "Lucille",
        description = "Vintage, industrial-chic tavern with 3 floors serving wood-fired & steel-pan pizzas, plus cocktails.",
        waitTimeMinutes = 15,
        type = "Italian",
        latLng = LatLng(43.07443434093093, -89.38136875043087)
    ),
    Restaurant(
        name = "A La Brasa",
        description = "Mole poblano, fajitas & other Mexican favorites doled out in a simple, brightly colored setting.",
        waitTimeMinutes = 20,
        type = "Mexican",
        latLng = LatLng(43.072012515791755, -89.3891851944116)
    ),
    Restaurant(
        name = "Canteen",
        description = "Casual Mexican grill for tacos & classic plates including brunch, with late hours & many tequilas.",
        waitTimeMinutes = 20,
        type = "Mexican",
        latLng = LatLng(43.072733364709585, -89.38403711683983)
    ),
    Restaurant(
        name = "Marigold Kitchen",
        description = "Bright, counter-serve eatery featuring creative breakfast & lunch fare plus outdoor seating.",
        waitTimeMinutes = 30,
        type = "Cafe",
        latLng = LatLng(43.074137489923885, -89.38150994070068)
    ),
    Restaurant(
        name = "Ancora Cafe",
        description = "\n" +
                "Charming cafe & bakery with house-made goods, creative egg sandwiches & brunch, plus craft coffee.",
        waitTimeMinutes = 30,
        type = "Cafe",
        latLng = LatLng(43.07456150831911, -89.38120399359781)
    ),
)
