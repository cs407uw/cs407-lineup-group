package com.cs407.lineup

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.lineup.data.HardcodedRestaurants
import com.cs407.lineup.data.Restaurant
import com.cs407.lineup.screens.MapScreen
import com.cs407.lineup.screens.RestaurantDetailScreen
import com.google.gson.Gson

@Composable
fun LineupApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "map") {
        composable("map") {
            MapScreen(
                onRestaurantClick = { restaurant ->
                    val json = Uri.encode(Gson().toJson(restaurant))
                    navController.navigate("restaurantDetail/$json")
                }

            )
        }

        composable("restaurantDetail/{restaurantJson}") { backStackEntry ->
            val json = backStackEntry.arguments?.getString("restaurantJson")
            val restaurant = Gson().fromJson(json, Restaurant::class.java)

            RestaurantDetailScreen(
                restaurant = restaurant,
                onBack = { navController.popBackStack() },
                locationViewModel = viewModel()
            )
        }

    }
}
