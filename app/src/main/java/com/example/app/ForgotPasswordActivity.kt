package com.example.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.clip
import com.example.app.ui.theme.AppTheme
import com.example.app.viewmodel.UserViewModel

class ForgotPasswordActivity : ComponentActivity() {

    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                ForgotPasswordScreen(
                    onBack = { finish() },
                    onSendReset = { email, stopLoading ->
                        if (email.isBlank()) {
                            stopLoading()
                            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                            return@ForgotPasswordScreen
                        }

                        viewModel.forgotPassword(email) { success, message ->
                            stopLoading()
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            if (success) finish()
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(onBack: () -> Unit, onSendReset: (String, () -> Unit) -> Unit) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FC)), horizontalAlignment = Alignment.CenterHorizontally) {

        TopAppBar(
            title = { Text("Forgot Password") }
        )

        Spacer(modifier = Modifier.height(18.dp))

        Surface(
            modifier = Modifier.size(140.dp),
            shape = CircleShape,
            color = Color.White
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Enter the email associated with your account and we'll send a password reset link.")

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                if (!isLoading) {
                    isLoading = true
                    onSendReset(email) { isLoading = false }
                }
            }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Send Reset Email")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onBack) { Text("Back to login") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordPreview() {
    AppTheme { ForgotPasswordScreen(onBack = {}, onSendReset = { _, stop -> stop() }) }
}