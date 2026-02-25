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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale

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
                        vehicleId = vehicleId,
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
fun VehicleDetailScreen(
    modifier: Modifier = Modifier,
    vehicleId: String,
    name: String,
    imageUrl: String,
    price: Double,
    description: String,
    count: Int,
    bookingStatus: String? = null,
    onBook: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isSaved by remember { mutableStateOf(false) }
    var savedDocId by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    // check saved status
    LaunchedEffect(vehicleId, user?.uid) {
        if (user == null) return@LaunchedEffect
        db.collection("savedVehicles")
            .whereEqualTo("userId", user.uid)
            .whereEqualTo("vehicleId", vehicleId)
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                val doc = snap.documents.firstOrNull()
                if (doc != null) {
                    isSaved = true
                    savedDocId = doc.id
                } else {
                    isSaved = false
                    savedDocId = null
                }
            }
            .addOnFailureListener { /* ignore */ }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        if (imageUrl.isNotBlank()) {
            AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(220.dp), contentScale = ContentScale.Crop)
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

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBook, modifier = Modifier.weight(1f), enabled = count > 0) {
                Text("Book Now")
            }

            // Save / Unsave button
            if (user != null) {
                if (isSaved) {
                    Button(onClick = {
                        // unsave
                        if (savedDocId == null) return@Button
                        saving = true
                        db.collection("savedVehicles").document(savedDocId!!).delete()
                            .addOnSuccessListener {
                                saving = false
                                isSaved = false
                                savedDocId = null
                                coroutineScope.launch { snackbarHostState.showSnackbar("Removed from saved") }
                            }
                            .addOnFailureListener { e ->
                                saving = false
                                coroutineScope.launch { snackbarHostState.showSnackbar("Remove failed: ${e.message}") }
                            }
                    }, modifier = Modifier.width(130.dp)) { Text("Saved") }
                } else {
                    Button(onClick = {
                        // save
                        saving = true
                        val map = hashMapOf(
                            "userId" to user.uid,
                            "vehicleId" to vehicleId,
                            "vehicleName" to name,
                            "pricePerDay" to price,
                            "imageUrl" to imageUrl,
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )
                        db.collection("savedVehicles").add(map)
                            .addOnSuccessListener { docRef ->
                                saving = false
                                isSaved = true
                                savedDocId = docRef.id
                                coroutineScope.launch { snackbarHostState.showSnackbar("Saved") }
                            }
                            .addOnFailureListener { e ->
                                saving = false
                                coroutineScope.launch { snackbarHostState.showSnackbar("Save failed: ${e.message}") }
                            }
                    }, modifier = Modifier.width(130.dp)) { Text("Save") }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // snackbar host for feedback
        Box(modifier = Modifier.fillMaxWidth()) {
            SnackbarHost(hostState = snackbarHostState)
        }
    }
}
