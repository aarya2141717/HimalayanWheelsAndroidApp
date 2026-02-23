package com.example.app

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
import org.json.JSONObject

// Added imports to support scrolling and IME padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding

// New imports for image preview and icons
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class AddVehicleActivity : ComponentActivity() {

    private val vm: VendorViewModel by viewModels()
    // make imageUri a mutable state so Compose can observe changes
    private var imageUri: Uri? by mutableStateOf(null)
    private lateinit var pickLauncher: ActivityResultLauncher<String>

    @Suppress("DEPRECATION") // SOFT_INPUT_ADJUST_RESIZE constant is deprecated but still functional across SDKs
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure the window resizes when soft keyboard is shown
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // Register Activity Result launcher for image picking using GetContent (safer, no permission required)
        pickLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) imageUri = uri
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

    @OptIn(ExperimentalMaterial3Api::class)
    private fun render(initialVehicle: VehicleModel?) {
        setContent {
            AppTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = { Text(if (initialVehicle == null) "Add Vehicle" else "Edit Vehicle") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { padding ->
                    AddVehicleScreen(
                        modifier = Modifier.padding(padding),
                        initialVehicle = initialVehicle,
                        previewUri = imageUri,
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
                                        val (url, debug) = uploadToCloudinary(imageUri!!)
                                        if (url.isNullOrBlank()) {
                                            coroutineScope.launch { snackbarHostState.showSnackbar("Image upload failed. ${debug ?: "unknown"}") }
                                            return@launch
                                        }
                                        updatedMap["imageUrl"] = url
                                        // show update image url in snackbar for debugging
                                        coroutineScope.launch { snackbarHostState.showSnackbar("New image: $url") }
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
                                    val uploadResult = imageUri?.let { uploadToCloudinary(it) }
                                    val imgUrl = uploadResult?.first
                                    val debug = uploadResult?.second
                                    if (imageUri != null && imgUrl.isNullOrBlank()) {
                                        // image was selected but upload failed — do not save with empty URL; show debug info
                                        snackbarHostState.showSnackbar("Image upload failed. ${debug ?: "unknown"}")
                                        return@launch
                                    }
                                    val finalVehicle = vehicle.copy(imageUrl = imgUrl ?: "")
                                    vm.addVehicle(finalVehicle) { ok, msg ->
                                        coroutineScope.launch {
                                            if (ok) {
                                                val displayMsg = if (!imgUrl.isNullOrBlank()) "Vehicle uploaded (image: ${imgUrl})" else "Vehicle uploaded"
                                                snackbarHostState.showSnackbar(displayMsg)
                                                // Read back the saved document to confirm what's stored in Firestore
                                                val docId = msg ?: ""
                                                if (docId.isNotBlank()) {
                                                    val db = FirebaseFirestore.getInstance()
                                                    db.collection("vehicles").document(docId).get().addOnSuccessListener { docSnapshot ->
                                                        val storedUrl = docSnapshot.getString("imageUrl") ?: ""
                                                        Log.d("AddVehicle", "Saved docId=$docId imageUrl=$storedUrl")
                                                        coroutineScope.launch { snackbarHostState.showSnackbar("Saved imageUrl: ${if (storedUrl.isNotBlank()) storedUrl else "(empty)"}") }
                                                        // finish after a short delay? we'll simply finish to return to dashboard
                                                        // finish() is called on the activity thread below via another coroutine
                                                    }.addOnFailureListener { e ->
                                                        Log.e("AddVehicle", "Failed to read saved doc $docId", e)
                                                        coroutineScope.launch { snackbarHostState.showSnackbar("Saved but failed to read doc: ${e.message}") }
                                                    }
                                                }
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
        // Use GetContent contract
        pickLauncher.launch("image/*")
    }

    // Return Pair(url, debug) where debug contains response body or error message for diagnostics
    private suspend fun uploadToCloudinary(uri: Uri): Pair<String?, String?> {
        return withContext(Dispatchers.IO) {
            try {
                val input: InputStream? = contentResolver.openInputStream(uri)
                val bytes = input?.readBytes()
                if (bytes == null) return@withContext Pair(null, "empty_input_stream")

                val client = OkHttpClient()
                // Try to detect mime type from content resolver for better compatibility with Cloudinary
                val detected = contentResolver.getType(uri) ?: "image/jpeg"
                val mediaType = detected.toMediaTypeOrNull()
                val filename = uri.lastPathSegment?.substringAfterLast('/') ?: "upload.jpg"
                val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file", filename,
                        bytes.toRequestBody(mediaType))
                    .addFormDataPart("upload_preset", AppConfig.CLOUDINARY_UPLOAD_PRESET)
                    .build()
                val req = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/${AppConfig.CLOUDINARY_CLOUD_NAME}/image/upload")
                    .post(requestBody)
                    .build()
                val resp = client.newCall(req).execute()
                val body = resp.body?.string()
                Log.d("AddVehicle", "Cloudinary upload response code=${resp.code} body=${body}")
                if (body.isNullOrBlank()) return@withContext Pair(null, "empty_response_body; code=${resp.code}")
                try {
                    val json = JSONObject(body)
                    val url = json.optString("secure_url", "")
                    if (url.isNotBlank()) return@withContext Pair(url, body)
                } catch (e: Exception) {
                    // fallback to regex
                    val url = Regex("\"secure_url\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groups?.get(1)?.value
                    if (!url.isNullOrBlank()) return@withContext Pair(url, body)
                }

                // no url found in body
                Pair(null, body)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("AddVehicle", "upload exception", e)
                Pair(null, e.message ?: "upload_exception")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(modifier: Modifier = Modifier, initialVehicle: VehicleModel? = null, previewUri: android.net.Uri? = null, onPick: () -> Unit, onSave: (String, String, String, String, String, String) -> Unit, isPickingImage: () -> Boolean = { false }) {
    var name by remember { mutableStateOf(initialVehicle?.name ?: "") }
    var type by remember { mutableStateOf(initialVehicle?.type ?: "Car") }
    var price by remember { mutableStateOf(if (initialVehicle != null) initialVehicle.pricePerDay.toString() else "") }
    var number by remember { mutableStateOf(initialVehicle?.vehicleNumber ?: "") }
    var count by remember { mutableStateOf(if (initialVehicle != null) initialVehicle.totalCount.toString() else "") }
    var desc by remember { mutableStateOf(initialVehicle?.description ?: "") }

    val types = listOf("Car", "Bike", "SUV")

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF7F9FC)).padding(16.dp).verticalScroll(rememberScrollState()).imePadding(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // preview area
        if (previewUri != null) {
            AsyncImage(model = previewUri, contentDescription = "Selected image", modifier = Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.Crop)
        } else if (!initialVehicle?.imageUrl.isNullOrBlank()) {
            AsyncImage(model = initialVehicle?.imageUrl, contentDescription = "Existing image", modifier = Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.Crop)
        }

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Vehicle Name") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.Black))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Type selection as simple chips
            types.forEach { t ->
                FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) })
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price per day") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.Black))

        OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("Vehicle Number (mandatory)") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.Black))

        OutlinedTextField(value = count, onValueChange = { count = it }, label = { Text("Total Count") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.Black))

        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.Black))

        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onPick, modifier = Modifier.weight(1f)) { Text(if (isPickingImage()) "Image selected" else "Pick Image") }
            Button(onClick = { onSave(name, type, price, number, count, desc) }, modifier = Modifier.weight(1f)) { Text(if (initialVehicle == null) "Upload Vehicle" else "Update Vehicle") }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Tip: Set total count. If it becomes 0 the vehicle will show as Unavailable.", fontSize = 12.sp, color = Color.Gray)
    }
}
