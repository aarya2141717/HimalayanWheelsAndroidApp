package com.example.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.app.model.VehicleModel
import com.example.app.ui.theme.AppTheme
import com.example.app.viewmodel.VendorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.InputStream
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

class AddVehicleActivity : ComponentActivity() {

    private val vm: VendorViewModel by viewModels()
    private var imageUri: Uri? = null
    private lateinit var pickLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register Activity Result launcher for image picking
        pickLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                imageUri = intent?.data
            }
        }

        // Check if editing existing vehicle
        val vehicleId = intent.getStringExtra("vehicleId")
        if (!vehicleId.isNullOrBlank()) {
            // fetch document then render with initialVehicle
            val db = FirebaseFirestore.getInstance()
            db.collection("vehicles").document(vehicleId).get()
                .addOnSuccessListener { doc ->
                    val v = doc.toObject(VehicleModel::class.java)?.copy(id = doc.id)
                    render(v)
                }
                .addOnFailureListener {
                    // failed to fetch, render empty for safety
                    render(null)
                }
        } else {
            render(null)
        }
    }

    private fun render(initialVehicle: VehicleModel?) {
        setContent {
            AppTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = { TopAppBar(title = { Text(if (initialVehicle == null) "Add Vehicle" else "Edit Vehicle") }) }
                ) { padding ->
                    AddVehicleScreen(
                        modifier = Modifier.padding(padding),
                        initialVehicle = initialVehicle,
                        onPick = { pickImage() },
                        onSave = { name, type, price, number, count, desc ->
                            if (name.isBlank() || number.isBlank()) {
                                coroutineScope.launch { snackbarHostState.showSnackbar("Name and Vehicle number are mandatory") }
                                return@AddVehicleScreen
                            }

                            if (price.toDoubleOrNull() == null) {
                                coroutineScope.launch { snackbarHostState.showSnackbar("Enter a valid price") }
                                return@AddVehicleScreen
                            }

                            val vendorId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "vendor-uid-placeholder"

                            // If editing existing vehicle
                            if (initialVehicle != null) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Updating vehicle...")
                                    val updatedMap = mutableMapOf<String, Any>(
                                        "name" to name.trim(),
                                        "type" to type,
                                        "pricePerDay" to (price.toDoubleOrNull() ?: 0.0),
                                        "vehicleNumber" to number.trim(),
                                        "totalCount" to (count.toIntOrNull() ?: 1),
                                        "description" to desc.trim(),
                                        "available" to ((count.toIntOrNull() ?: 1) > 0)
                                    )

                                    if (imageUri != null) {
                                        // upload new image
                                        val url = uploadToCloudinary(imageUri!!)
                                        if (!url.isNullOrBlank()) updatedMap["imageUrl"] = url
                                    }

                                    vm.updateVehicle(initialVehicle.id, updatedMap) { ok, msg ->
                                        coroutineScope.launch {
                                            if (ok) {
                                                snackbarHostState.showSnackbar("Vehicle updated")
                                                finish()
                                            } else {
                                                snackbarHostState.showSnackbar("Update failed: ${msg ?: "unknown"}")
                                            }
                                        }
                                    }
                                }

                            } else {
                                // creating new vehicle
                                val vehicle = VehicleModel(
                                    vendorId = vendorId,
                                    name = name.trim(),
                                    type = type,
                                    pricePerDay = price.toDoubleOrNull() ?: 0.0,
                                    vehicleNumber = number.trim(),
                                    totalCount = count.toIntOrNull() ?: 1,
                                    description = desc.trim(),
                                    imageUrl = "",
                                    available = (count.toIntOrNull() ?: 1) > 0
                                )

                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Uploading image and saving vehicle...")
                                    val imgUrl = imageUri?.let { uploadToCloudinary(it) }
                                    val finalVehicle = vehicle.copy(imageUrl = imgUrl ?: "")
                                    vm.addVehicle(finalVehicle) { ok, msg ->
                                        coroutineScope.launch {
                                            if (ok) {
                                                snackbarHostState.showSnackbar("Vehicle uploaded")
                                                finish()
                                            } else {
                                                snackbarHostState.showSnackbar("Failed to save: ${msg ?: "unknown"}")
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        isPickingImage = { imageUri != null }
                    )
                }
            }
        }
    }

    private fun pickImage() {
        val i = Intent(Intent.ACTION_PICK)
        i.type = "image/*"
        pickLauncher.launch(i)
    }

    private suspend fun uploadToCloudinary(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val input: InputStream? = contentResolver.openInputStream(uri)
                val bytes = input?.readBytes()
                val client = OkHttpClient()
                val mediaType = "image/*".toMediaTypeOrNull()
                val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file", "upload.jpg",
                        bytes!!.toRequestBody(mediaType))
                    .addFormDataPart("upload_preset", AppConfig.CLOUDINARY_UPLOAD_PRESET)
                    .build()
                val req = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/${AppConfig.CLOUDINARY_CLOUD_NAME}/image/upload")
                    .post(requestBody)
                    .build()
                val resp = client.newCall(req).execute()
                val body = resp.body?.string()
                val url = Regex("\"secure_url\"\\s*:\\s*\"([^\"]+)\"").find(body ?: "")?.groups?.get(1)?.value
                url
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(modifier: Modifier = Modifier, initialVehicle: VehicleModel? = null, onPick: () -> Unit, onSave: (String, String, String, String, String, String) -> Unit, isPickingImage: () -> Boolean = { false }) {
    var name by remember { mutableStateOf(initialVehicle?.name ?: "") }
    var type by remember { mutableStateOf(initialVehicle?.type ?: "Car") }
    var price by remember { mutableStateOf(if (initialVehicle != null) initialVehicle.pricePerDay.toString() else "") }
    var number by remember { mutableStateOf(initialVehicle?.vehicleNumber ?: "") }
    var count by remember { mutableStateOf(if (initialVehicle != null) initialVehicle.totalCount.toString() else "") }
    var desc by remember { mutableStateOf(initialVehicle?.description ?: "") }

    val types = listOf("Car", "Bike", "SUV")

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF7F9FC)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Vehicle Name") }, modifier = Modifier.fillMaxWidth())

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Type selection as simple chips
            types.forEach { t ->
                FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) })
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price per day") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("Vehicle Number (mandatory)") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = count, onValueChange = { count = it }, label = { Text("Total Count") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onPick) { Text(if (isPickingImage()) "Image selected" else "Pick Image") }
            Button(onClick = { onSave(name, type, price, number, count, desc) }) { Text(if (initialVehicle == null) "Upload Vehicle" else "Update Vehicle") }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Tip: Set total count. If it becomes 0 the vehicle will show as Unavailable.", fontSize = 12.sp, color = Color.Gray)
    }
}
