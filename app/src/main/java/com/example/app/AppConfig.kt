package com.example.app

// Safe defaults for development. Override by setting local.properties and BuildConfig fields later.
object AppConfig {
    const val CLOUDINARY_CLOUD_NAME: String = "drp3b3nlj"
    const val CLOUDINARY_UPLOAD_PRESET: String = "vehicle"

    // Default admin credentials for local testing — change in local.properties or in Firebase later.
    const val ADMIN_EMAIL: String = "admin@himalayanwheels.local"
    const val ADMIN_PASS: String = "Admin@123"
}
