package tools.quanta.sdk

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.math.pow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tools.quanta.sdk.config.ConfigReader
import tools.quanta.sdk.model.EventTask
import tools.quanta.sdk.network.NetworkClient
import tools.quanta.sdk.storage.LocalStorageManager
import tools.quanta.sdk.util.QuantaLogger
import tools.quanta.sdk.util.UserDataProvider
import tools.quanta.sdk.util.getAbDict
import tools.quanta.sdk.util.getAbLetters
import tools.quanta.sdk.util.shorten
import tools.quanta.sdk.util.shortenUuid

private const val RECORD_SEPARATOR = "\u001E"
private const val UNIT_SEPARATOR = "\u001F"

/**
 * Manages the lifecycle of events, including logging, queuing, processing, and A/B test variant
 * handling. This class is responsible for initializing user and app identifiers, handling event
 * persistence, and communicating with the network client to send event data.
 *
 * This version of Quanta initializes its dependencies using an `initialize` method, rather than
 * requiring them as constructor parameters. The application should ensure that any necessary global
 * state (like Application Context) is available for the SDK to retrieve during initialization.
 */
object Quanta {
    private lateinit var context: Context
    private var xmlResourceId: Int = 0
    private lateinit var localStorageManager: LocalStorageManager
    private lateinit var networkClient: NetworkClient
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var userDataProvider: UserDataProvider

    private val _queue = mutableListOf<EventTask>()
    private var _isProcessing = false
    private val queueMutex = Mutex()
    private var _appId: String = ""
    private var _userId: String = ""
    private var _abJson: String? = null
    private var _abLetters: String = ""
    private var _abDict: Map<String, String> = emptyMap()
    private var initialized = false
    private val initializationMutex = Mutex()

    /**
     * Initializes the Quanta with the application context. This method should be called once,
     * preferably from a ContentProvider or Application class.
     */
    suspend fun initialize(appContext: Context) {
        ConfigReader.initialize(appContext)
        initializationMutex.withLock {
            if (initialized) {
                QuantaLogger.i("Quanta already initialized.")
                return
            }
            context = appContext
            xmlResourceId = retrieveDefaultXmlResourceIdInternal()
            localStorageManager = LocalStorageManager(context)
            networkClient = NetworkClient()
            userDataProvider = UserDataProvider(context, QuantaLogger)
            _appId = shorten(ConfigReader.getString("QuantaAppId") ?: "")
            initialized = true

            if (_appId.isEmpty()) {
                QuantaLogger.e(
                        "QuantaAppId is not set in config.xml. Please ensure it is configured for Quanta SDK to work."
                )
            } else {
                postAppIdInitialization()
            }
            _userId = generateUserId()
            _abJson = localStorageManager.getString("tools.quanta.ab")
            _abLetters = calcAbLetters()
            _abDict = calcAbDict()
            QuantaLogger.i("Quanta initialized successfully.")
        }
    }

    private fun retrieveDefaultXmlResourceIdInternal(): Int {
        return context.resources.getIdentifier("quanta_config", "xml", context.packageName)
    }

    private fun generateUserId(): String {
        val userId = localStorageManager.getString("tools.quanta.user.id")
        return if (userId.isNullOrEmpty()) {
            val newUserId = shortenUuid(UUID.randomUUID())
            localStorageManager.saveData("tools.quanta.user.id", newUserId)
            newUserId
        } else {
            userId
        }
    }

    /**
     * Sets or updates the user ID for event tracking and A/B testing. The provided ID is persisted
     * to local storage. If an empty string is provided, the user ID is not changed, and a warning
     * is logged.
     *
     * @param userId The new user ID to set.
     */
    public fun setUserId(userId: String) {
        if (userId.isNotEmpty()) {
            _userId = userId
            localStorageManager.saveData("tools.quanta.user.id", userId)
        } else {
            QuantaLogger.w("User ID cannot be empty. It will not be set.")
        }
    }

