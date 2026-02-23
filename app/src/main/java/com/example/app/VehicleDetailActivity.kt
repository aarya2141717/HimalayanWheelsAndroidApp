package com.example.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.AppTheme
import coil.compose.AsyncImage
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
class VehicleDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vehicleId = intent.getStringExtra("vehicleId") ?: ""
        val vehicleName = intent.getStringExtra("vehicleName") ?: "Vehicle"
        val vehicleImageUrl = intent.getStringExtra("vehicleImageUrl") ?: ""
        val vehiclePrice = intent.getDoubleExtra("vehiclePrice", 0.0)
        val vehicleDesc = intent.getStringExtra("vehicleDesc") ?: "No description"
        val vehicleCount = intent.getIntExtra("vehicleCount", 0)

        setContent {
            AppTheme {
                val activity = this@VehicleDetailActivity
                var bookingStatus by remember { mutableStateOf<String?>(null) }

                // load current user's booking status for this vehicle (latest)
                LaunchedEffect(vehicleId) {
                    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        FirebaseFirestore.getInstance().collection("bookings")
                            .whereEqualTo("vehicleId", vehicleId)
                            .whereEqualTo("userId", user.uid)
                            .orderBy("createdAt")
                            .limit(1)
                            .get()
                            .addOnSuccessListener { snap ->
                                val doc = snap.documents.firstOrNull()
                                if (doc != null) {
                                    bookingStatus = doc.getString("status") ?: "PENDING"
                                }
                            }
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(text = vehicleName) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(painter = painterResource(R.drawable.baseline_arrow_back_24), contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { padding ->
                    VehicleDetailScreen(
                        modifier = Modifier.padding(padding),
                        name = vehicleName,
                        imageUrl = vehicleImageUrl,
                        price = vehiclePrice,
                        description = vehicleDesc,
                        count = vehicleCount,
                        bookingStatus = bookingStatus,
                        onBook = {
                            val intent = Intent(activity, BookingActivity::class.java).apply {
                                putExtra("vehicleId", vehicleId)
                                putExtra("vehicleName", vehicleName)
                                putExtra("vehicleImageUrl", vehicleImageUrl)
                                putExtra("vehiclePrice", vehiclePrice)
                            }
                            activity.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(modifier: Modifier = Modifier, name: String, imageUrl: String, price: Double, description: String, count: Int, bookingStatus: String? = null, onBook: () -> Unit) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        if (imageUrl.isNotBlank()) {
            AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(220.dp), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        } else {
            Image(painter = painterResource(R.drawable.thar), contentDescription = null, modifier = Modifier.fillMaxWidth().height(220.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(name, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(description)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Price: ₹${price}/day", color = Color(0xFF2196F3))
        Spacer(modifier = Modifier.height(8.dp))
        Text(if (count <= 0) "Unavailable" else "Available: $count", color = if (count <= 0) Color.Red else Color.Green)
        Spacer(modifier = Modifier.height(12.dp))

        if (bookingStatus != null) {
            Text("Your booking status: ${bookingStatus}", color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(onClick = onBook, modifier = Modifier.fillMaxWidth(), enabled = count > 0) {
            Text("Book Now")
        }
    }
}
