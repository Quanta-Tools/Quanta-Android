package tools.quanta.sdk.util

import android.util.Log
import tools.quanta.sdk.config.ConfigReader

/// This file does not send log events to the server.
/// Instead, it's used for printing out log messages
/// to the developer console during development.
/// For sending log events to the server, use the Quanta class.

object QuantaLogger {
    public var isDebugModeEnabled: Boolean = false
    private val tag: String = "Quanta"

    private fun shouldLog(): Boolean {
        return if (isDebugModeEnabled) {
            ConfigReader.getBoolean(key = ConfigReader.KEY_LOG_IN_DEBUG, defaultValue = true)
        } else {
            ConfigReader.getBoolean(key = ConfigReader.KEY_LOG_IN_PROD, defaultValue = false)
        }
    }

    fun log(message: String, throwable: Throwable? = null) {
        if (!shouldLog()) return

        if (throwable != null) {
            Log.d(tag, message, throwable)
        } else {
            Log.d(tag, message)
        }
    }

    fun i(message: String, throwable: Throwable? = null) {
        if (!shouldLog()) return

        if (throwable != null) {
            Log.i(tag, message, throwable)
        } else {
            Log.i(tag, message)
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        // always log errors

        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }

    fun w(message: String, throwable: Throwable? = null) {
        if (!shouldLog()) return

        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
    }
}
