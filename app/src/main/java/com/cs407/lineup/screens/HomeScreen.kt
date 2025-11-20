package com.cs407.lineup.screens

import NearbySearchRepository
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.lineup.R
import com.cs407.lineup.data.Restaurant
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
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
import com.cs407.lineup.widget.LastRestaurantWidget
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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

    // variable for fetching nearby restaurants
    var nearbyRestaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) }

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
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
                    // launch a coroutine in GlobalScope so it lives throughout lifetime of app to update widget
                    // when a restaurant is clicked on
                    restaurants = nearbyRestaurants, onItemClick = { r ->
                        selected = r
                        RestaurantPrefs.saveRestaurant(
                            context, r.name, r.waitTimeMinutes, r.color.value.toInt()
                        )
                        GlobalScope.launch {
                            LastRestaurantWidget().updateAll(context)
                        }
                        onRestaurantClick(r)
                    },
                    // automatically highlight the first restaurant when user changes the filter category
                    // this is so that we can display the first one in the list on the map by default
                    onCategoryChangeFirst = { first -> first?.let { selected = it } }, onClose = {
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
    onItemClick: (Restaurant) -> Unit,
    onCategoryChangeFirst: (Restaurant?) -> Unit,
    onClose: () -> Unit,
    favoriteCategories: Set<String> = emptySet()
) {
    // label for dropdown & all the categories
    val allLabel = stringResource(R.string.category_all)

    // determine initial category based on favorites
    val initialCategory = when {
        favoriteCategories.isEmpty() -> allLabel
        favoriteCategories.size == 1 -> favoriteCategories.first()
        else -> allLabel // multiple favorites, show all
    }

    var selectedCategory by remember { mutableStateOf(initialCategory) }

    val categories = listOf(
        stringResource(R.string.category_all),
        "Restaurant",
        "Bar",
        "Cafe",
        "Grocery"
    )

    // variables for expanded and filter to remember their respective states
    var expanded by remember { mutableStateOf(false) }

    val filtered by remember(selectedCategory, restaurants) {
        derivedStateOf {
            if (selectedCategory == allLabel) restaurants
            else restaurants.filter { it.type == selectedCategory }
        }
    }

    // when selected category is changed, refilter and update first restaurant to be highlighted on map
    LaunchedEffect(selectedCategory) {
        onCategoryChangeFirst(filtered.firstOrNull())
    }

    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .menuAnchor() // anchor the dropdown to this element
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF1B5E20))
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                // show the currently selected category text in the dropdown menu title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = selectedCategory,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontSize = 22.sp,
                        fontFamily = monaspace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown arrow",
                        tint = Color.White
                    )
                }
            }

            // the dropdown list of categories
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color(0xFF1B5E20))
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // add each category option available
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontFamily = monaspace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        onClick = {
                            // if one is clicked, update the selected category
                            selectedCategory = category
                            expanded = false
                        }


                    )
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
            return
        }

        // list of restaurants (once loaded)
        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            itemsIndexed(filtered) { index, restaurant ->
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                RestaurantListItem(restaurant, index) { onItemClick(restaurant) }
            }
        }
    }
}


/**
 * a single restaurant list item
 */
@Composable
fun RestaurantListItem(restaurant: Restaurant, index: Int, onClick: () -> Unit) {
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
                    text = "${restaurant.waitTimeMinutes}",
                    fontSize = 25.sp,
                    fontFamily = monaspace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    text = "MIN", fontSize = 15.sp, fontFamily = ubuntu, color = Color.Black
                )
            }
        }
    }
}

