package com.example.app

import android.app.Application
import android.util.Log
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = HashMap<String, String>()
        config["cloud_name"] = "drp3n3bij"
        MediaManager.init(this, config)

        // Global uncaught exception handler — logs uncaught exceptions to help debugging crashes
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("UncaughtException", "Uncaught exception in thread ${thread.name}", throwable)
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
