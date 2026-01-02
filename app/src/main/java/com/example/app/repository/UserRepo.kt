package com.example.app.repository

import com.example.app.model.UserModel
import com.google.firebase.auth.FirebaseUser

interface UserRepo {
    fun login(email: String, password: String, callback: (Boolean, String) -> Unit)
    fun register(email: String, password: String, name: String, callback: (Boolean, String, String?) -> Unit)
    fun forgotPassword(email: String, callback: (Boolean, String) -> Unit)
    fun addUserToDatabase(userId: String, userModel: UserModel, callback: (Boolean, String) -> Unit)
    fun getCurrentUser(): FirebaseUser?
    fun getUserDetails(userId: String, callback: (UserModel?) -> Unit)
}
