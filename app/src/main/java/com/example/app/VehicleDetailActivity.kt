package com.example.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.AppTheme

class VehicleDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vehicleName = intent.getStringExtra("vehicleName") ?: "Vehicle"
        val vehicleImage = intent.getIntExtra("vehicleImage", R.drawable.thar)
        setContent {
            AppTheme {
                val activity = this@VehicleDetailActivity
                VehicleDetailScreen(vehicleName, vehicleImage) {
                    // start booking using captured activity reference
                    val intent = Intent(activity, BookingActivity::class.java).apply {
                        putExtra("vehicleName", vehicleName)
                    }
                    activity.startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun VehicleDetailScreen(name: String, image: Int, onBook: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Image(painter = painterResource(image), contentDescription = null, modifier = Modifier.fillMaxWidth().height(220.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(name, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Description: Comfortable and reliable vehicle for rent. Includes insurance and roadside assistance.")
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onBook, modifier = Modifier.fillMaxWidth()) {
            Text("Book Now")
        }
    }
}
