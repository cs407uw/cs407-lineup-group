package com.cs407.lineup.data

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng

data class Restaurant(
    val name: String,
    val description: String,
    val waitTimeMinutes: Int,
    val type: String,
    val latLng: LatLng,
    val color: Color,
    val imageUrl: String
)

val restaurantColors = listOf(
    Color(0xFFFFCDC9),
    Color(0xFFFFECBF),
    Color(0xFFCFEDC7),
    Color(0xFFB0DFF7),
    Color(0xFFE7D5F7),
    Color(0xFFFADCF1)
)

val HardcodedRestaurants = listOf(
    // the colors get overwritten, at the end, just didn't want to hardcode them
    // completely, wanted to use index-based coloring w/ modulus
    Restaurant(
        name = "Nitty Gritty",
        description = "Industrial-style tavern with varied burgers, sandwiches & other American eats, plus many beers.",
        waitTimeMinutes = 15,
        type = "American",
        latLng = LatLng(43.07180, -89.39573),
        color = Color.Unspecified,
        // not sure how feasible images will be, but adding as hardcoded for demo reasons for now
        imageUrl = "https://lh3.googleusercontent.com/gps-cs-s/AG0ilSz4s1VyHH-wEZXwKU-kuXYi_LYWEwBpQoWSuGi2zAgIa0Y3vyrfgYkBnx9YAc7JQVZC9m3jdaw-v3kgrvJIhJXXmETtg-35DsRD2NMX-54LwKrKdCbxz4VXa5DgLzAsi1YY0kMi=w408-h302-k-no"
    ),
    Restaurant(
        name = "Dotty Dumpling's Dowry",
        description = "Longtime local institution offering specialty burgers, cheese curds & tap beers in quirky digs.",
        waitTimeMinutes = 20,
        type = "American",
        latLng = LatLng(43.07293, -89.39567),
        color = Color.Unspecified,
        imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQv6wIZMYeKIzxR_voy8He4Nia0chJb9f0zAA&s"
    ),
    Restaurant(
        name = "Old Fashioned",
        description = "Wisconsin-themed, retro-style tavern offering beers, brats & cheese curds (all sourced in-state).",
        waitTimeMinutes = 35,
        type = "American",
        latLng = LatLng(43.07650, -89.38377),
        color = Color.Unspecified,
        imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_av8AxiIRLSdAIqurGwhEm32nv2KsgJQqhQ&s"
    ),
    Restaurant(
        name = "Great Dane Pub",
        description = "Craft brews & pub eats served in a lively venue with beer garden & pool tables.",
        waitTimeMinutes = 10,
        type = "Pub/Bar",
        latLng = LatLng(43.07459, -89.38026),
        color = Color.Unspecified,
        imageUrl = "https://i0.wp.com/absolutebeer.com/wp-content/uploads/2020/05/AB-Breweries-Great-Dane-Pub-Brewing-Company-Locations-Downtown-Exterior-1-Banner.jpg?fit=2400%2C1200&ssl=1"
    ),
    Restaurant(
        name = "Lucille",
        description = "Vintage, industrial-chic tavern with 3 floors serving wood-fired & steel-pan pizzas, plus cocktails.",
        waitTimeMinutes = 15,
        type = "Italian",
        latLng = LatLng(43.07443434093093, -89.38136875043087),
        color = Color.Unspecified,
        imageUrl = "https://static.wixstatic.com/media/50d0a5_ff180554c80a41e1bfa7a708bc19050d~mv2.jpg/v1/fill/w_1960,h_996,al_c/50d0a5_ff180554c80a41e1bfa7a708bc19050d~mv2.jpg"
    ),
    Restaurant(
        name = "A La Brasa",
        description = "Mole poblano, fajitas & other Mexican favorites doled out in a simple, brightly colored setting.",
        waitTimeMinutes = 20,
        type = "Mexican",
        latLng = LatLng(43.072012515791755, -89.3891851944116),
        color = Color.Unspecified,
        imageUrl = "https://static.where-e.com/United_States/A-La-Brasa-Mexican-Madison_b0eec6247d94c5881ebe4fecc529c514.jpg"
    ),
    Restaurant(
        name = "Canteen",
        description = "Casual Mexican grill for tacos & classic plates including brunch, with late hours & many tequilas.",
        waitTimeMinutes = 20,
        type = "Mexican",
        latLng = LatLng(43.072733364709585, -89.38403711683983),
        color = Color.Unspecified,
        imageUrl = "https://www.ourchanginglives.com/wp-content/uploads/2022/07/Canteen_5-2-1024x736-1.jpg"
    ),
    Restaurant(
        name = "Marigold Kitchen",
        description = "Bright, counter-serve eatery featuring creative breakfast & lunch fare plus outdoor seating.",
        waitTimeMinutes = 30,
        type = "Cafe",
        latLng = LatLng(43.074137489923885, -89.38150994070068),
        color = Color.Unspecified,
        imageUrl = "https://assets.simpleviewinc.com/simpleview/image/upload/crm/madison/DC6AD9EA-F81C-4539-9E36-51BEB9D952A9_C8E61E34-E5C4-44B3-8253722EC0FF0A36_9655e195-6638-410f-bdc2e943db75af00.jpg"
    ),
    Restaurant(
        name = "Ancora Cafe",
        description = "Charming cafe & bakery with house-made goods, creative egg sandwiches & brunch, plus craft coffee.",
        waitTimeMinutes = 30,
        type = "Cafe",
        latLng = LatLng(43.07456150831911, -89.38120399359781),
        color = Color.Unspecified,
        imageUrl = "https://photos.bringfido.com/restaurants/8/1/0/99018/99018_18728804.jpg?size=slide&density=2x"
    ),
)
    .mapIndexed { index, restaurant ->
        restaurant.copy(color = restaurantColors[index % restaurantColors.size])
    }
