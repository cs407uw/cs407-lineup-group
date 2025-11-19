package com.cs407.lineup.screens

import android.R.attr.fontWeight
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.lineup.data.Restaurant
import coil.compose.AsyncImage
import com.cs407.lineup.data.LocationViewModel
import com.cs407.lineup.data.RestaurantPrefs

@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant,
    onBack: () -> Unit,
    locationViewModel: LocationViewModel = viewModel()
) {
    val userLocation by locationViewModel.location.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(true) {
        locationViewModel.startLocationUpdates()
    }

    val safeImage = restaurant.imageUrl.ifBlank {
        "https://via.placeholder.com/400x300?text=No+Image"
    }
    val safeDescription = restaurant.description.ifBlank { "No description available." }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(restaurant.color.copy(alpha = 0.4f))
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = restaurant.name,
            fontFamily = monaspace,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .background(Color.White.copy(alpha = 0.8f))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "${restaurant.waitTimeMinutes} MIN WAIT",
                fontFamily = monaspace,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        AsyncImage(
            model = safeImage,
            contentDescription = "Restaurant Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = safeDescription,
            fontFamily = ubuntu,
            fontSize = 16.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                if (userLocation != null) {
                    val uri = Uri.parse(
                        "https://www.google.com/maps/dir/?api=1" +
                                "&origin=${userLocation!!.latitude},${userLocation!!.longitude}" +
                                "&destination=${restaurant.latLng.latitude},${restaurant.latLng.longitude}" +
                                "&travelmode=walking"
                    )
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                } else {
                    Toast.makeText(context, "Finding your location…", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("NAVIGATE", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Back", color = Color.Gray)
        }
    }
}
