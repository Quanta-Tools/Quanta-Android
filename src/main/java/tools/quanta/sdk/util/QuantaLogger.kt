package tools.quanta.sdk.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import tools.quanta.sdk.config.ConfigReader

class QuantaLogger(context: Context, xmlResourceId: Int) {

    private val configReader: ConfigReader = ConfigReader(context, xmlResourceId)
    private val isDebugModeEnabled: Boolean =
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    private val isLogEnabledInConfig: Boolean = configReader.getBoolean("Log", false)

    private val shouldLog: Boolean = isDebugModeEnabled || isLogEnabledInConfig
    private val tag: String = "Quanta"

    fun log(message: String, throwable: Throwable? = null) {
        if (!shouldLog) return

        if (throwable != null) {
            Log.d(tag, message, throwable)
        } else {
            Log.d(tag, message)
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (!shouldLog) return

        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }

    fun w(message: String, throwable: Throwable? = null) {
        if (!shouldLog) return

        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
    }
}
