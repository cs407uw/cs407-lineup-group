package com.cs407.lineup.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.lineup.data.Restaurant
import com.cs407.lineup.screens.monaspace
import com.cs407.lineup.screens.ubuntu

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
    // state for opening filter modal w/ categories & sorting
    var showFilterSheet by remember { mutableStateOf(false) }

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
            if (selectedCategories.contains(allLabel)) {
                restaurants
            } else {
                restaurants.filter { r ->
                    r.types.any { it in selectedCategories }
                }
            }
        }
    }

    // when selected category is changed, refilter and update first restaurant to be highlighted on map
    //LaunchedEffect(selectedCategories) {
    //    onCategoryChangeFirst(filtered.firstOrNull())
    //}

    Column {
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
                                        if (cat == allLabel) {
                                            setOf(allLabel)
                                        } else {
                                            val newSet = if (selectedCategories.contains(cat)) {
                                                selectedCategories - cat
                                            } else {
                                                (selectedCategories - allLabel) + cat
                                            }
                                            if (newSet.isEmpty()) setOf(allLabel) else newSet
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
                            onCategoryChangeFirst(filtered.firstOrNull())
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