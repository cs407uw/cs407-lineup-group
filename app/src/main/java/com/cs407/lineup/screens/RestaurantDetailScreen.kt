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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.lineup.data.Restaurant
import coil.compose.AsyncImage
import com.cs407.lineup.data.LocationViewModel
import com.cs407.lineup.data.WaitTimeRepository
import com.cs407.lineup.data.FirebaseRepository
import com.cs407.lineup.data.WaitTimeData
import com.cs407.lineup.data.HomeViewModel
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant, onBack: () -> Unit, locationViewModel: LocationViewModel = viewModel()
) {
    val context = LocalContext.current

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

                    // Capture Line button
                    CaptureLineButton(restaurant = restaurant)

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

/**
 * Composable button to capture an image of the line and upload to Gemini API
 * for wait time estimation, with manual entry option
 */
@Composable
fun CaptureLineButton(restaurant: Restaurant) {
    val context = LocalContext.current
    val waitTimeRepository = remember { WaitTimeRepository() }
    val firebaseRepository = remember { FirebaseRepository() }
    val homeViewModel: HomeViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    // State for loading and result
    var isUploading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    // State for manual entry dialog
    var showManualEntryDialog by remember { mutableStateOf(false) }
    var manualWaitTime by remember { mutableStateOf("") }

    // File to store the captured image
    val imageFile = remember {
        File(context.cacheDir, "captured_line_${System.currentTimeMillis()}.jpg")
    }

    // URI for the image file
    val imageUri = remember(imageFile) {
        androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    // Shared function to process image upload
    fun processImageUpload() {
        isUploading = true
        resultMessage = null

        coroutineScope.launch {
            val apiKey = com.cs407.lineup.data.ApiKeyProvider.geminiApiKey
            val result = waitTimeRepository.uploadImage(imageFile, apiKey)

            isUploading = false

            if (result.isSuccess) {
                val waitMinutes = result.waitTimeMinutes!!
                val calculator = com.cs407.lineup.data.WaitTimeCalculator()
                resultMessage = "Wait Time: ${calculator.formatWaitTime(
                    waitMinutes,
                    result.confidence!!
                )}"

                // Save AI estimate to Firebase
                val saved = firebaseRepository.saveWaitTime(
                    venueId = restaurant.id,
                    venueName = restaurant.name,
                    waitMinutes = waitMinutes,
                    source = WaitTimeData.SOURCE_AI,
                    rawAiEstimate = waitMinutes
                )
                if (saved) {
                    homeViewModel.markNeedsRefresh()
                    Toast.makeText(context, "Wait time saved to database", Toast.LENGTH_SHORT).show()
                }
            } else {
                resultMessage = result.errorMessage ?: "Unknown error"
            }
        }
    }

    // Camera launcher
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            processImageUpload()
        } else {
            Toast.makeText(context, "Image capture cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    // Image picker launcher (for gallery)
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                // Copy the selected image to our cache file
                context.contentResolver.openInputStream(uri)?.use { input ->
                    imageFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                processImageUpload()
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permission launcher
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(imageUri)
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    // Manual Entry Dialog
    if (showManualEntryDialog) {
        AlertDialog(
            onDismissRequest = { showManualEntryDialog = false },
            title = {
                Text(
                    "Enter Wait Time",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "How many minutes is the current wait?",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = manualWaitTime,
                        onValueChange = {
                            // Only allow numbers
                            if (it.all { char -> char.isDigit() }) {
                                manualWaitTime = it
                            }
                        },
                        label = { Text("Minutes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val minutes = manualWaitTime.toIntOrNull()
                        if (minutes != null && minutes >= 0) {
                            // Save to Firebase
                            coroutineScope.launch {
                                val saved = firebaseRepository.saveWaitTime(
                                    venueId = restaurant.id,
                                    venueName = restaurant.name,
                                    waitMinutes = minutes,
                                    source = WaitTimeData.SOURCE_MANUAL
                                )
                                if (saved) {
                                    homeViewModel.markNeedsRefresh()
                                    resultMessage = "Wait Time: $minutes min (Manual Entry) ✓ Saved"
                                    Toast.makeText(context, "Wait time saved to database!", Toast.LENGTH_SHORT).show()
                                } else {
                                    resultMessage = "Wait Time: $minutes min (Manual Entry) - Save failed"
                                    Toast.makeText(context, "Failed to save wait time", Toast.LENGTH_SHORT).show()
                                }
                            }
                            showManualEntryDialog = false
                            manualWaitTime = ""
                        } else {
                            Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showManualEntryDialog = false
                    manualWaitTime = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Row with Camera button (75%) and Gallery icon (25%)
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Camera button - takes most of the space
            Button(
                onClick = {
                    when {
                        androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.CAMERA
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                            cameraLauncher.launch(imageUri)
                        }
                        else -> {
                            permissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    }
                },
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                modifier = Modifier
                    .weight(0.75f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = if (isUploading) "ANALYZING..." else "CAPTURE LINE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Gallery icon button
            IconButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                enabled = !isUploading,
                modifier = Modifier
                    .weight(0.25f)
                    .height(50.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(0xFF2196F3).copy(alpha = 0.15f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Choose from gallery",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Manual entry button
        Button(
            onClick = { showManualEntryDialog = true },
            enabled = !isUploading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            ),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            shape = RoundedCornerShape(25.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ENTER WAIT TIME",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        // Display result message
        resultMessage?.let { message ->
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .background(
                        color = if (message.startsWith("Wait Time"))
                            Color(0xFF1B5E20).copy(alpha = 0.1f)
                        else Color.Red.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = message,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (message.startsWith("Wait Time")) Color(0xFF1B5E20) else Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
