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

    fun register(email: String, password: String, callback: (Boolean, String) -> Unit) {
        repo.register(email, password) { success, message, userId ->
            if (success && userId != null) {
                val user = UserModel(userId, email)
                repo.addUserToDatabase(userId, user) { dbSuccess, dbMessage ->
                     if (dbSuccess) {
                         callback(true, "Registration Successful")
                     } else {
                         callback(false, dbMessage)
                     }
                }
            } else {
                callback(false, message)
            }
        }
    }

    fun forgotPassword(email: String, callback: (Boolean, String) -> Unit) {
        repo.forgotPassword(email, callback)
    }
}
