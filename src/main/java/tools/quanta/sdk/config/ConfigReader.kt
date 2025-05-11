package tools.quanta.sdk.config

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log

object ConfigReader {

    private lateinit var appContext: Context
    private var metaDataBundle: Bundle = Bundle() // Initialize with an empty Bundle
    private var initialized = false
    private val initializationLock = Any()

    const val KEY_APP_ID = "tools.quanta.AppId"
    const val KEY_LOG_IN_DEBUG = "tools.quanta.LogInDebug"
    const val KEY_LOG_IN_PROD = "tools.quanta.LogInProd"

    fun initialize(context: Context) {
        synchronized(initializationLock) {
            if (initialized) {
                return
            }
            appContext = context.applicationContext
            try {
                val appInfo =
                        appContext.packageManager.getApplicationInfo(
                                appContext.packageName,
                                PackageManager.GET_META_DATA
                        )
                // If appInfo.metaData is null, keep metaDataBundle as the empty Bundle
                appInfo.metaData?.let { metaDataBundle = it }
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e("ConfigReader", "Failed to load application info: ${e.message}", e)
                // metaDataBundle remains an empty Bundle
            }
            initialized = true
            Log.i("ConfigReader", "ConfigReader initialized.")
        }
    }

    private fun checkInitialized() {
        if (!initialized) {
            val errorMessage =
                    "ConfigReader not initialized. Call ConfigReader.initialize(context) first."
            Log.e("ConfigReader", errorMessage)
            throw IllegalStateException(errorMessage)
        }
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        checkInitialized()
        return metaDataBundle.getString(key, defaultValue)
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        checkInitialized()
        return metaDataBundle.getInt(key, defaultValue)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        checkInitialized()
        if (!metaDataBundle.containsKey(key)) {
            return defaultValue
        }
        val str = metaDataBundle.getString(key) ?: ""

        val char = str.lowercase().slice(0..0)
        return (char == "y" || char == "t")
    }
}
