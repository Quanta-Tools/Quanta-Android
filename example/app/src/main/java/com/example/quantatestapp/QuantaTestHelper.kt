package com.example.quantatestapp

import tools.quanta.sdk.Quanta
import tools.quanta.sdk.util.QuantaLogger

/** Helper for Quanta SDK info and diagnostics */
object QuantaTestHelper {
    fun getUserId(): String {
        return try {
            Quanta.getUserId()
        } catch (e: Exception) {
            QuantaLogger.e("Failed to get user ID", e)
            "Unknown"
        }
    }

    fun isLogEnabled(): Boolean {
        return QuantaLogger.isDebugModeEnabled
    }

    fun abTest(experimentName: String): String {
        return try {
            Quanta.abTest(experimentName)
        } catch (e: Exception) {
            QuantaLogger.e("Failed to get A/B test variant", e)
            "Unknown"
        }
    }
}
