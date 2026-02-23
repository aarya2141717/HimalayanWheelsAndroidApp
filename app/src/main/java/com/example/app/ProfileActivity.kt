package com.example.app

import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.app.ui.theme.AppTheme
import com.example.app.viewmodel.UserViewModel

class ProfileActivity : ComponentActivity() {

    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                ProfileScreen(
                    viewModel = viewModel,
                    onLogout = {
                        // sign out and redirect to SignUpActivity clearing back-stack
                        viewModel.logout()
                        val i = Intent(this@ProfileActivity, SignUpActivity::class.java)
                        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(i)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: UserViewModel, onLogout: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchCurrentUserName { fetched ->
            if (!fetched.isNullOrEmpty()) name = fetched
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FC)).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {

        Surface(modifier = Modifier.size(92.dp).background(Color.White, CircleShape), shape = CircleShape, color = Color.White) {
            Image(painter = painterResource(R.drawable.logo), contentDescription = null, modifier = Modifier.padding(12.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Profile", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            if (name.isBlank()) {
                Toast.makeText(context, "Name can't be empty", Toast.LENGTH_SHORT).show()
                return@Button
            }
            isSaving = true
            viewModel.updateProfileName(name) { success, message ->
                isSaving = false
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Save")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), onClick = onLogout, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Logout", color = Color.White)
        }
    }
}
