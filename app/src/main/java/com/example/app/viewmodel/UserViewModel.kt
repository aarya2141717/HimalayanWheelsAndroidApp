package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.app.model.UserModel
import com.example.app.repository.UserRepo
import com.example.app.repository.UserRepoImpl

class UserViewModel : ViewModel() {

    private val repo: UserRepo = UserRepoImpl()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // 🔐 LOGIN
    fun login(
        email: String,
        password: String,
        callback: (Boolean, String, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val uid = auth.currentUser!!.uid

                    db.collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener { doc ->
                            val role = doc.getString("role") ?: "USER"
                            callback(true, "Login successful", role)
                        }
                        .addOnFailureListener {
                            callback(false, "Failed to fetch role", null)
                        }

                } else {
                    callback(false, task.exception?.message ?: "Login failed", null)
                }
            }
    }

    // 📝 REGISTER
    fun register(
        email: String,
        password: String,
        name: String,
        role: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val uid = auth.currentUser!!.uid

                    val userData = hashMapOf(
                        "uid" to uid,
                        "name" to name,
                        "email" to email,
                        "role" to role
                    )

                    db.collection("users")
                        .document(uid)
                        .set(userData)
                        .addOnSuccessListener {
                            callback(true, "Registration successful")
                        }
                        .addOnFailureListener {
                            callback(false, "Failed to save user data")
                        }

                } else {
                    callback(false, task.exception?.message ?: "Registration failed")
                }
            }
    }

    fun forgotPassword(email: String, callback: (Boolean, String) -> Unit) {
        repo.forgotPassword(email, callback)
    }

    fun getUserDetails(callback: (UserModel?) -> Unit) {
        val currentUser = repo.getCurrentUser()
        if (currentUser != null) {
            repo.getUserDetails(currentUser.uid, callback)
        } else {
            callback(null)
        }
    }
}
