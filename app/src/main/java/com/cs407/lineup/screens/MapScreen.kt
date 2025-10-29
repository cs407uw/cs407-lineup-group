package com.cs407.lineup.screens

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
import androidx.compose.material3.MenuAnchorType
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
import com.cs407.lineup.data.HardcodedRestaurants
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


val roca = FontFamily(Font(R.font.roca2))
val ubuntu = FontFamily(Font(R.font.ubuntu))
val monaspace = FontFamily(Font(R.font.monaspace_neon))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showSheet by remember { mutableStateOf(true) }

    var showProfileCard by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var home by remember { mutableStateOf("") }
    var work by remember { mutableStateOf("") }

    var selected by remember { mutableStateOf<Restaurant?>(null) }
    val initial = selected?.latLng ?: LatLng(43.0731, -89.4012) // default Madison
    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initial, 13f)
    }

    LaunchedEffect(selected) {
        selected?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it.latLng, 15f),
                durationMs = 600
            )
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showSheet) 220.dp else 0.dp),
            cameraPositionState = cameraPositionState
        ) {
            val toShow = selected ?: HardcodedRestaurants.first()
            Marker(
                state = MarkerState(position = toShow.latLng),
                title = toShow.name
            )
        }

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


        if (showProfileCard) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showProfileCard = false },
                contentAlignment = Alignment.Center
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(0.9f)
                        .clickable(enabled = false) {}
                ) {
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
                        Text(stringResource(R.string.profile_preferences), fontFamily = monaspace, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(stringResource(R.string.name)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
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

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showProfileCard = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                        ) {
                            Text(stringResource(R.string.close_button), color = Color.White)
                        }
                    }
                }
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
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
                }
            ) {
                RestaurantListSheet(
                    restaurants = HardcodedRestaurants,
                    onItemClick = { r -> selected = r },
                    onCategoryChangeFirst = { first -> first?.let { selected = it } },
                    onClose = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showSheet = false
                        }
                    }
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.White, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .clickable {
                        showSheet = true
                        scope.launch { sheetState.show() }
                    },
                contentAlignment = Alignment.Center
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantListSheet(
    restaurants: List<Restaurant>,
    onItemClick: (Restaurant) -> Unit,
    onCategoryChangeFirst: (Restaurant?) -> Unit,
    onClose: () -> Unit
) {
    val allLabel = stringResource(R.string.category_all)
    var selectedCategory by remember { mutableStateOf(allLabel) }
    val categories = listOf(
        stringResource(R.string.category_all),
        stringResource(R.string.category_american),
        stringResource(R.string.category_mexican),
        stringResource(R.string.category_italian),
        stringResource(R.string.category_pub_bar)
    )
    var expanded by remember { mutableStateOf(false) }

    val filtered by remember(selectedCategory, restaurants) {
        derivedStateOf {
            if (selectedCategory == allLabel) restaurants
            else restaurants.filter { it.type == selectedCategory }
        }
    }

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
                    .menuAnchor()
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF1B5E20))
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
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

                Box (
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

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color(0xFF1B5E20))
                    .clip(RoundedCornerShape(12.dp))
            ) {
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
                            selectedCategory = category
                            expanded = false
                        }
                    )
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            itemsIndexed(filtered) { index, restaurant ->
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                RestaurantListItem(restaurant, index) { onItemClick(restaurant) }
            }
        }
    }
}

@Composable
fun RestaurantListItem(restaurant: Restaurant, index: Int, onClick: () -> Unit) {
    val backgroundColors = listOf(
        Color(0xFFFFCDC9),
        Color(0xFFFFECBF),
        Color(0xFFCFEDC7),
        Color(0xFFB0DFF7),
        Color(0xFFE7D5F7),
        Color(0xFFFADCF1)
    )
    val backgroundColor = backgroundColors[index % backgroundColors.size]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(restaurant.name, fontSize = 25.sp, color = Color.Black, fontFamily = monaspace, fontWeight = FontWeight.Bold)
            Text(restaurant.type, fontSize = 20.sp, color = Color.DarkGray, fontFamily = ubuntu)
        }
        Box(
            modifier = Modifier
                .width(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "${restaurant.waitTimeMinutes}",
                    fontSize = 25.sp,
                    fontFamily = monaspace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    text = "MIN",
                    fontSize = 15.sp,
                    fontFamily = ubuntu,
                    color = Color.Black
                )
            }
        }
    }
}
