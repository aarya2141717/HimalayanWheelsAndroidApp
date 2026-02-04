package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.AppTheme

class BookingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vehicleName = intent.getStringExtra("vehicleName") ?: "Vehicle"
        setContent {
            AppTheme {
                BookingScreen(vehicleName) { /* TODO: save booking to Firestore */ }
            }
        }
    }
}

@Composable
fun BookingScreen(vehicleName: String, onConfirm: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Booking: $vehicleName")
        Spacer(modifier = Modifier.height(12.dp))
        Text("Select days and confirm booking (demo UI)")
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onConfirm) { Text("Confirm Booking") }
    }
}
