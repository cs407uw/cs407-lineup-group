package com.cs407.lineup

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.lineup.data.Restaurant
import com.cs407.lineup.screens.HomeScreen
import com.cs407.lineup.screens.RestaurantDetailScreen
import com.google.gson.Gson

/**
 * Root composable of the lineup app that includes nav controller and app routing
 */
@Composable
fun LineupApp() {
    val navController = rememberNavController()

    // main navigation graph
    NavHost(navController = navController, startDestination = "home") {
        //  home screen route; when a restaurant is clicked, we conert the restaurant object into json using
        // gson, then uri encode it so it can be passed in the route, and lastly navigate to the detail screen
        // route with that json attached.
        composable("home") {
            HomeScreen(
                onRestaurantClick = { restaurant ->
                    val json = Uri.encode(Gson().toJson(restaurant))
                    navController.navigate("restaurantDetail/$json")
                }
            )
        }

        // restaurant details screen route to extract the json string from nav arguments, decode it back to the restaurant object,
        // and pass it into restaurant details screen.
        composable("restaurantDetail/{restaurantJson}") { backStackEntry ->
            // read the json argument from the nav route
            val json = backStackEntry.arguments?.getString("restaurantJson")
           // convert json to restaurant object (using gson)
            val restaurant = Gson().fromJson(json, Restaurant::class.java)

            // render the details screen
            RestaurantDetailScreen(
                restaurant = restaurant,
                onBack = { navController.popBackStack() },
                locationViewModel = viewModel()
            )
        }

    }
}
