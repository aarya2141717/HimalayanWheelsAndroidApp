package com.example.app

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.ui.theme.AppTheme
import com.example.app.viewmodel.UserViewModel
import androidx.compose.foundation.layout.imePadding

class RegistrationActivity : ComponentActivity() {

    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                RegistrationScreen(
                    onBackClick = { finish() },
                    onRegisterClick = { name, email, password, role, stopLoading ->

                        val trimmedEmail = email.trim()

                        if (name.isBlank() || trimmedEmail.isBlank() || password.isBlank()) {
                            stopLoading()
                            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@RegistrationScreen
                        }

                        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                            stopLoading()
                            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                            return@RegistrationScreen
                        }

                        if (password.length < 6) {
                            stopLoading()
                            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                            return@RegistrationScreen
                        }

                        // Ensure correct parameter order and trimmed email
                        viewModel.register(name, trimmedEmail, password, role) { success, message ->
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
fun RegistrationScreen(
    onBackClick: () -> Unit,
    onRegisterClick: (String, String, String, String, () -> Unit) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("USER") }

    // We'll use explicit textStyle on fields to ensure visibility across devices
    val fieldTextStyle = TextStyle(color = Color.Black)

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FC)).verticalScroll(rememberScrollState()).imePadding()) {

        TopAppBar(
            title = { Text("Create Account") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_arrow_back_24),
                        contentDescription = "Back"
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Larger rounded logo
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

            Spacer(modifier = Modifier.height(28.dp))

            Text("Create an account", fontSize = 22.sp, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                textStyle = fieldTextStyle
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                textStyle = fieldTextStyle
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            painter = painterResource(
                                if (showPassword)
                                    R.drawable.baseline_visibility_24
                                else
                                    R.drawable.baseline_visibility_off_24
                            ),
                            contentDescription = null
                        )
                    }
                },
                visualTransformation =
                if (showPassword) VisualTransformation.None
                else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                textStyle = fieldTextStyle
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Register as")

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedRole == "USER",
                    onClick = { selectedRole = "USER" }
                )
                Text("User")

                Spacer(modifier = Modifier.width(16.dp))

                RadioButton(
                    selected = selectedRole == "COMPANY",
                    onClick = { selectedRole = "COMPANY" }
                )
                Text("Company")
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (!isLoading) {
                        isLoading = true
                        onRegisterClick(name, email, password, selectedRole) {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(30.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Register")
                }
            }
        }
    }
}