    /**
     * Retrieves the current user ID being used for event tracking.
     *
     * @return The current user ID as a String.
     */
    public fun getUserId(): String {
        return _userId
    }

    /**
     * Retrieves the A/B test variant letter for a given experiment name. Defaults to "A" if the
     * experiment name is not found or not configured.
     *
     * @param experimentName The name of the experiment.
     * @return The variant letter (e.g., "A", "B") or "A" by default.
     */
    public fun abTest(experimentName: String): String {
        return _abDict[experimentName.lowercase()] ?: "A"
    }

    private fun postAppIdInitialization() {
        loadQueue()

        scope.launch { processQueue() }

        if (!ConfigReader.getBoolean("SkipLaunchEvent", false)) {
            log("launch")
        }
    }

    /**
     * Logs an event with the specified details, including optional revenue and a map of custom
     * arguments. Events are queued and processed asynchronously.
     *
     * @param event The name of the event to log. Should be concise.
     * @param revenue The revenue amount associated with this event (e.g., for purchases).
     * @param addedArguments Optional. A map of custom key-value pairs to associate with the event.
     * Defaults to an empty map.
     * @param time Optional. The timestamp for when the event occurred. Defaults to the current
     * system time.
     */
    public fun log(
            event: String,
            revenue: Double,
            addedArguments: Map<String, String> = emptyMap(),
            time: Date = Date()
    ) {
        scope.launch { logInternal(event, revenue, addedArguments, time) }
    }

    /**
     * Logs an event with the specified details, including optional revenue and a map of custom
     * arguments. Events are queued and processed asynchronously.
     *
     * @param event The name of the event to log. Should be concise.
     * @param addedArguments Optional. A map of custom key-value pairs to associate with the event.
     * Defaults to an empty map.
     * @param time Optional. The timestamp for when the event occurred. Defaults to the current
     * system time.
     */
    public fun log(
            event: String,
            addedArguments: Map<String, String> = emptyMap(),
            time: Date = Date()
    ) {
        scope.launch { logInternal(event, 0.0, addedArguments, time) }
    }

    /**
     * Logs an event with the specified details, including optional revenue and a pre-formatted
     * string of custom arguments. Events are queued and processed asynchronously. This overload is
     * useful when arguments are already serialized.
     *
     * @param event The name of the event to log. Should be concise.
     * @param revenue Optional. The revenue amount associated with this event. Defaults to 0.0.
     * @param addedArguments Optional. A string containing custom arguments. Defaults to an empty
     * string.
     * @param time Optional. The timestamp for when the event occurred. Defaults to the current
     * system time.
     */
    public fun log(
            event: String,
            revenue: Double = 0.0,
            addedArguments: String = "",
            time: Date = Date()
    ) {
        scope.launch { logInternal(event, revenue, addedArguments, time) }
    }

    private suspend fun waitForInitialization(): Boolean {
        val startTime = System.currentTimeMillis()
        val timeoutMillis = 10000L // 10 seconds timeout

        while (!initialized && System.currentTimeMillis() - startTime < timeoutMillis) {
            delay(100) // Wait for 100ms before checking again
        }

        return initialized
    }

