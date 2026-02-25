package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.AppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

class BookingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                BookingsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen() {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var bookings by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    LaunchedEffect(user?.uid) {
        if (user == null) {
            Toast.makeText(ctx, "Please login to see bookings", Toast.LENGTH_SHORT).show()
            loading = false
            return@LaunchedEffect
        }

        db.collection("bookings")
            .whereEqualTo("userId", user.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap: com.google.firebase.firestore.QuerySnapshot?, err: com.google.firebase.firestore.FirebaseFirestoreException? ->
                if (err != null) {
                    err.printStackTrace()
                    loading = false
                    return@addSnapshotListener
                }
                bookings = snap?.documents?.mapNotNull { doc: com.google.firebase.firestore.DocumentSnapshot ->
                    val map = doc.data ?: return@mapNotNull null
                    // ensure Map<String, Any> shape
                    @Suppress("UNCHECKED_CAST")
                    (map as? Map<String, Any>)?.plus(mapOf("_id" to doc.id))
                } ?: emptyList()
                loading = false
            }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("My Bookings") }, navigationIcon = {
            IconButton(onClick = {
                // back to dashboard
                ctx.startActivity(Intent(ctx, DashboardActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP })
            }) {
                Icon(painter = painterResource(R.drawable.baseline_arrow_back_24), contentDescription = "Back")
            }
        })
    }, snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Box
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (bookings.isEmpty()) {
                    item { Text("No bookings found", style = MaterialTheme.typography.bodyLarge) }
                } else {
                    items(bookings) { b ->
                        val id = b["_id"] as? String ?: ""
                        val vName = b["vehicleName"] as? String ?: "Vehicle"
                        val days = (b["days"] as? Long)?.toInt() ?: (b["days"] as? Double)?.toInt() ?: 0
                        val totalPrice = b["totalPrice"]?.toString() ?: "0"
                        val status = b["status"] as? String ?: ""

                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = vName, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Days: $days")
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Total: ₹$totalPrice")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Status: $status")

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    if (status == "PENDING") {
                                        Button(onClick = {
                                            // open edit screen
                                            ctx.startActivity(Intent(ctx, EditBookingActivity::class.java).apply {
                                                putExtra("bookingId", id)
                                            })
                                        }) { Text("Edit") }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(onClick = {
                                            // Cancel booking while pending
                                            db.collection("bookings").document(id).update(mapOf("status" to "CANCELLED", "companyApproved" to false, "adminApproved" to false))
                                                .addOnSuccessListener {
                                                    coroutineScope.launch { snackbarHostState.showSnackbar("Booking cancelled") }
                                                }
                                                .addOnFailureListener { e ->
                                                    coroutineScope.launch { snackbarHostState.showSnackbar("Cancel failed: ${e.message}") }
                                                }
                                        }) { Text("Cancel") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
