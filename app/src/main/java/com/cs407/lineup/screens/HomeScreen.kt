package com.cs407.lineup.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.lineup.R
import com.cs407.lineup.data.FavoritePrefs
import com.cs407.lineup.data.HomeViewModel
import com.cs407.lineup.data.LocationHelper
import com.cs407.lineup.data.LocationViewModel
import com.cs407.lineup.data.ProfilePrefs
import com.cs407.lineup.data.Restaurant
import com.cs407.lineup.data.RestaurantPrefs
import com.cs407.lineup.widget.LastRestaurantWidget
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

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
    val activity = context as android.app.Activity
    val locationViewModel: LocationViewModel = viewModel()
    val gpsLocation by locationViewModel.location.collectAsState()

    // activity-scoped so its shared with detail screen
    val homeViewModel: HomeViewModel = viewModel(viewModelStoreOwner = activity as androidx.lifecycle.ViewModelStoreOwner)
    val nearbyRestaurants by homeViewModel.nearbyRestaurants.collectAsState()
    val isLoadingRestaurants by homeViewModel.isLoading.collectAsState()
    val needsRefresh by homeViewModel.needsRefresh.collectAsState()

    var sortedRestaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) }

    val locationHelper = remember { LocationHelper(context) }
    val isManualMode by locationHelper.isManualMode.collectAsState()
    val manualLocation by locationHelper.manualLocation.collectAsState()

    // manual location overrides gps
    val userLocation = remember(gpsLocation, manualLocation, isManualMode) {
        locationHelper.getEffectiveLocation(gpsLocation)
    }

    var sortOption by remember { mutableStateOf("Distance") }

    fun distanceMeters(a: LatLng, b: LatLng): Double {
        val arr = FloatArray(1)
        android.location.Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, arr)
        return arr[0].toDouble()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            hasLocationPermission = true
            locationViewModel.startLocationUpdates()
        }
    }

    // fetch restaurants when location changes
    LaunchedEffect(userLocation) {
        if (userLocation != null) {
            homeViewModel.fetchNearbyRestaurants(userLocation)
        }
    }

    // refresh after wait time submitted
    LaunchedEffect(needsRefresh, userLocation) {
        if (needsRefresh && userLocation != null) {
            homeViewModel.fetchNearbyRestaurants(userLocation, forceRefresh = true)
            homeViewModel.clearNeedsRefresh()
        }
    }

    // animate map when location updates
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it, 15f), durationMs = 600
            )
        }
    }

    // request location permissions
    LaunchedEffect(true) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
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

    LaunchedEffect(sortOption, nearbyRestaurants, userLocation, cameraPositionState.isMoving) {
        val currentLocation = userLocation  // Smart cast helper
        val baseSorted =
            when (sortOption) {
                "Wait Time" -> nearbyRestaurants.sortedBy { it.waitTimeMinutes ?: Int.MAX_VALUE }
                "Rating" -> nearbyRestaurants.sortedByDescending { it.rating ?: 0.0 }
                "Price" -> nearbyRestaurants.sortedBy { it.priceLevel ?: Int.MAX_VALUE }
                "Distance" ->
                    if (currentLocation != null)
                        nearbyRestaurants.sortedBy { distanceMeters(currentLocation, it.latLng) }
                    else nearbyRestaurants
                else -> nearbyRestaurants
            }

        val projection = cameraPositionState.projection
        val finalSorted =
            if (!cameraPositionState.isMoving && projection != null) {
                val bounds = projection.visibleRegion.latLngBounds
                val inView = baseSorted.filter { bounds.contains(it.latLng) }
                val outView = baseSorted.filter { !bounds.contains(it.latLng) }
                inView + outView
            } else baseSorted

        sortedRestaurants = finalSorted
    }


    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (hasLocationPermission) {
            // main google map UI; bottom padding shrinks the map when the sheet is open
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (showSheet) 220.dp else 0.dp),
                cameraPositionState = cameraPositionState
            ) {
                // create marker for user's current location (blue)
                userLocation?.let { loc ->
                    Marker(
                        state = MarkerState(position = loc),
                        title = "You",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                }
                // draw all nearby restaurants w/ red marker
                nearbyRestaurants.forEach { restaurant ->
                    Marker(
                        state = MarkerState(position = restaurant.latLng),
                        title = restaurant.name,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }
                // highlight marker for the currently selected restaurant
                selected?.let { restaurant ->
                    Marker(
                        state = MarkerState(position = restaurant.latLng), title = restaurant.name
                    )
                }
            }
        } else {
            // Show a message or a loading indicator while waiting for permission
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Requesting location permission...")
            }
        }

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
                    .background(Color.Black.copy(alpha = 0.6f))// close profile settings when clicked outside of the menu
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

                        Spacer(modifier = Modifier.height(4.dp))

                        //Manual Location Icons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            //Home Icon for Manual location
                            OutlinedTextField(
                                value = home,
                                onValueChange = { home = it },
                                label = { Text(stringResource(R.string.home)) },
                                modifier = Modifier
                                    .weight(1f)
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

                            // Icon button to set location to home
                            IconButton(
                                onClick = {
                                    if (home.isNotBlank()) {
                                        locationHelper.setManualLocation(home, scope)
                                        showProfileCard = false
                                    }
                                },
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(40.dp)
                                    .background(Color(0xFF1B5E20), shape = CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = "Go to Home",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            //Work address for manual
                            OutlinedTextField(
                                value = work,
                                onValueChange = { work = it },
                                label = { Text(stringResource(R.string.work)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF1B5E20),
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedLabelColor = Color(0xFF1B5E20),
                                    cursorColor = Color(0xFF1B5E20)
                                )
                            )
                            IconButton(
                                onClick = {
                                    if (work.isNotBlank()) {
                                        locationHelper.setManualLocation(work, scope)
                                        showProfileCard = false
                                    }
                                },
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(40.dp)
                                    .background(Color(0xFF1B5E20), shape = CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Go to Work",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        if (isManualMode) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    locationHelper.useCurrentLocation()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E7D32)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Use Current Location", color = Color.White, fontFamily = ubuntu)
                            }
                        }

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
                                        favoriteCategories =
                                            if (favoriteCategories.contains(category)) {
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
                                    ProfilePrefs.saveProfile(
                                        context,
                                        name,
                                        home,
                                        work,
                                        favoriteCategories
                                    )
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
                            r.waitTimeMinutes ?: 0,
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

/**
 * bottom sheet displaying the list of restaurants with category filtering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantListSheet(
    restaurants: List<Restaurant>,
    sortOption: String,
    onSortChange: (String) -> Unit,
    onItemClick: (Restaurant) -> Unit,
    onCategoryChangeFirst: (Restaurant?) -> Unit,
    onClose: () -> Unit,
    favoriteCategories: Set<String> = emptySet()
) {
    // get context for accessing SharedPreferences
    val context = LocalContext.current

    // state for opening filter modal w/ categories & sorting
    var showFilterSheet by remember { mutableStateOf(false) }

    // search query state
    var searchQuery by remember { mutableStateOf("") }

    // favorites toggle state
    var showOnlyFavorites by remember { mutableStateOf(false) }

    // state to track favorited restaurant IDs (triggers recomposition when changed)
    var favoriteIds by remember { mutableStateOf(FavoritePrefs.getFavorites(context)) }

    // label for dropdown & all the categories
    val allLabel = "All Establishments"

    // if user has favorites, use them initially, otherwise show all
    var selectedCategories by remember {
        mutableStateOf(
            if (favoriteCategories.isEmpty()) setOf(allLabel)
            else favoriteCategories
        )
    }


    // all category options to display and choose from in modal
    val categories = listOf(allLabel, "Restaurant", "Bar", "Cafe", "Grocery")
    // filtering logic: apply the selected categories
    val filtered by remember(selectedCategories, restaurants) {
        derivedStateOf {
            if (selectedCategories.contains(allLabel)){
                restaurants
            } else {
                restaurants.filter { r ->
                    r.type in selectedCategories
                }
            }
        }
    }

    // search filtering: apply search query on top of category filtering
    val searchFiltered by remember(searchQuery, filtered) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                filtered
            } else {
                filtered.filter { r ->
                    r.name.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    // favorites filtering: apply favorites filter on top of search and category filtering
    val favoritesFiltered by remember(showOnlyFavorites, searchFiltered, favoriteIds) {
        derivedStateOf {
            if (showOnlyFavorites) {
                searchFiltered.filter { r ->
                    favoriteIds.contains(r.id)
                }
            } else {
                searchFiltered
            }
        }
    }

    // when selected category is changed, refilter and update first restaurant to be highlighted on map
    LaunchedEffect(selectedCategories) {
        onCategoryChangeFirst(filtered.firstOrNull())
    }

    Column {
        // search bar for filtering by name
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search...", fontFamily = ubuntu) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF1B5E20)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = Color.Gray
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1B5E20),
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = Color(0xFF1B5E20),
                cursorColor = Color(0xFF1B5E20)
            ),
            singleLine = true
        )

        // favorites toggle switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { showOnlyFavorites = !showOnlyFavorites },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Favorites",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Show only favorites",
                    fontFamily = ubuntu,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
            Switch(
                checked = showOnlyFavorites,
                onCheckedChange = { showOnlyFavorites = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF1B5E20),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }

        // filter button that opens the modal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF1B5E20))
                .clickable { showFilterSheet = true },
            contentAlignment = Alignment.Center
        ) {
            // show the currently selected category text in the dropdown menu title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp)
            ) {
                Text(
                    text = "Filters",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = monaspace,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        // filter modal bottom sheet (contains sorting section, category selection, and apply button)
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                containerColor = Color.White,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Sort By",
                        fontFamily = monaspace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    val sortOptions = listOf("Distance", "Wait Time", "Rating", "Price")

                    sortOptions.forEach { option ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onSortChange(option) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (sortOption == option),
                                onClick = { onSortChange(option) }
                            )
                            Text(
                                text = option,
                                fontFamily = ubuntu,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "Categories",
                        fontFamily = monaspace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    categories.forEach { cat ->

                        val isSelected = selectedCategories.contains(cat)

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedCategories =
                                        if (cat == allLabel) { // reset all filters
                                            emptySet()
                                        } else if (isSelected) { // unselect the specific category
                                            selectedCategories - cat
                                        } else {
                                            (selectedCategories + cat) - allLabel
                                        }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    selectedCategories =
                                        if (checked) {
                                            if (cat == allLabel) setOf(allLabel)
                                            else (selectedCategories + cat) - allLabel

                                        } else {
                                            selectedCategories - cat
                                        }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF1B5E20),
                                    uncheckedColor = Color.Gray
                                )
                            )

                            Text(
                                text = cat,
                                fontFamily = ubuntu,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // apply button to apply filters (sorting happens in HomeScreen)
                    Button(
                        onClick = {
                            onCategoryChangeFirst(favoritesFiltered.firstOrNull())
                            showFilterSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1B5E20)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply", color = Color.White, fontFamily = monaspace)
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }

        // if restaurants haven't loaded yet, show loading spinner
        if (restaurants.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF1B5E20))
            }
            return@Column
        }
        // list of restaurants (once loaded)
        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            itemsIndexed(favoritesFiltered) { index, restaurant ->
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                RestaurantListItem(
                    restaurant = restaurant,
                    index = index,
                    onClick = { onItemClick(restaurant) },
                    onFavoriteToggle = {
                        if (favoriteIds.contains(restaurant.id)) {
                            FavoritePrefs.removeFavorite(context, restaurant.id)
                            favoriteIds = favoriteIds - restaurant.id
                        } else {
                            FavoritePrefs.addFavorite(context, restaurant.id)
                            favoriteIds = favoriteIds + restaurant.id
                        }
                    },
                    isFavorite = favoriteIds.contains(restaurant.id)
                )
            }
        }
    }
}

/**
 * a single restaurant list item
 */
@Composable
fun RestaurantListItem(
    restaurant: Restaurant,
    index: Int,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    isFavorite: Boolean
) {
    // background colors that rotate every 6 items  ( use modulus ;) )
    val backgroundColors = listOf(
        Color(0xFFFFCDC9),
        Color(0xFFFFECBF),
        Color(0xFFCFEDC7),
        Color(0xFFB0DFF7),
        Color(0xFFE7D5F7),
        Color(0xFFFADCF1)
    )
    val backgroundColor = backgroundColors[index % backgroundColors.size]

    Row(modifier = Modifier
        .fillMaxWidth()
        .background(backgroundColor)
        .clickable { onClick() }
        .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            // left side has the name and category
            Text(
                restaurant.name,
                fontSize = 25.sp,
                color = Color.Black,
                fontFamily = monaspace,
                fontWeight = FontWeight.Bold
            )
            Text(
                restaurant.types.joinToString(" · "),
                fontSize = 20.sp,
                color = Color.DarkGray,
                fontFamily = ubuntu
            )
        }

        // star icon for favorites
        IconButton(
            onClick = { onFavoriteToggle() },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (isFavorite) Color(0xFFFFD700) else Color.LightGray,
                modifier = Modifier
                    .size(28.dp)
                    .then(
                        if (!isFavorite) Modifier.border(
                            1.dp,
                            Color.Gray,
                            CircleShape
                        ) else Modifier
                    )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // right side shows the wait time box
        Box(
            modifier = Modifier
                .width(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = restaurant.waitTimeMinutes?.toString() ?: "—",
                    fontSize = 25.sp,
                    fontFamily = monaspace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    text = if (restaurant.waitTimeMinutes != null) "MIN" else "",
                    fontSize = 15.sp,
                    fontFamily = ubuntu,
                    color = Color.Black
                )
            }
        }
    }
}