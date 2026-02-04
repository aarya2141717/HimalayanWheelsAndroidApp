package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

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
    fun fetchCurrentUserName(callback: (String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(null)
            return
        }
        val uid = user.uid
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: user.displayName ?: ""
                callback(name)
            }
            .addOnFailureListener {
                callback(null)
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
        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                callback(true, "Profile updated")
            }
            .addOnFailureListener { e ->
                callback(false, e.message ?: "Failed to update profile")
            }
    }

    // New helper: logout
    fun logout() {
        auth.signOut()
    }
}
