package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.app.ui.theme.AppTheme
import com.example.app.viewmodel.UserViewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.imePadding

class SignUpActivity : ComponentActivity() {

    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {

                LoginScreen(
                    onLoginClick = { email: String, password: String, stopLoading: () -> Unit ->

                        if (email.isBlank() || password.isBlank()) {
                            stopLoading()
                            Toast.makeText(
                                this,
                                "Please enter email and password",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@LoginScreen
                        }

                        // Use AppConfig admin credentials (editable in local.properties later if desired)
                        // sanitize inputs and stored admin values to avoid formatting mismatches
                        val userEmailSanitized = email.trim().lowercase()
                        val userPassword = password.trim()

                        fun String.sanitizeAdminField(): String = this.trim().removeSurrounding("\"").lowercase()

                        val useAdminEmail = AppConfig.ADMIN_EMAIL.sanitizeAdminField()
                        val useAdminPass = AppConfig.ADMIN_PASS.trim().removeSurrounding("\"")

                        // Hardcoded admin shortcut
                        if (userEmailSanitized == useAdminEmail && userPassword == useAdminPass) {
                            stopLoading()
                            startActivity(Intent(this, AdminDashboardActivity::class.java))
                            finish()
                            return@LoginScreen
                        }

                        viewModel.login(email, password) { success, message, role ->
                            stopLoading()
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                            if (success) {
                                when (role) {
                                    "ADMIN" ->
                                        startActivity(
                                            Intent(this, AdminDashboardActivity::class.java)
                                        )
                                    "COMPANY" ->
                                        startActivity(
                                            Intent(this, CompanyDashboardActivity::class.java)
                                        )
                                    "VENDOR" ->
                                        startActivity(
                                            Intent(this, VendorDashboardActivity::class.java)
                                        )
                                    else ->
                                        startActivity(
                                            Intent(this, DashboardActivity::class.java)
                                        )
                                }
                                finish()
                            }
                        }
                    },

                    onSignupClick = {
                        startActivity(Intent(this, RegistrationActivity::class.java))
                    },

                    onForgotPasswordClick = {
                        startActivity(Intent(this, ForgotPasswordActivity::class.java))
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: (String, String, () -> Unit) -> Unit,
    onSignupClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        // Rounded logo at top center (bigger)
        Surface(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .shadow(10.dp, CircleShape),
            color = Color.White
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.padding(16.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Welcome to Himalayan Wheels", color = Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Find and rent vehicles quickly", color = Color.DarkGray.copy(alpha = 0.9f), fontSize = 14.sp)

        Spacer(modifier = Modifier.height(28.dp))

        // Full screen form area
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
        ) {
            val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.Black,
                cursorColor = Color.Black,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray,
                placeholderColor = Color.Gray
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_email_24), contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_lock_24), contentDescription = null) },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Color.Black)
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        onLoginClick(email.trim(), password) { isLoading = false }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text("Login")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onSignupClick) {
                    Text("Create account", color = Color.Black)
                }

                TextButton(onClick = onForgotPasswordClick) {
                    Text("Forgot password?", color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // footer
        Text("© Himalayan Wheels", color = Color.Gray.copy(alpha = 0.6f), fontSize = 12.sp)

        Spacer(modifier = Modifier.height(16.dp))
    }
}
