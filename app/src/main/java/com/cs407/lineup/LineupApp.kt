package com.cs407.lineup

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.lineup.data.HardcodedRestaurants
import com.cs407.lineup.screens.MapScreen
import com.cs407.lineup.screens.RestaurantDetailScreen

@Composable
fun LineupApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "map") {
        composable("map") {
            MapScreen(
                onRestaurantClick = { restaurant ->
                    navController.navigate("restaurantDetail/${restaurant.name}")
                }
            )
        }

        composable("restaurantDetail/{name}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name")
            val restaurant = HardcodedRestaurants.find { it.name == name }
            restaurant?.let {
                RestaurantDetailScreen(
                    restaurant = it,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
