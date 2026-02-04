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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.app.model.VehicleModel
import com.example.app.ui.theme.AppTheme
import com.example.app.viewmodel.VendorViewModel
import kotlinx.coroutines.launch

class CompanyDashboardActivity : ComponentActivity() {

    private val vm: VendorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val companyId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "company-uid-placeholder"
                vm.observeVendor(companyId)
                val vehicles by vm.vehicles.observeAsState(emptyList())

                CompanyDashboardScreen(vehicles = vehicles, vm = vm)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDashboardScreen(vehicles: List<VehicleModel>, vm: VendorViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Company Dashboard") })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                try {
                    val intent = Intent(context, AddVehicleActivity::class.java)
                    if (context is Activity) context.startActivity(intent) else {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                } catch (e: Exception) {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Cannot open Add Vehicle: ${e.localizedMessage}") }
                }
            }, containerColor = Color(0xFF2F4CFA)) {
                Text("+", color = Color.White)
            }
        },
        containerColor = Color(0xFFF7F9FC)
    ) { paddingValues ->

        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            item {
                // Top stats
                val available = vehicles.count { it.totalCount > 0 }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(modifier = Modifier.weight(1f), title = "Vehicles", value = "${vehicles.size}")
                    StatCard(modifier = Modifier.weight(1f), title = "Available", value = "$available")
                    StatCard(modifier = Modifier.weight(1f), title = "Unavailable", value = "${vehicles.size - available}")
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Text("My Vehicles", style = MaterialTheme.typography.titleMedium)
            }

            if (vehicles.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                        Text("No vehicles yet. Tap + to add your first vehicle.")
                    }
                }
            } else {
                items(vehicles) { v ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp), verticalAlignment = Alignment.CenterVertically) {

                            if (v.imageUrl.isNotBlank()) {
                                AsyncImage(model = v.imageUrl, contentDescription = null, modifier = Modifier.size(80.dp).clip(CircleShape))
                            } else {
                                Image(painter = painterResource(R.drawable.baseline_car_rental_24), contentDescription = null, modifier = Modifier.size(80.dp).clip(CircleShape))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(v.name, fontSize = 16.sp)
                                Text("₹${v.pricePerDay}/day", color = Color(0xFF2F4CFA))
                                Text(if (v.totalCount <= 0) "Unavailable" else "Available", color = if (v.totalCount <= 0) Color.Red else Color.Green)
                            }

                            Column {
                                TextButton(onClick = {
                                    // Edit
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

            item { Spacer(modifier = Modifier.height(36.dp)) }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}
