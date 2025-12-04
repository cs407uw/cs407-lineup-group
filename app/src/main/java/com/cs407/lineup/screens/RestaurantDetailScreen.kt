package com.cs407.lineup.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.lineup.data.FavoritePrefs
import com.cs407.lineup.data.Restaurant
import coil.compose.AsyncImage
import com.cs407.lineup.data.LocationViewModel

@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant, onBack: () -> Unit, locationViewModel: LocationViewModel = viewModel()
) {
    val context = LocalContext.current

    // favorite state for toggle button
    var isFavorite by remember { mutableStateOf(FavoritePrefs.isFavorite(context, restaurant.id)) }

    // location state for navigation
    val userLocation by locationViewModel.location.collectAsState()

    // request location updates right upon screen load
    LaunchedEffect(true) {
        locationViewModel.startLocationUpdates()
    }

    // fallback image & descrip. in case the api doesn't properly return one for a restaurant
    val safeImage = restaurant.imageUrl.ifBlank {
        "https://via.placeholder.com/400x300?text=No+Image"
    }
    val safeDescription = restaurant.description.ifBlank { "No description available." }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(restaurant.color)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // card to hold restaurant content
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // restaurant name
                    Text(
                        text = restaurant.name,
                        fontFamily = monaspace,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // wait time
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEEEDE9), RoundedCornerShape(10.dp))
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${restaurant.waitTimeMinutes} MIN WAIT",
                            fontFamily = monaspace,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // restaurant metadata including rating, price, and open status (we can add a few more things i think)
                    RestaurantMetaInfo(restaurant)

                    Spacer(modifier = Modifier.height(16.dp))

                    // main restaurant image (use fallback if needed)
                    AsyncImage(
                        model = safeImage,
                        contentDescription = "Restaurant Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // description w/ fallback
                    Text(
                        text = safeDescription,
                        fontFamily = ubuntu,
                        fontSize = 17.sp,
                        color = Color.Black,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // external maps navigation button (to android maps)
                    Button(
                        onClick = {
                            if (userLocation != null) {
                                // build the maps walking directions url (we can change this to drive, but since
                                // we are catering to students, kept it as walking)
                                val uri = Uri.parse(
                                    "https://www.google.com/maps/dir/?api=1" + "&origin=${userLocation!!.latitude},${userLocation!!.longitude}" + "&destination=${restaurant.latLng.latitude},${restaurant.latLng.longitude}" + "&travelmode=walking"
                                )
                                // launch the maps in android with a toast if the user's location is not available yet
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            } else {
                                Toast.makeText(
                                    context, "Finding your location…", Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("NAVIGATE", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // favorite toggle button
                    OutlinedButton(
                        onClick = {
                            isFavorite = if (isFavorite) {
                                FavoritePrefs.removeFavorite(context, restaurant.id)
                                false
                            } else {
                                FavoritePrefs.addFavorite(context, restaurant.id)
                                // Save as last viewed favorite for widget
                                FavoritePrefs.saveLastViewedFavorite(
                                    context,
                                    restaurant.name,
                                    restaurant.waitTimeMinutes,
                                    restaurant.color.value.toInt()
                                )
                                true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isFavorite) Color(0xFFFFF8DC) else Color.Transparent,
                            contentColor = Color(0xFF1B5E20)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color(0xFFFFD700) else Color(0xFF1B5E20)
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = if (isFavorite) "REMOVE FROM FAVORITES" else "ADD TO FAVORITES",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // back button
                    TextButton(onClick = onBack) {
                        Text("Back", color = Color.Gray, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

/**
 * function to build the single row of restaurant metadata
 */
@Composable
fun RestaurantMetaInfo(restaurant: Restaurant) {
    // metadata fields
    val rating = restaurant.rating
    val ratingCount = restaurant.ratingCount
    val priceLevel = restaurant.priceLevel
    val isOpen = restaurant.isOpenNow

    // separator for visual aesthetics
    val separator = "  •  "

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        rating?.let {
            // star emojis are unicode, but maybe better practice to change this
            Text(
                text = "⭐ $rating",
                fontFamily = ubuntu,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
        }

        // restaurant reviews with counts
        ratingCount?.let {
            Text(
                text = "  ($ratingCount reviews)",
                fontFamily = ubuntu,
                fontSize = 15.sp,
                color = Color.Gray
            )
        }

        if (isOpen != null) {
            Text(separator, color = Color.DarkGray)
        }

        // open status (open / closed)
        isOpen?.let {
            Text(
                text = if (it) "Open Now" else "Closed",
                fontFamily = ubuntu,
                fontSize = 15.sp,
                color = if (it) Color(0xFF1B5E20) else Color.Red
            )
        }

        if (priceLevel != null) {
            Text(separator, color = Color.DarkGray)
        }

        // price level ($, $$, or $$$)
        priceLevel?.let {
            Text(
                text = "$".repeat(priceLevel),
                fontFamily = ubuntu,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}
