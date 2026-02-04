package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.AppTheme

class BookingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val demo = remember { listOf("Booking 1 - Thar", "Booking 2 - Royal Enfield") }
                BookingsScreen(demo)
            }
        }
    }
}

@Composable
fun BookingsScreen(bookings: List<String>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(bookings) { b ->
            Text(b, modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}
