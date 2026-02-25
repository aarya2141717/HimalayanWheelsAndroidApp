package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.AppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row

class VendorBookingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                VendorBookingsScreen()
            }
        }
    }
}

@Composable
fun VendorBookingsScreen() {
    val db = FirebaseFirestore.getInstance()
    val vendorId = FirebaseAuth.getInstance().currentUser?.uid
    var loading by remember { mutableStateOf(true) }
    var bookings by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    LaunchedEffect(vendorId) {
        if (vendorId == null) {
            loading = false
            return@LaunchedEffect
        }
        db.collection("bookings")
            .whereEqualTo("vendorId", vendorId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    loading = false
                    return@addSnapshotListener
                }
                bookings = snap?.documents?.mapNotNull { doc -> doc.data?.plus(mapOf("_id" to doc.id)) } ?: emptyList()
                loading = false
            }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (bookings.isEmpty()) {
            item { Text("No bookings found", style = MaterialTheme.typography.bodyLarge) }
        } else {
            items(bookings) { b ->
                val id = b["_id"] as? String ?: ""
                val vName = b["vehicleName"] as? String ?: "Vehicle"
                val userName = b["userName"] as? String ?: "User"
                val status = b["status"] as? String ?: ""

                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = vName, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Requested by: $userName")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Status: $status")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            // Display approve/reject buttons via Firestore updates similar to AdminDashboard
                            val dbRef = FirebaseFirestore.getInstance()
                            Button(onClick = {
                                // company approve
                                dbRef.collection("bookings").document(id).update(mapOf("companyApproved" to true)).addOnSuccessListener {
                                    // if admin already approved, finalize
                                    dbRef.collection("bookings").document(id).get().addOnSuccessListener { snap ->
                                        val adminApproved = snap.getBoolean("adminApproved") ?: false
                                        if (adminApproved) {
                                            finalizeBooking(id, snap.getString("vehicleId") ?: "")
                                        }
                                    }
                                }
                            }) { Text("Approve") }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(onClick = {
                                dbRef.collection("bookings").document(id).update(mapOf("companyApproved" to false, "status" to "REJECTED"))
                            }) { Text("Reject") }
                        }
                    }
                }
            }
        }
    }
}
