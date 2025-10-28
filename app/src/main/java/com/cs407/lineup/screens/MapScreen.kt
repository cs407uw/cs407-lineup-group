package com.cs407.lineup.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.lineup.R
import kotlinx.coroutines.launch

/**
 * data class to represent restaurants ( i think this is what we should use b/c objects are singletons in Kotlin
 * takes in some simple data (name, description, wait time, restaurant type). we can change these or add to these
 * as needed.
 */
data class Restaurant(
    val name: String,
    val description: String,
    val waitTimeMinutes: Int,
    val type: String
)

// list of hardcoded restaurants using our Restaurant data class. just passes in some hardcoded data for now.
val HardcodedRestaurants = listOf(
    Restaurant("Nitty Gritty", "Industrial-style tavern with varied burgers, sandwiches & other Americans eats, plus many beers.", 15, "American"),
    Restaurant("Dotty Dumpling's", "Longtime local institution offering specialty burgers, cheese curds & tap beers in quirky digs.", 20, "American"),
    Restaurant("Old Fashioned", "Wisconsin-themed, retro-style tavern offering beers, brats & cheese curds (all sourced in-state).", 35, "American"),
    Restaurant("Great Dane Pub", "A changing roster of craft brews & pub eats served in a lively venue with beer garden & pool tables.", 10, "Pub/Bar"),
    Restaurant("Lucille's", "Vintage, industrial-chic tavern with 3 floors serving wood-fired & steel-pan pizzas, plus cocktails.", 45, "Italian"),
    Restaurant("Porta Bella", "Longtime romantic restaurant serving traditional Italian fare in a venue with a cellar bar & patio.", 25, "Italian"),
    Restaurant("A La Brasa", "Mole poblano, fajitas & other Mexican favorites doled out in a simple, brightly colored setting.", 25, "Mexican")
)

// some custom imported fonts
val roca = FontFamily(Font(R.font.roca2))
val ubuntu = FontFamily(Font(R.font.ubuntu))

// using modalBottomSheet for the little menu that you can swipe up and down. It's
// an experimential feature but I couldn't find anything else built-in that had this functionality.
// if we want to change this, we probbaly have to build our own from scratch (or find something else).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    // sheet documentation: https://developer.android.com/develop/ui/compose/components/bottom-sheets-partial
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    // remember boolean to handle if the swipeable list/menu is showing or not
    var showSheet by remember { mutableStateOf(true) }

    var showProfileCard by remember { mutableStateOf(false) }

    //Variables for profile card
    var name by remember { mutableStateOf("") }
    var home by remember { mutableStateOf("") }
    var work by remember { mutableStateOf("") }

    MapPlaceholder(Modifier.fillMaxSize())

    Box(modifier = modifier
        .fillMaxSize()
        .background(Color.White))
    {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
//                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {showProfileCard = !showProfileCard})
            {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
//                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // TODO: add real map composable
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
                        modifier = Modifier
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row (
                            modifier = Modifier
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                modifier = Modifier.size(64.dp)
                            )
                            Text("Profile")
                        }

                        OutlinedTextField(value = name,
                            onValueChange = {name = it},
                            label = {Text(stringResource(id = R.string.name)) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {Icon(Icons.Default.Person, contentDescription = null)},
                            shape = RoundedCornerShape(12.dp), // Rounded corners
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1B5E20), // Your app's green
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = Color(0xFF1B5E20),
                                cursorColor = Color(0xFF1B5E20)
                            )
                        )
                        OutlinedTextField(value = home,
                            onValueChange = {home = it},
                            label = {Text(stringResource(id = R.string.home)) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {Icon(Icons.Default.Home, contentDescription = null)},
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1B5E20), // Your app's green
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = Color(0xFF1B5E20),
                                cursorColor = Color(0xFF1B5E20)
                            )
                        )
                        OutlinedTextField(value = work,
                            onValueChange = {work = it},
                            label = {Text(stringResource(id = R.string.work)) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {Icon(Icons.Default.Place, contentDescription = null)},
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1B5E20), // Your app's green
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = Color(0xFF1B5E20),
                                cursorColor = Color(0xFF1B5E20)
                            )
                        )
                    }
                    Button(
                        onClick = { showProfileCard = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
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
// TODO: replace with map composable
@Composable
fun MapPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(Color(0xFFE0E0E0)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "placeholder for map :D",
            color = Color.DarkGray,
            fontSize = 18.sp,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantListSheet(restaurants: List<Restaurant>, onClose: () -> Unit) {
    var selectedCategory by remember { mutableStateOf("All Establishments") }
    // note that we can absolutely change and add to these categories, I just made them based on the random
    // hardcoded locations we have lol
    val categories = listOf("All Establishments", "American", "Mexican", "Italian", "Pub/Bar")
    var expanded by remember { mutableStateOf(false) }

    Column {
        // use exposed dropdown menu because it tracks whether the menu is open or collapsed and
        // it was the only thing that worked for aligning the dropdown right under for some reason
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    // this is deprecated and the fix is to use textfield but i am so tired
                    // so its going to stay here for now
                    // https://developer.android.com/develop/ui/compose/modifiers-list
                    // https://stackoverflow.com/questions/77756304/textfieldcolors-deprecated-in-jetpack-compose-android
                    .menuAnchor()
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF1B5E20))
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // show the selectedCategory as the menu header
                    Text(
                        text = selectedCategory,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontFamily = roca
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown arrow",
                        tint = Color.White
                    )
                }
            }

            // this is the menu for when you click on the dropdown icon and it shows
            // the list of categories to choose from
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
                                    fontFamily = roca
                                )
                            }
                        },
                        // close the menu and update the selected category when clicked
                        onClick = {
                            selectedCategory = category
                            expanded = false
                        }
                    )
                }
            }
        }

        // scrollable column for restaurant items that includes filtering logic to display
        // the appropriate establishments under a specific filter (don't know exactly what categories we want yet)
        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            val filteredRestaurants =
                // no filter if all establishments is selected, otherwise filter by category
                if (selectedCategory == "All Establishments") restaurants
                else restaurants.filter { it.type == selectedCategory }
            // display each restaurant item, with a black divider in between
            itemsIndexed(filteredRestaurants) { index, restaurant ->
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                RestaurantListItem(restaurant, index)
            }
        }
    }
}

@Composable
fun RestaurantListItem(restaurant: Restaurant, index: Int) {
    // list of colors (can change if we want) so that we can cycle through them as per the figma
    val backgroundColors = listOf(
        Color(0xFFFFCDC9),
        Color(0xFFFFECBF),
        Color(0xFFCFEDC7),
        Color(0xFFB0DFF7),
        Color(0xFFE7D5F7),
        Color(0xFFFADCF1)
    )
    // use modulus operator to cycle thru colors so that we switch off in the order specified above
    val backgroundColor = backgroundColors[index % backgroundColors.size]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(restaurant.name, fontSize = 25.sp, color = Color.Black, fontFamily=roca)
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
                    fontFamily = roca,
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
