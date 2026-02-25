package com.example.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app.model.VehicleModel
import com.example.app.ui.theme.AppTheme
import com.example.app.viewmodel.VendorViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

class VendorDashboardActivity : ComponentActivity() {

    private val vm: VendorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val vendorId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "vendor-uid-placeholder"
                vm.observeVendor(vendorId)
                val vehicles by vm.vehicles.observeAsState(emptyList())

                // Pass activity-scoped vm into composable
                VendorDashboardScreen(vehicles = vehicles, vm = vm)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDashboardScreen(vehicles: List<VehicleModel>, vm: VendorViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vendor Dashboard") },
                actions = {
                    IconButton(onClick = {
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                        val i = Intent(context, SignUpActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
                        context.startActivity(i)
                    }) {
                        Icon(painter = painterResource(R.drawable.baseline_lock_24), contentDescription = "Logout")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                try {
                    val intent = Intent(context, AddVehicleActivity::class.java)
                    // if context isn't an Activity, add FLAG_NEW_TASK
                    if (context is Activity) context.startActivity(intent) else {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                } catch (e: Exception) {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Unable to open add vehicle screen: ${e.localizedMessage}") }
                }
            }, containerColor = Color(0xFF2F4CFA)) {
                Text("+", color = Color.White)
            }
        },
        bottomBar = {
            Column {
                BottomAppBar(containerColor = Color.White) {
                    TextButton(onClick = {}) { Text("View My Vehicles") }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        // Open vendor bookings screen
                        try {
                            val intent = Intent(context, VendorBookingsActivity::class.java)
                            if (context is Activity) context.startActivity(intent) else {
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        } catch (e: Exception) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("Unable to open bookings: ${e.localizedMessage}") }
                        }
                    }) { Text("View Bookings") }
                    TextButton(onClick = {}) { Text("Earnings") }
                }
            }
        },
        containerColor = Color(0xFFF7F9FC)
    ) { paddingValues ->
        if (vehicles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No vehicles available. Tap + to add one.")
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(vehicles) { v ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (v.imageUrl.isNotBlank()) {
                            AsyncImage(model = v.imageUrl, contentDescription = null, modifier = Modifier.size(72.dp).clip(CircleShape))
                        } else {
                            Image(painter = painterResource(R.drawable.baseline_car_rental_24), contentDescription = null, modifier = Modifier.size(72.dp).clip(CircleShape))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(v.name, fontSize = 16.sp)
                            Text("₹${v.pricePerDay} / day", color = Color(0xFF2F4CFA))
                            Text(if (v.totalCount <= 0) "Unavailable" else "Available", color = if (v.totalCount <= 0) Color.Red else Color.Green)
                        }
                        Column {
                            TextButton(onClick = {
                                // open edit screen with vehicle id
                                try {
                                    val intent = Intent(context, AddVehicleActivity::class.java)
                                    intent.putExtra("vehicleId", v.id)
                                    if (context is Activity) context.startActivity(intent) else {
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    }
                                } catch (e: Exception) {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Unable to open edit: ${e.localizedMessage}") }
                                }
                            }) { Text("Edit") }

                            TextButton(onClick = {
                                // ask for confirm via snackbar action
                                coroutineScope.launch {
                                    val res = snackbarHostState.showSnackbar(message = "Delete ${v.name}?", actionLabel = "Confirm")
                                    if (res == SnackbarResult.ActionPerformed) {
                                        vm.deleteVehicle(v.id) { ok, msg ->
                                            coroutineScope.launch {
                                                if (ok) snackbarHostState.showSnackbar("Deleted ${v.name}") else snackbarHostState.showSnackbar("Delete failed: ${msg ?: "unknown"}")
                                            }
                                        }
                                    }
                                }
                            }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
}
