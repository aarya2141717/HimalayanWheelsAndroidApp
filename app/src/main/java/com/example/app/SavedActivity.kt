package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.app.ui.theme.AppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

class SavedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                SavedScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen() {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var loading by remember { mutableStateOf(true) }
    var savedItems by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    LaunchedEffect(user?.uid) {
        if (user == null) {
            loading = false
            return@LaunchedEffect
        }
        // stored in collection 'savedVehicles' with fields: userId, vehicleId
        db.collection("savedVehicles").whereEqualTo("userId", user.uid).orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    loading = false
                    return@addSnapshotListener
                }
                savedItems = snap?.documents?.mapNotNull { d -> d.data?.plus(mapOf("_id" to d.id)) } ?: emptyList()
                loading = false
            }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Saved") }, navigationIcon = {
            IconButton(onClick = { ctx.startActivity(Intent(ctx, DashboardActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }) }) {
                Icon(painter = painterResource(R.drawable.baseline_arrow_back_24), contentDescription = "Back")
            }
        })
    }, snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { padding ->

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (savedItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No saved vehicles")
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            items(savedItems) { s ->
                val id = s["_id"] as? String ?: ""
                val vehicleId = s["vehicleId"] as? String ?: ""
                val name = s["vehicleName"] as? String ?: "Vehicle"
                val price = s["pricePerDay"]?.toString() ?: "0"
                val imageUrl = s["imageUrl"] as? String ?: ""

                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        if (imageUrl.isNotBlank()) {
                            AsyncImage(model = imageUrl, contentDescription = name, modifier = Modifier.size(72.dp))
                        } else {
                            Image(painter = painterResource(R.drawable.thar), contentDescription = name, modifier = Modifier.size(72.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.titleMedium)
                            Text("₹$price / day", color = MaterialTheme.colorScheme.primary)
                        }

                        Column {
                            Button(onClick = {
                                // Book directly from saved
                                val intent = Intent(ctx, VehicleDetailActivity::class.java).apply {
                                    putExtra("vehicleId", vehicleId)
                                    putExtra("vehicleName", name)
                                    putExtra("vehiclePrice", (s["pricePerDay"] as? Double) ?: 0.0)
                                    putExtra("vehicleImageUrl", imageUrl)
                                }
                                ctx.startActivity(intent)
                            }) { Text("View") }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(onClick = {
                                // Unsave
                                db.collection("savedVehicles").document(id).delete()
                                    .addOnSuccessListener { coroutineScope.launch { snackbarHostState.showSnackbar("Removed from saved") } }
                                    .addOnFailureListener { e -> coroutineScope.launch { snackbarHostState.showSnackbar("Remove failed: ${e.message}") } }
                            }) { Text("Remove") }
                        }
                    }
                }
            }
        }
    }
}
