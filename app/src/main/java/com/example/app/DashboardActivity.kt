package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.ui.theme.AppTheme
import com.example.app.viewmodel.UserViewModel

class DashboardActivity : ComponentActivity() {
    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                var userName by remember { mutableStateOf("User") }
                
                LaunchedEffect(Unit) {
                    viewModel.getUserDetails { user ->
                        if (user != null) {
                            // If name is present, use it.
                            // If name is empty, try using email prefix or fallback to "User"
                            userName = user.name.ifEmpty { 
                                user.email.substringBefore("@").ifEmpty { "User" } 
                            }
                        }
                    }
                }

                DashboardScreen(userName = userName)
            }
        }
    }
}

@Composable
fun DashboardScreen(userName: String) {

    Scaffold(
        bottomBar = { BottomNavBar() },
        containerColor = Color(0xFFF7F9FC)
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            item { TopBar(userName) }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item { SearchBar() }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item { CategorySection() }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            item { FeaturedHeader() }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item { FeaturedVehicles() }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            item {
                OfferCard(
                    title = "Monsoon Special",
                    description = "Get 20% off on all SUVs this weekend.",
                    image = R.drawable.thar,
                    bgColor = Color(0xFF1565C0)
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                OfferCard(
                    title = "New Year Offer",
                    description = "Flat 30% off on Royal Enfield bookings!",
                    image = R.drawable.royal_enfield,
                    bgColor = Color(0xFF2E7D32)
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

/* -------------------- TOP BAR -------------------- */

@Composable
fun TopBar(userName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.baseline_account_circle_24),
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .background(Color.Transparent, CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text("Welcome back", color = Color.Gray, fontSize = 12.sp)
            Text(userName, fontWeight = FontWeight.Bold)
        }

        Icon(
            painter = painterResource(R.drawable.baseline_notifications_24),
            contentDescription = null
        )
    }
}

/* -------------------- SEARCH BAR -------------------- */

@Composable
fun SearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        placeholder = { Text("Find your ride (e.g Jeep, Bike)...") },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.baseline_search_24),
                contentDescription = null
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}

/* -------------------- CATEGORY SECTION -------------------- */

@Composable
fun CategorySection() {
    LazyRow {
        item { CategoryChip("Car", R.drawable.baseline_car_rental_24, true) }
        item { CategoryChip("Bike", R.drawable.baseline_directions_bike_24) }
        item { CategoryChip("Jeep", R.drawable.baseline_car_rental_24) }
        item { CategoryChip("Scooter", R.drawable.baseline_directions_bike_24) }
    }
}

@Composable
fun CategoryChip(text: String, iconRes: Int, selected: Boolean = false) {
    Row(
        modifier = Modifier
            .padding(end = 8.dp)
            .background(
                if (selected) Color(0xFF2196F3) else Color.White,
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (selected) Color.White else Color.Gray
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = if (selected) Color.White else Color.Black)
    }
}

/* -------------------- FEATURED -------------------- */

@Composable
fun FeaturedHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Featured Vehicles", fontWeight = FontWeight.Bold)
        Text("See All", color = Color(0xFF3F51B5))
    }
}

@Composable
fun FeaturedVehicles() {
    LazyRow {
        item {
            VehicleCard(R.drawable.thar, "Mahindra Thar 4×4", "$60/day")
        }
        item {
            VehicleCard(R.drawable.royal_enfield, "Royal Enfield", "$25/day")
        }
    }
}

@Composable
fun VehicleCard(image: Int, name: String, price: String) {
    Card(
        modifier = Modifier
            .width(240.dp)
            .padding(end = 12.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(name, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(price, color = Color(0xFF2196F3))
            }
        }
    }
}

/* -------------------- OFFER CARD -------------------- */

@Composable
fun OfferCard(
    title: String,
    description: String,
    image: Int,
    bgColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                Text(description, color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {}) {
                    Text("Book Now")
                }
            }

            Image(
                painter = painterResource(image),
                contentDescription = null,
                modifier = Modifier.size(90.dp)
            )
        }
    }
}

/* -------------------- BOTTOM NAV -------------------- */

@Composable
fun BottomNavBar() {
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(painterResource(R.drawable.baseline_home_24), null) },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(painterResource(R.drawable.baseline_calendar_month_24), null) },
            label = { Text("Bookings") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(painterResource(R.drawable.baseline_favorite_24), null) },
            label = { Text("Saved") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(painterResource(R.drawable.baseline_account_circle_24), null) },
            label = { Text("Profile") }
        )
    }
}
