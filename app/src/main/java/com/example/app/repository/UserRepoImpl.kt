package com.example.app.repository

import com.example.app.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class UserRepoImpl : UserRepo {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Login Successful")
                } else {
                    callback(false, task.exception?.message ?: "Login Failed")
                }
            }
    }

    override fun register(email: String, password: String, callback: (Boolean, String, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Registration Successful", task.result?.user?.uid)
                } else {
                    callback(false, task.exception?.message ?: "Registration Failed", null)
                }
            }
    }

    override fun forgotPassword(email: String, callback: (Boolean, String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Reset link sent to your email")
                } else {
                    callback(false, task.exception?.message ?: "Error")
                }
            }
    }

    override fun addUserToDatabase(userId: String, userModel: UserModel, callback: (Boolean, String) -> Unit) {
        database.reference.child("users").child(userId).setValue(userModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "User added to database")
                } else {
                    callback(false, task.exception?.message ?: "Database Error")
                }
            }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}
