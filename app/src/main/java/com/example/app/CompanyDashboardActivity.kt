package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.AppTheme

class CompanyDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                CompanyDashboardScreen()
            }
        }
    }
}

@Composable
fun CompanyDashboardScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Company Dashboard", style = MaterialTheme.typography.headlineMedium)

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { /* Navigate to Add Vehicle */ }
        ) {
            Text("Add Vehicle")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { /* View My Vehicles */ }
        ) {
            Text("My Vehicles")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { /* View Bookings */ }
        ) {
            Text("View Bookings")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            onClick = { /* Logout */ }
        ) {
            Text("Logout")
        }
    }
}