    private suspend fun logInternal(
            event: String,
            revenue: Double,
            addedArguments: Any, // Can be Map<String, String> or String
            time: Date
    ) {
        if (!initialized) {
            // Wait for initialization for up to 10 seconds
            if (!waitForInitialization()) {
                QuantaLogger.e(
                        "Quanta not initialized. Call initialize() first. Event logging aborted."
                )
                return
            }
        }

        if (_appId.isEmpty()) {
            QuantaLogger.e("QuantaAppId is empty. Event logging aborted.")
            return
        }

        // Proceed with logging logic using the (now hopefully non-empty) _appId.
        var mutableEvent = event
        if (mutableEvent.length > 200) {
            QuantaLogger.w(
                    "Event name is too long. Event name + args should be 200 characters or less. It will be truncated."
            )
            mutableEvent = mutableEvent.substring(0, 200)
        }

        var argString = ""
        when (addedArguments) {
            is String -> {
                argString = safe(addedArguments, true)
            }
            is Map<*, *> -> {
                // Ensure keys are strings and sort them
                val sortedKeys = addedArguments.keys.filterIsInstance<String>().sorted()

                for (key in sortedKeys) {
                    val value = addedArguments[key]
                    if (value is String) { // Ensure value is also a string
                        val safeKey = safe(key, false)
                        val safeValue = safe(value, false)
                        argString += "$safeKey$UNIT_SEPARATOR$safeValue$UNIT_SEPARATOR"
                    }
                }

                if (argString.isNotEmpty()) {
                    argString = argString.substring(0, argString.length - UNIT_SEPARATOR.length)
                }
            }
        }

        if (mutableEvent.length + argString.length > 200) {
            QuantaLogger.w(
                    "Added arguments are too long. Event name + args should be 200 characters or less. They will be truncated."
            )
            val remainingLength = 200 - mutableEvent.length
            argString =
                    if (remainingLength > 0) {
                        argString.substring(0, kotlin.math.min(argString.length, remainingLength))
                    } else {
                        "" // Event name itself is 200 chars, no space for args
                    }
        }

        val userData = userDataProvider.getUserData()
        val revenueString = stringForDouble(revenue)

        val eventTask =
                EventTask(
                        appId = _appId, // Use the potentially updated _appId
                        userData = userData,
                        event = safe(mutableEvent),
                        revenue = revenueString,
                        addedArguments = safe(argString, true),
                        userId = _userId,
                        time = time,
                        abLetters = _abLetters
                )
        addEvent(eventTask)
    }

    private fun calcAbLetters(): String {
        return getAbLetters(_abJson ?: "", _userId)
    }

    private fun calcAbDict(): Map<String, String> {
        return getAbDict(_abJson ?: "", _abLetters)
    }

    private fun safe(value: String?, isArgumentString: Boolean = false): String {
        if (value == null) return ""
        val cleanedValue = value.replace(RECORD_SEPARATOR, "")
        return if (isArgumentString) {
            cleanedValue.trim()
        } else {
            cleanedValue.replace(UNIT_SEPARATOR, "").trim()
        }
    }

    private fun stringForDouble(value: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("0.00", symbols)
        return df.format(value)
    }

    /**
     * Adds a pre-constructed [EventTask] to the internal queue for processing. This method is
     * typically used internally but can be public for advanced scenarios where an [EventTask] is
     * created manually. Triggers queue processing.
     *
     * @param event The [EventTask] object to be added to the queue.
     */
    fun addEvent(event: EventTask) {
        scope.launch {
            queueMutex.withLock {
                _queue.add(event)
                saveQueue()
            }
            processQueue()
        }
    }

    private suspend fun sendEventSuspend(event: EventTask): Boolean {
        return suspendCancellableCoroutine { continuation ->
            executeNetworkRequest(event) { success ->
                if (continuation.isActive) {
                    continuation.resume(success)
                }
            }
        }
    }

    private suspend fun processQueue() {
        queueMutex.withLock {
            if (_isProcessing || _queue.isEmpty()) {
                return@withLock
            }
            _isProcessing = true
        }

        var failures = 0

        while (true) {
            val eventToProcess = queueMutex.withLock { _queue.firstOrNull() } ?: break

            if (failures > 0) {
                val delayMillis = (1.5.pow(failures - 1) * 500).toLong()
                delay(delayMillis)
            }

            val success = sendEventSuspend(eventToProcess)

            val eventAgeHours =
                    (System.currentTimeMillis() - eventToProcess.time.time) / (1000 * 60 * 60)

            if (success || failures >= 27 || eventAgeHours > 48) {
                queueMutex.withLock {
                    _queue.removeFirstOrNull()
                    failures = 0
                    saveQueue()
                }
            } else {
                failures++
            }
            delay(100)
        }

        queueMutex.withLock { _isProcessing = false }
    }

