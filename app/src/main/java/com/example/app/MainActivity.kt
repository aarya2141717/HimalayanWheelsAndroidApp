package com.example.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Redirect to SignUpActivity (which displays the LoginScreen)
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }
}
