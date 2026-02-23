package com.example.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app.model.VehicleModel
import com.example.app.ui.theme.AppTheme
import com.example.app.viewmodel.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class DashboardActivity : ComponentActivity() {
    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                var userName by remember { mutableStateOf("User") }
                var vehicles by remember { mutableStateOf(listOf<VehicleModel>()) }

                // Step 1: Fetch username safely
                LaunchedEffect(Unit) {
                    viewModel.fetchCurrentUserName { name ->
                        userName = name
                        Log.d("DashboardActivity", "Current user name: $userName")
                    }
                }

                // Step 2: Listen to vehicles safely
                val db = FirebaseFirestore.getInstance()
                DisposableEffect(Unit) {
                    val registration: ListenerRegistration = db.collection("vehicles")
                        .addSnapshotListener { snap, err ->
                            if (err != null) {
                                Log.w("DashboardActivity", "Vehicle listener error: ${err.message}")
                                return@addSnapshotListener
                            }
                            val list = snap?.documents?.mapNotNull {
                                try {
                                    val vehicle = it.toObject(VehicleModel::class.java)
                                    if (vehicle != null) vehicle.copy(id = it.id) else null
                                } catch (ex: Exception) {
                                    Log.e("DashboardActivity", "Failed parsing vehicle ${it.id}", ex)
                                    null
                                }
                            } ?: emptyList()
                            vehicles = list
                            Log.d("DashboardActivity", "Vehicles loaded: ${vehicles.size}")
                        }
                    onDispose { registration.remove() }
                }

                // Step 3: Safe DashboardScreen
                DashboardScreen(
                    userName = userName,
                    vehicles = vehicles,
                    onProfileClick = { startActivity(Intent(this, ProfileActivity::class.java)) },
                    onBookingsClick = { startActivity(Intent(this, BookingsActivity::class.java)) },
                    onSavedClick = { startActivity(Intent(this, SavedActivity::class.java)) },
                    onVehicleClick = { vehicle ->
                        val i = Intent(this, VehicleDetailActivity::class.java)
                        // Use direct properties (no Elvis operators)
                        i.putExtra("vehicleId", vehicle.id)
                        i.putExtra("vehicleName", vehicle.name)
                        i.putExtra("vehicleImageUrl", vehicle.imageUrl)
                        i.putExtra("vehiclePrice", vehicle.pricePerDay)
                        i.putExtra("vehicleDesc", vehicle.description)
                        i.putExtra("vehicleCount", vehicle.totalCount)
                        startActivity(i)
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
    userName: String,
    vehicles: List<VehicleModel>,
    onProfileClick: () -> Unit,
    onBookingsClick: () -> Unit,
    onSavedClick: () -> Unit,
    onVehicleClick: (VehicleModel) -> Unit
) {
    Scaffold(
        bottomBar = { DashboardBottomNavBar(onProfileClick, onBookingsClick, onSavedClick) },
        containerColor = Color(0xFFF7F9FC)
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item { DashboardTopBar(userName, onProfileClick) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { DashboardSearchBar() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { DashboardCategorySection() }
            item { Spacer(modifier = Modifier.height(20.dp)) }
            item { DashboardFeaturedHeader() }
            item { Spacer(modifier = Modifier.height(12.dp)) }

            // === REPLACED: render vehicles in vertical rows (2 columns) to avoid nested LazyColumn ===
            items(vehicles.chunked(2)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (v in rowItems) {
                        VehicleCard(v, onVehicleClick, modifier = Modifier.weight(1f))
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
            item {
                DashboardOfferCard(
                    title = "Monsoon Special",
                    description = "Get 20% off on all SUVs this weekend.",
                    image = R.drawable.thar,
                    bgColor = Color(0xFF1565C0)
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                DashboardOfferCard(
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

// New VehicleCard composable per instructions (now accepts modifier)
@Composable
fun VehicleCard(
    v: VehicleModel,
    onVehicleClick: (VehicleModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable {
                // onVehicleClick should pass the vehicle directly (intent built in caller)
                onVehicleClick(v)
            },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            if (v.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = v.imageUrl,
                    contentDescription = v.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.thar),
                    contentDescription = v.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(v.name, fontWeight = FontWeight.Bold)
                Text("₹${v.pricePerDay}/day", color = Color(0xFF2196F3))

                if (v.totalCount <= 0) Text("Unavailable", color = Color.Red)
                else Text("Available: ${v.totalCount}", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun DashboardBottomNavBar(onProfile: () -> Unit, onBookings: () -> Unit, onSaved: () -> Unit) {
    Surface(shadowElevation = 6.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = "Profile", modifier = Modifier.clickable { onProfile() })
            Text(text = "Bookings", modifier = Modifier.clickable { onBookings() })
            Text(text = "Saved", modifier = Modifier.clickable { onSaved() })
        }
    }
}

// --- Added missing composables as requested ---
@Composable
fun DashboardTopBar(userName: String, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Welcome,", fontSize = 14.sp, color = Color.Gray)
            Text(userName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "Profile",
            color = Color(0xFF1565C0),
            modifier = Modifier.clickable { onProfileClick() }
        )
    }
}

@Composable
fun DashboardSearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        placeholder = { Text("Search vehicles...") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun DashboardCategorySection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("SUV")
        Text("Bike")
        Text("Luxury")
        Text("Electric")
    }
}

@Composable
fun DashboardFeaturedHeader() {
    Text(
        text = "Featured Vehicles",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
}

@Composable
fun DashboardOfferCard(
    title: String,
    description: String,
    image: Int,
    bgColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, color = Color.White)
            }

            Image(
                painter = painterResource(image),
                contentDescription = title,
                modifier = Modifier.size(70.dp)
            )
        }
    }
}