    private fun saveQueue() {
        scope.launch {
            queueMutex.withLock {
                try {
                    val serializedQueue = Json.encodeToString(_queue)
                    localStorageManager.saveData("event_queue", serializedQueue)
                } catch (e: Exception) {
                    QuantaLogger.e("Error saving event queue: ${e.message}", e)
                }
            }
        }
    }

    private fun loadQueue() {
        scope.launch {
            queueMutex.withLock {
                val serializedQueue = localStorageManager.getString("event_queue")
                if (!serializedQueue.isNullOrEmpty()) {
                    try {
                        val deserializedQueue =
                                Json.decodeFromString<List<EventTask>>(serializedQueue)
                        _queue.clear()
                        _queue.addAll(deserializedQueue)
                    } catch (e: Exception) {
                        QuantaLogger.e("Error loading event queue: ${e.message}", e)
                    }
                }
            }
        }
    }

    private fun setAbString(abString: String) {
        localStorageManager.saveData("tools.quanta.ab", abString)
        _abJson = abString
        _abLetters = calcAbLetters()
        _abDict = calcAbDict()
    }

    private fun executeNetworkRequest(
            eventDetails: EventTask,
            completionHandler: (Boolean) -> Unit
    ) {
        scope.launch {
            var eventSentSuccessfully = false
            var connection: HttpURLConnection? = null
            try {
                val url = URL("https://analytics-ingress.quanta.tools/ee/")
                connection = url.openConnection() as? HttpURLConnection

                if (connection == null) {
                    QuantaLogger.e("Failed to send event: Could not establish HttpURLConnection.")
                } else {
                    var body = ""
                    body += eventDetails.appId
                    body += "$RECORD_SEPARATOR${eventDetails.time.time / 1000}"
                    body += "$RECORD_SEPARATOR${eventDetails.event}"
                    body += "$RECORD_SEPARATOR${eventDetails.revenue}"
                    body += "$RECORD_SEPARATOR${eventDetails.addedArguments}"
                    body += "$RECORD_SEPARATOR${eventDetails.userId}"
                    body += "$RECORD_SEPARATOR${eventDetails.userData}"
                    eventDetails.abLetters?.let { body += "$RECORD_SEPARATOR$it" }

                    val headers = mutableMapOf<String, String>()
                    headers["Content-Type"] = "text/plain"
                    localStorageManager.getString("tools.quanta.ab.version")?.let {
                        headers["X-AB-Version"] = it
                    }

                    connection.requestMethod = "POST"
                    connection.doOutput = true
                    headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000

                    OutputStreamWriter(connection.outputStream).use { writer ->
                        writer.write(body)
                        writer.flush()
                    }

                    val responseCode = connection.responseCode
                    if (responseCode in 200..299) {
                        try {
                            BufferedReader(InputStreamReader(connection.inputStream)).use { reader
                                ->
                                val responseText = reader.readText()
                                if (responseText.isNotEmpty()) {
                                    setAbString(responseText)
                                }
                            }
                            connection.headerFields["X-AB-Version"]?.firstOrNull()?.let {
                                    abVersionHeaderValue ->
                                localStorageManager.saveData(
                                        "tools.quanta.ab.version",
                                        abVersionHeaderValue
                                )
                            }
                            eventSentSuccessfully = true
                        } catch (responseParsingException: Exception) {
                            QuantaLogger.e(
                                    "Error parsing response: ${responseParsingException.message}",
                                    responseParsingException
                            )
                        }
                    } else {
                        QuantaLogger.w("Failed to send event. Response code: $responseCode")
                    }
                }
            } catch (networkOrSetupException: Exception) {
                QuantaLogger.e(
                        "Network or setup error during event sending: ${networkOrSetupException.message}",
                        networkOrSetupException
                )
            } finally {
                connection?.disconnect()
                completionHandler(eventSentSuccessfully)
            }
        }
    }
}
