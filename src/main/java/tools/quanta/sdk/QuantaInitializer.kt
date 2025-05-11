package tools.quanta.sdk

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.ApplicationInfo
import android.database.Cursor
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tools.quanta.sdk.util.QuantaLogger

/**
 * Initializes the Quanta SDK when the application starts. This ContentProvider ensures that Quanta
 * is initialized early in the application lifecycle.
 */
class QuantaInitializer : ContentProvider() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(): Boolean {
        val context = context
        if (context == null) {
            // logger is not initialized yet, so we println
            println("QuantaInitializer: Context is null, SDK cannot be initialized.")
            return false
        }

        val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        QuantaLogger.isDebugModeEnabled = isDebug

        // It's crucial that the application context is used for long-lived objects.
        val appContext = context.applicationContext
        scope.launch {
            if (appContext == null) {
                QuantaLogger.w(
                        "QuantaInitializer: ApplicationContext is null. Using context, but this might lead to memory leaks if not handled carefully elsewhere."
                )
                Quanta.initialize(context)
            } else {
                Quanta.initialize(appContext)
            }
        }
        QuantaLogger.i("Quanta SDK Initialization Launched via QuantaInitializer.")
        return true
    }

    override fun query(
            uri: Uri,
            projection: Array<String>?,
            selection: String?,
            selectionArgs: Array<String>?,
            sortOrder: String?
    ): Cursor? {
        // No query operations supported
        return null
    }

    override fun getType(uri: Uri): String? {
        // No type information needed
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // No insert operations supported
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        // No delete operations supported
        return 0
    }

    override fun update(
            uri: Uri,
            values: ContentValues?,
            selection: String?,
            selectionArgs: Array<String>?
    ): Int {
        // No update operations supported
        return 0
    }
}
