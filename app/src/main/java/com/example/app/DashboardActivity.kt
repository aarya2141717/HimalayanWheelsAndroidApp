package com.example.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import coil.compose.AsyncImage
import com.example.app.model.VehicleModel

class DashboardActivity : ComponentActivity() {
    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                var userName by remember { mutableStateOf("User") }
                var vehicles by remember { mutableStateOf(listOf<VehicleModel>()) }

                LaunchedEffect(Unit) {
                    viewModel.fetchCurrentUserName { name ->
                        if (!name.isNullOrBlank()) userName = name
                    }
                }

                // Real-time listener for vehicles so new vehicles show up immediately
                val db = FirebaseFirestore.getInstance()
                DisposableEffect(Unit) {
                    val registration: ListenerRegistration = db.collection("vehicles")
                        .addSnapshotListener { snap, err ->
                            if (err != null) {
                                // keep list empty or unchanged
                                return@addSnapshotListener
                            }
                            vehicles = snap?.documents?.mapNotNull { it.toObject(VehicleModel::class.java)?.copy(id = it.id) } ?: emptyList()
                        }
                    onDispose { registration.remove() }
                }

                DashboardScreen(userName = userName, vehicles = vehicles, onProfileClick = {
                    startActivity(Intent(this, ProfileActivity::class.java))
                }, onBookingsClick = {
                    startActivity(Intent(this, BookingsActivity::class.java))
                }, onSavedClick = {
                    startActivity(Intent(this, SavedActivity::class.java))
                }, onVehicleClick = { vehicle ->
                    val i = Intent(this, VehicleDetailActivity::class.java)
                    i.putExtra("vehicleName", vehicle.name)
                    i.putExtra("vehicleImageUrl", vehicle.imageUrl)
                    startActivity(i)
                })
            }
        }
    }
}

@Composable
fun DashboardScreen(userName: String, vehicles: List<VehicleModel>, onProfileClick: () -> Unit, onBookingsClick: () -> Unit, onSavedClick: () -> Unit, onVehicleClick: (VehicleModel) -> Unit) {

    Scaffold(
        bottomBar = { BottomNavBar(onProfileClick, onBookingsClick, onSavedClick) },
        containerColor = Color(0xFFF7F9FC)
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            item { TopBar(userName, onProfileClick) }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item { SearchBar() }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item { CategorySection() }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            item { FeaturedHeader() }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item { FeaturedVehiclesList(vehicles, onVehicleClick) }

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

@Composable
fun FeaturedVehiclesList(vehicles: List<VehicleModel>, onVehicleClick: (VehicleModel) -> Unit) {
    LazyRow {
        items(count = vehicles.size) { index ->
            val v = vehicles[index]
            Card(modifier = Modifier.width(240.dp).padding(end = 12.dp)) {
                Column(modifier = Modifier.clickable { onVehicleClick(v) }) {
                    if (v.imageUrl.isNotBlank()) {
                        AsyncImage(model = v.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(140.dp))
                    } else {
                        Image(painter = painterResource(R.drawable.thar), contentDescription = null, modifier = Modifier.fillMaxWidth().height(140.dp))
                    }
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(v.name, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("₹${v.pricePerDay}/day", color = Color(0xFF2196F3))
                        if (v.totalCount <= 0) Text("Unavailable", color = Color.Red)
                    }
                }
            }
        }
    }
}

/* -------------------- TOP BAR -------------------- */

@Composable
fun TopBar(userName: String, onProfileClick: () -> Unit) {
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
            Text("Welcome", color = Color.Gray, fontSize = 12.sp)
            Text(userName, fontWeight = FontWeight.Bold)
        }

        IconButton(onClick = onProfileClick) {
            Icon(painter = painterResource(R.drawable.baseline_settings_24), contentDescription = "Profile")
        }
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
fun FeaturedVehicles(onVehicleClick: (Int,String) -> Unit) {
    // kept for demo purposes; not used in the featured list implementation
    LazyRow {
        item {
            VehicleCard(R.drawable.thar, "Mahindra Thar 4×4", "$60/day", onVehicleClick)
        }
        item {
            VehicleCard(R.drawable.royal_enfield, "Royal Enfield", "$25/day", onVehicleClick)
        }
    }
}

@Composable
fun VehicleCard(image: Int, name: String, price: String, onClick: (Int,String) -> Unit) {
    Card(
        modifier = Modifier
            .width(240.dp)
            .padding(end = 12.dp)
            .clickable { onClick(image,name) },
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
fun BottomNavBar(onProfileClick: () -> Unit, onBookingsClick: () -> Unit, onSavedClick: () -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(painterResource(R.drawable.baseline_home_24), null) }, // Dashboard
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onBookingsClick,
            icon = { Icon(painterResource(R.drawable.baseline_calendar_month_24), null) }, // Bookings
            label = { Text("Bookings") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onSavedClick,
            icon = { Icon(painterResource(R.drawable.baseline_favorite_24), null) }, // Saved
            label = { Text("Saved") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = { Icon(painterResource(R.drawable.baseline_account_circle_24), null) }, // Profile
            label = { Text("Profile") }
        )
    }
}
