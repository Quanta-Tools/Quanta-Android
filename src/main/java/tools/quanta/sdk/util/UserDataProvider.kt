package tools.quanta.sdk.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import java.util.Locale

private const val RECORD_SEPARATOR = "\u001E"

class UserDataProvider(
        private val context: Context,
        private val logger: QuantaLogger // Added QuantaLogger instance
) {

    private val installDate: String by lazy {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            (packageInfo.firstInstallTime / 1000).toInt().toString()
        } catch (e: Exception) {
            logger.e("UserDataProvider: Failed to get install date", e) // Use logger instance
            "" // Return empty string on error
        }
    }

    private fun getDeviceInfo(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    private fun getOSInfoSafe(): String {
        return "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
    }

    private fun getBundleId(): String {
        return context.packageName
    }

    private fun getAppVersion(): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        } catch (e: Exception) {
            logger.e("UserDataProvider: Failed to get app version", e) // Use logger instance
            "" // Return empty string on error
        }
    }

    private fun getSystemLanguage(): String {
        // Handles locales like "en-US" by converting to "en_US"
        // and "en" by leaving it as "en"
        return Locale.getDefault().toLanguageTag().replace("-", "_")
    }

    private object DebugFlags {
        const val IS_DEBUG = 1
        const val IS_SIMULATOR = 2
        const val IS_INTERNAL_APP_SHARING =
                4 // Google Play Internal App Sharing (via installer package name)
    }

    private fun isAppDebuggable(): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic", ignoreCase = true) ||
                Build.FINGERPRINT.startsWith("unknown", ignoreCase = true) ||
                Build.MODEL.contains("google_sdk", ignoreCase = true) ||
                Build.MODEL.contains("Emulator", ignoreCase = true) ||
                Build.MODEL.contains("Android SDK built for x86", ignoreCase = true) ||
                Build.MANUFACTURER.contains("Genymotion", ignoreCase = true) ||
                (Build.BRAND.startsWith("generic", ignoreCase = true) &&
                        Build.DEVICE.startsWith("generic", ignoreCase = true)) ||
                Build.PRODUCT.equals("google_sdk", ignoreCase = true) ||
                Build.PRODUCT.equals("sdk_gphone_x86", ignoreCase = true) ||
                Build.PRODUCT.equals("sdk_google_phone_x86", ignoreCase = true) ||
                Build.PRODUCT.equals(
                        "sdk_gphone64_x86_64",
                        ignoreCase = true
                ) // For newer emulators
        )
    }

    private fun isInternalAppSharingBuild(): Boolean {
        return try {
            val installerPackageName =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val installSourceInfo =
                                context.packageManager.getInstallSourceInfo(context.packageName)
                        installSourceInfo.installingPackageName
                    } else {
                        @Suppress("DEPRECATION")
                        context.packageManager.getInstallerPackageName(context.packageName)
                    }
            "com.google.android.apps.internalappsharing" == installerPackageName
        } catch (e: Exception) {
            logger.e("UserDataProvider: Failed to get installer package name", e)
            false
        }
    }

    private fun getDebugFlagsValue(): Int {
        var flags = 0
        if (isAppDebuggable()) {
            flags = flags or DebugFlags.IS_DEBUG
        }
        if (isEmulator()) {
            flags = flags or DebugFlags.IS_SIMULATOR
        }
        if (isInternalAppSharingBuild()) { // Check for Internal App Sharing (installer based)
            flags = flags or DebugFlags.IS_INTERNAL_APP_SHARING
        }
        return flags
    }

    private fun safe(value: String?): String {
        return value?.replace(RECORD_SEPARATOR, "")?.trim() ?: ""
    }

    fun getUserData(): String {
        val device = getDeviceInfo()
        val os = getOSInfoSafe()
        val bundleId = getBundleId()
        val debugFlags = getDebugFlagsValue()
        val version = getAppVersion()
        val language = getSystemLanguage()

        return StringBuilder()
                .apply {
                    append(safe(device))
                    append(RECORD_SEPARATOR)
                    append(safe(os))
                    append(RECORD_SEPARATOR)
                    append(safe(bundleId))
                    append(RECORD_SEPARATOR)
                    append(debugFlags) // Appends integer as string
                    append(RECORD_SEPARATOR)
                    append(safe(version))
                    append(RECORD_SEPARATOR)
                    append(safe(language))
                    append(RECORD_SEPARATOR)
                    append(safe(installDate))
                }
                .toString()
    }
}
