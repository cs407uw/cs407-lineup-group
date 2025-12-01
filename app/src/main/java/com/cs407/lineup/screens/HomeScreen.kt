package com.cs407.lineup.screens

import NearbySearchRepository
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.lineup.R
import com.cs407.lineup.data.Restaurant
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import androidx.compose.ui.platform.LocalContext
import androidx.glance.appwidget.updateAll
import com.cs407.lineup.BuildConfig
import com.cs407.lineup.data.LocationViewModel
import com.cs407.lineup.data.RestaurantPrefs
import com.cs407.lineup.data.ProfilePrefs
import com.cs407.lineup.ui.RestaurantListSheet
import com.cs407.lineup.ui.RestaurantMap
import com.cs407.lineup.widget.LastRestaurantWidget
import kotlinx.coroutines.GlobalScope

val ubuntu = FontFamily(Font(R.font.ubuntu))
val monaspace = FontFamily(Font(R.font.monaspace_neon))

/**
 * the home screen of the app that includes a google map, user location marker,
 * restaurant markers, bottom sheet w/ list of nearby fetched restaurants, and profile card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier, onRestaurantClick: (Restaurant) -> Unit
) {
    val context = LocalContext.current

    // variables for bottom sheet scope & state
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showSheet by remember { mutableStateOf(true) }

    // variables for profile card and user profile fields
    var showProfileCard by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var home by remember { mutableStateOf("") }
    var work by remember { mutableStateOf("") }
    var favoriteCategories by remember { mutableStateOf<Set<String>>(emptySet()) }

    // current selected restaurant
    var selected by remember { mutableStateOf<Restaurant?>(null) }

    // variables for map camera, initial position, and user GPS
    val initial = selected?.latLng ?: LatLng(43.0731, -89.4012) // default Madison
    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initial, 13f)
    }
    val locationViewModel: LocationViewModel = viewModel()
    val userLocation by locationViewModel.location.collectAsState()

    // variable for fetching nearby restaurants and restaurants within user's map frame, respectively:
    var nearbyRestaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) }
    var sortedRestaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) }

    // sorting option state
    var sortOption by remember { mutableStateOf("Distance") }

    fun distanceMeters(a: LatLng, b: LatLng): Double {
        val arr = FloatArray(1)
        android.location.Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, arr)
        return arr[0].toDouble()
    }

    // permissions launcher that requests fine and coarse location
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            locationViewModel.startLocationUpdates()
        }
    }

    // when the user location changes, fetch nearby restaurants and store results in a state to update ui
    LaunchedEffect(userLocation) {
        if (userLocation != null) {
            val repo = NearbySearchRepository()

            val results = repo.getNearbyRestaurants(
                lat = userLocation!!.latitude,
                lng = userLocation!!.longitude,
                apiKey = BuildConfig.MAPS_API_KEY
            )

            nearbyRestaurants = results
        }
    }

    // if the camera pos changes, update the sorted restaurants list to include the restaurants in view
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            // get bounds to determine if in view or out of view
            val projection = cameraPositionState.projection ?: return@LaunchedEffect
            val visibleRegion = projection.visibleRegion
            val bounds = visibleRegion.latLngBounds

            val inView = nearbyRestaurants.filter { bounds.contains(it.latLng) }
            val outOfView = nearbyRestaurants.filter { !bounds.contains(it.latLng) }

            sortedRestaurants = inView + outOfView
        }
    }

    // map animation that gets triggered whenever use location updates
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it, 15f), durationMs = 600
            )
        }
    }

    // map animation when user selects a restaurant (also used for updating homescreen widget)
    LaunchedEffect(selected) {
        selected?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it.latLng, 15f), durationMs = 600
            )
            GlobalScope.launch {
                LastRestaurantWidget().updateAll(context)
            }
        }
    }

    // ask for location permissions as soon as the screen is visible
    LaunchedEffect(true) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // load saved profile data when screen starts
    LaunchedEffect(Unit) {
        val savedProfile = ProfilePrefs.loadProfile(context)
        name = savedProfile.name
        home = savedProfile.home
        work = savedProfile.work
        favoriteCategories = savedProfile.favoriteCategories
    }

    // launched effect triggered when user changes sort option, moves map, or
    // restaurants update. updates to the properly sorted restaurants list
    LaunchedEffect(sortOption, nearbyRestaurants, userLocation) {
        if (nearbyRestaurants.isEmpty()) return@LaunchedEffect
        // sort restaurants based on selected option
        val sorted = when (sortOption) {
            "Wait Time" -> nearbyRestaurants.sortedBy { it.waitTimeMinutes }
            "Rating" -> nearbyRestaurants.sortedByDescending { it.rating ?: 0.0 }
            "Price" -> nearbyRestaurants.sortedBy { it.priceLevel ?: Int.MAX_VALUE }
            "Distance" -> {
                if (userLocation != null) {
                    nearbyRestaurants.sortedBy {
                        distanceMeters(userLocation!!, it.latLng)
                    }
                } else nearbyRestaurants
            }
            else -> nearbyRestaurants
        }
        sortedRestaurants = sorted
    }

    // reorder restaurant lists so restaurants currently visible appear first in the list
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val projection = cameraPositionState.projection ?: return@LaunchedEffect
            val bounds = projection.visibleRegion.latLngBounds

            val inView = sortedRestaurants.filter { bounds.contains(it.latLng) }
            val outView = sortedRestaurants.filter { !bounds.contains(it.latLng) }

            sortedRestaurants = inView + outView
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        RestaurantMap(
            showSheet = showSheet,
            cameraPositionState = cameraPositionState,
            userLocation = userLocation,
            nearbyRestaurants = nearbyRestaurants,
            selected = selected)


        // profile button
        Box(
            modifier = Modifier
                .padding(top = 40.dp, end = 20.dp)
                .align(Alignment.TopEnd)
        ) {
            IconButton(
                onClick = { showProfileCard = !showProfileCard },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, shape = CircleShape)
                    .border(2.dp, Color.Black, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // profile card overlay (when profile button is clicked to open)
        if (showProfileCard) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    // close profile settings when clicked outside of the menu
                    .clickable { showProfileCard = false }, contentAlignment = Alignment.Center
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(0.9f)
                        .clickable(enabled = false) {}) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // profile title and fields
                        Text(
                            stringResource(R.string.profile_preferences),
                            fontFamily = monaspace,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.name)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AccountCircle, contentDescription = null
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1B5E20),
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = Color(0xFF1B5E20),
                                cursorColor = Color(0xFF1B5E20)
                            )
                        )

                        OutlinedTextField(
                            value = home,
                            onValueChange = { home = it },
                            label = { Text(stringResource(R.string.home)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1B5E20),
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = Color(0xFF1B5E20),
                                cursorColor = Color(0xFF1B5E20)
                            )
                        )

                        OutlinedTextField(
                            value = work,
                            onValueChange = { work = it },
                            label = { Text(stringResource(R.string.work)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1B5E20),
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = Color(0xFF1B5E20),
                                cursorColor = Color(0xFF1B5E20)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // favorite categories section
                        Text(
                            "Favorite Categories",
                            fontFamily = monaspace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // category checkboxes
                        val availableCategories = listOf("Restaurant", "Bar", "Cafe", "Grocery")
                        availableCategories.forEach { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        favoriteCategories = if (favoriteCategories.contains(category)) {
                                            favoriteCategories - category
                                        } else {
                                            favoriteCategories + category
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = favoriteCategories.contains(category),
                                    onCheckedChange = { checked ->
                                        favoriteCategories = if (checked) {
                                            favoriteCategories + category
                                        } else {
                                            favoriteCategories - category
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF1B5E20),
                                        uncheckedColor = Color.Gray
                                    )
                                )
                                Text(
                                    text = category,
                                    fontFamily = ubuntu,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        // buttons to save and close the profile card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    ProfilePrefs.saveProfile(context, name, home, work, favoriteCategories)
                                    showProfileCard = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save", color = Color.White)
                            }
                            OutlinedButton(
                                onClick = { showProfileCard = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF1B5E20)
                                )
                            ) {
                                Text(stringResource(R.string.close_button))
                            }
                        }
                    }
                }
            }
        }

        // bottom sheet that displays all nearby restaurant, including a dropdown selector and filtered restaurant list
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,

                // drag handle (small gray bar at top of sheet)
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.LightGray)
                        )
                    }
                }) {

                // the actual list within the bottom sheet; includes the restaurants w/ filtering applied
                RestaurantListSheet(
                    restaurants = sortedRestaurants.ifEmpty { nearbyRestaurants },
                    sortOption = sortOption,
                    onSortChange = { sortOption = it },
                    onItemClick = { r ->
                        selected = r
                        RestaurantPrefs.saveRestaurant(
                            context,
                            r.name,
                            r.waitTimeMinutes,
                            r.color.value.toInt()
                        )
                        GlobalScope.launch {
                            LastRestaurantWidget().updateAll(context)
                        }
                        onRestaurantClick(r)
                    },
                    onCategoryChangeFirst = { first -> first?.let { selected = it } },
                    onClose = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showSheet = false
                        }
                    },
                    favoriteCategories = favoriteCategories
                )

            }
        } else {
            // when the sheet is closed, show a small draggable bar at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.White, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .clickable {
                        showSheet = true
                        // reopen list of restaurants in bottom sheet when clicked
                        scope.launch { sheetState.show() }
                    }, contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.Gray)
                )
            }
        }
    }
}
