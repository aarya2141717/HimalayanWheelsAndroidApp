package com.example.app.repository

import com.example.app.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

class UserRepoImpl : UserRepo {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { 
                callback(true, "Login Successful") 
            }
            .addOnFailureListener { e ->
                callback(false, e.message ?: "Login Failed")
            }
    }

    override fun register(email: String, password: String, name: String, callback: (Boolean, String, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user == null) {
                    callback(false, "Registration failed: User is null.", null)
                    return@addOnSuccessListener
                }

                // Set the display name in the Auth profile
                val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                user.updateProfile(profileUpdates).addOnCompleteListener { 
                     // Only after profile update is attempted do we return success.
                     // This ensures that when the user is logged in immediately after, the displayName is available.
                     callback(true, "Registration Successful", user.uid)
                }
            }
            .addOnFailureListener { exception ->
                callback(false, exception.message ?: "Registration Failed", null)
            }
    }

    override fun forgotPassword(email: String, callback: (Boolean, String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { 
                callback(true, "Reset link sent to your email") 
            }
            .addOnFailureListener { e ->
                callback(false, e.message ?: "Error")
            }
    }

    override fun addUserToDatabase(userId: String, userModel: UserModel, callback: (Boolean, String) -> Unit) {
        database.reference.child("users").child(userId).setValue(userModel)
            .addOnSuccessListener { 
                callback(true, "User added to database") 
            }
            .addOnFailureListener { e ->
                callback(false, e.message ?: "Database Error")
            }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    override fun getUserDetails(userId: String, callback: (UserModel?) -> Unit) {
        database.reference.child("users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(UserModel::class.java)
                if (user != null && user.name.isNotEmpty()) {
                    callback(user)
                } else {
                    // Fallback to Auth Display Name if DB name is missing/empty
                    val authUser = auth.currentUser
                    val displayName = if (authUser != null && authUser.uid == userId) authUser.displayName else ""
                    
                    // Return user model with the name from Auth if possible
                    val finalUser = user?.copy(name = displayName ?: "") ?: UserModel(userId = userId, name = displayName ?: "")
                    callback(finalUser)
                }
            }
            .addOnFailureListener {
                // Fallback to Auth Display Name on failure
                val authUser = auth.currentUser
                if (authUser != null && authUser.uid == userId) {
                    val displayName = authUser.displayName ?: ""
                    callback(UserModel(userId, authUser.email ?: "", displayName))
                } else {
                    callback(null)
                }
            }
    }
}
