package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Expose current user name as StateFlow so Compose can collect it and auto-update UI
    private val _currentUserName = MutableStateFlow("User")
    val currentUserName: StateFlow<String> get() = _currentUserName

    fun login(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser!!.uid
                db.collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val role = doc.getString("role") ?: "USER"
                        // update current name state
                        _currentUserName.value = doc.getString("name") ?: "User"
                        callback(true, "Login successful", role)
                    }
                    .addOnFailureListener {
                        callback(true, "Login successful", "USER")
                    }
            }
            .addOnFailureListener {
                callback(false, it.message ?: "Login failed", "")
            }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser!!.uid
                val userData = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "role" to role
                )

                db.collection("users").document(uid).set(userData)
                    .addOnSuccessListener {
                        // update current name
                        _currentUserName.value = name
                        callback(true, "Registration successful")
                    }
                    .addOnFailureListener {
                        callback(false, "User created but Firestore failed")
                    }
            }
            .addOnFailureListener {
                callback(false, it.message ?: "Registration failed")
            }
    }

    fun forgotPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                callback(true, "Reset email sent")
            }
            .addOnFailureListener {
                callback(false, it.message ?: "Failed")
            }
    }

    // New helper: fetch current user's display name (from Firestore)
    fun fetchCurrentUserName(callback: (String) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback("User") // fallback
            return
        }

        val uid = user.uid
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "User"
                _currentUserName.value = name
                callback(name)
            }
            .addOnFailureListener {
                callback("User") // fallback
            }
    }

    // New helper: update user's name in Firestore
    fun updateProfileName(newName: String, callback: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(false, "Not signed in")
            return
        }
        val uid = user.uid
        val updates = mapOf("name" to newName)
        // use set with merge to create the document if it does not exist (avoids No document to update)
        db.collection("users").document(uid).set(updates, SetOptions.merge())
            .addOnSuccessListener {
                _currentUserName.value = newName
                callback(true, "Profile updated")
            }
            .addOnFailureListener { e ->
                callback(false, e.message ?: "Failed to update profile")
            }
    }

    // New helper: logout
    fun logout() {
        auth.signOut()
        _currentUserName.value = "User"
    }
}
