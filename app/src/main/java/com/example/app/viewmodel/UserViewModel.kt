package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import com.example.app.model.UserModel
import com.example.app.repository.UserRepo
import com.example.app.repository.UserRepoImpl

class UserViewModel : ViewModel() {
    private val repo: UserRepo = UserRepoImpl()

    fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        repo.login(email, password, callback)
    }

    fun register(email: String, password: String, name: String, callback: (Boolean, String) -> Unit) {
        repo.register(email, password, name) { success, message, userId ->
            // Immediately forward the result to the UI
            callback(success, message)
            
            if (success && userId != null) {
                // If registration was successful, start a background task to save user details to the database.
                // We do NOT block the UI waiting for this.
                val user = UserModel(userId, email, name)
                repo.addUserToDatabase(userId, user) { _, _ -> 
                    // This callback is now purely for logging/debugging if needed
                    // The user has already been welcomed.
                }
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
