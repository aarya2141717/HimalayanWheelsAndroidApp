package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.util.Log
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.clickable

class SignUpActivity : ComponentActivity() {

    private val viewModel: UserViewModel by viewModels()

    // Toggle this to true during testing so successful login opens TestDashboardActivity
    // Set to false for normal behavior (opens real DashboardActivity)
    private val USE_TEST_DASHBOARD = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {

                // simplified lambda parameter names to avoid confusing typed-parameter lambda syntax
                LoginScreen(
                    onLoginClick = { email, password, stopLoading ->

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
                            Log.d("SignUpActivity", "Login callback: success=$success role=$role")

                            if (success) {
                                when (role) {
                                    "ADMIN" ->
                                        try {
                                            val adminIntent = Intent(this, AdminDashboardActivity::class.java)
                                            if (adminIntent.resolveActivity(packageManager) != null) {
                                                startActivity(adminIntent)
                                            } else {
                                                Log.e("SignUpActivity", "Admin activity not found in package manager")
                                                Toast.makeText(this, "Admin Dashboard not available", Toast.LENGTH_LONG).show()
                                            }
                                            // do not finish() immediately to allow safe debugging if the target activity crashes
                                        } catch (e: Exception) {
                                            Log.e("SignUpActivity", "Failed to open AdminDashboard", e)
                                            Toast.makeText(this, "Unable to open Admin Dashboard: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    "COMPANY" ->
                                        try {
                                            val cIntent = Intent(this, CompanyDashboardActivity::class.java)
                                            if (cIntent.resolveActivity(packageManager) != null) {
                                                startActivity(cIntent)
                                            } else {
                                                Log.e("SignUpActivity", "Company activity not found in package manager")
                                                Toast.makeText(this, "Company Dashboard not available", Toast.LENGTH_LONG).show()
                                            }
                                            // do not finish() immediately
                                        } catch (e: Exception) {
                                            Log.e("SignUpActivity", "Failed to open CompanyDashboard", e)
                                            Toast.makeText(this, "Unable to open Company Dashboard: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    "VENDOR" ->
                                        try {
                                            val vIntent = Intent(this, VendorDashboardActivity::class.java)
                                            if (vIntent.resolveActivity(packageManager) != null) {
                                                startActivity(vIntent)
                                            } else {
                                                Log.e("SignUpActivity", "Vendor activity not found in package manager")
                                                Toast.makeText(this, "Vendor Dashboard not available", Toast.LENGTH_LONG).show()
                                            }
                                            // do not finish() immediately
                                        } catch (e: Exception) {
                                            Log.e("SignUpActivity", "Failed to open VendorDashboard", e)
                                            Toast.makeText(this, "Unable to open Vendor Dashboard: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    else ->
                                        try {
                                            // During debugging we open a safe TestDashboardActivity to verify login flows
                                            val targetCls = if (USE_TEST_DASHBOARD) TestDashboardActivity::class.java else DashboardActivity::class.java
                                            val dIntent = Intent(this, targetCls)
                                            if (dIntent.resolveActivity(packageManager) != null) {
                                                startActivity(dIntent)
                                            } else {
                                                Log.e("SignUpActivity", "Target dashboard activity not found in package manager")
                                                Toast.makeText(this, "Dashboard not available", Toast.LENGTH_LONG).show()
                                            }
                                            // do not finish() immediately so the login screen remains if Dashboard crashes
                                        } catch (e: Exception) {
                                            Log.e("SignUpActivity", "Failed to open Dashboard", e)
                                            Toast.makeText(this, "Unable to open Dashboard: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                }
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

    val fieldTextStyle = TextStyle(color = Color.Black)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // FocusRequesters so Next moves focus correctly on real devices
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

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

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_email_24), contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocusRequester),
                shape = RoundedCornerShape(12.dp),
                textStyle = fieldTextStyle
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_lock_24), contentDescription = null) },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                shape = RoundedCornerShape(12.dp),
                textStyle = fieldTextStyle,
                trailingIcon = {
                    Text(
                        text = if (showPassword) "Hide" else "Show",
                        color = Color.Gray,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { showPassword = !showPassword }
                    )
                }
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
