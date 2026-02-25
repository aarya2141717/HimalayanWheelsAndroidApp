package com.example.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.app.ui.theme.AppTheme
import com.google.firebase.firestore.FirebaseFirestore

class EditBookingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bookingId = intent.getStringExtra("bookingId") ?: ""
        setContent {
            AppTheme {
                EditBookingScreen(bookingId = bookingId, onDone = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookingScreen(bookingId: String, onDone: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val ctx = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var daysText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var pricePerDay by remember { mutableStateOf(0.0) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(bookingId) {
        if (bookingId.isBlank()) {
            loading = false
            return@LaunchedEffect
        }
        db.collection("bookings").document(bookingId).get().addOnSuccessListener { doc ->
            val d = doc
            val days = d.getLong("days")?.toInt() ?: d.getDouble("days")?.toInt() ?: 0
            daysText = if (days > 0) days.toString() else ""
            status = d.getString("status") ?: ""
            pricePerDay = d.getDouble("pricePerDay") ?: (d.getLong("pricePerDay")?.toDouble() ?: 0.0)
            loading = false
        }.addOnFailureListener {
            it.printStackTrace()
            loading = false
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Edit Booking") },
            navigationIcon = {
                IconButton(onClick = { onDone() }) {
                    Icon(painter = painterResource(R.drawable.baseline_arrow_back_24), contentDescription = "Back")
                }
            }
        )
    }) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(padding)) {

            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            if (status != "PENDING") {
                Text("Booking cannot be edited because its status is $status", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { onDone() }) { Text("Back") }
                return@Column
            }

            OutlinedTextField(
                value = daysText,
                onValueChange = { input -> if (input.all { it.isDigit() }) daysText = input },
                label = { Text("Number of days") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            val days = daysText.toIntOrNull() ?: 0
            Text("Price per day: ₹${"%.2f".format(pricePerDay)}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total: ₹${"%.2f".format(days * pricePerDay)}", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (days <= 0) {
                    Toast.makeText(ctx, "Enter number of days", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isSaving = true
                val updates = mapOf("days" to days, "totalPrice" to days * pricePerDay)
                db.collection("bookings").document(bookingId).update(updates)
                    .addOnSuccessListener {
                        isSaving = false
                        Toast.makeText(ctx, "Booking updated", Toast.LENGTH_SHORT).show()
                        onDone()
                    }
                    .addOnFailureListener { e ->
                        isSaving = false
                        Toast.makeText(ctx, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }

            }, enabled = !isSaving, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Save")
            }
        }
    }
}
