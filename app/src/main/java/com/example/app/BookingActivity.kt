package com.example.app

import android.widget.Toast
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.AppTheme
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
class BookingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vehicleId = intent.getStringExtra("vehicleId") ?: ""
        val vehicleName = intent.getStringExtra("vehicleName") ?: "Vehicle"
        val vehiclePrice = intent.getDoubleExtra("vehiclePrice", 0.0)

        setContent {
            AppTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Book: $vehicleName") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(painter = painterResource(R.drawable.baseline_arrow_back_24), contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    BookingScreen(vehicleId, vehicleName, vehiclePrice, onComplete = { success -> if (success) finish() }, modifier = Modifier.padding(paddingValues))
                }
            }
        }
    }
}

@Composable
fun BookingScreen(vehicleId: String, vehicleName: String, price: Double, onComplete: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val db = FirebaseFirestore.getInstance()
    val ctx = LocalContext.current

    var daysText by remember { mutableStateOf("") }
    val days = daysText.toIntOrNull() ?: 0
    val totalPrice = days * price
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Booking: $vehicleName", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Price per day: ₹${price}")
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = daysText,
            onValueChange = { input ->
                // allow only numbers
                if (input.all { it.isDigit() }) daysText = input
            },
            label = { Text("Number of days") },
            placeholder = { Text("e.g. 3") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text("Total: ₹${"%.2f".format(totalPrice)}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (days <= 0) {
                Toast.makeText(ctx, "Enter number of days", Toast.LENGTH_SHORT).show()
                return@Button
            }

            isLoading = true

            // fetch vehicle to find vendorId and availability
            val vehicleRef = db.collection("vehicles").document(vehicleId)
            vehicleRef.get()
                .addOnSuccessListener { snap ->
                    val vendorId = snap.getString("vendorId") ?: ""
                    val availableCount = snap.getLong("totalCount")?.toInt() ?: 0
                    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    if (user == null) {
                        Toast.makeText(ctx, "Please login to book", Toast.LENGTH_SHORT).show()
                        isLoading = false
                        onComplete(false)
                        return@addOnSuccessListener
                    }

                    if (availableCount <= 0) {
                        Toast.makeText(ctx, "Vehicle unavailable", Toast.LENGTH_SHORT).show()
                        isLoading = false
                        onComplete(false)
                        return@addOnSuccessListener
                    }

                    val userName = user.displayName ?: user.email ?: user.uid

                    val booking = hashMapOf(
                        "vehicleId" to vehicleId,
                        "vendorId" to vendorId,
                        "userId" to user.uid,
                        "userName" to userName,
                        "vehicleName" to vehicleName,
                        "pricePerDay" to price,
                        "days" to days,
                        "totalPrice" to totalPrice,
                        "status" to "PENDING",
                        "companyApproved" to false,
                        "adminApproved" to false,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )

                    db.collection("bookings").add(booking)
                        .addOnSuccessListener { _ ->
                            isLoading = false
                            Toast.makeText(ctx, "Booking request sent. Awaiting approvals.", Toast.LENGTH_SHORT).show()
                            onComplete(true)
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                            isLoading = false
                            Toast.makeText(ctx, "Booking failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            onComplete(false)
                        }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    isLoading = false
                    Toast.makeText(ctx, "Unable to fetch vehicle: ${e.message}", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }

        }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Request Booking")
        }
    }
}
