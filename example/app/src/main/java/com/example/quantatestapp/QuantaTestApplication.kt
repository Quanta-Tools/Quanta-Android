package com.example.quantatestapp

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tools.quanta.sdk.Quanta
import tools.quanta.sdk.util.QuantaLogger

class QuantaTestApplication : Application() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Enable debug logging
        QuantaLogger.isDebugModeEnabled = true

        // Initialize Quanta SDK
        scope.launch {
            try {
                Quanta.initialize(applicationContext)
                QuantaLogger.i("Quanta SDK initialized successfully in Application class")
            } catch (e: Exception) {
                QuantaLogger.e("Failed to initialize Quanta SDK", e)
            }
        }
    }
}
