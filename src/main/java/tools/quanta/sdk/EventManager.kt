package tools.quanta.sdk

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
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
import tools.quanta.sdk.model.EventTask
import tools.quanta.sdk.network.NetworkClient
import tools.quanta.sdk.storage.LocalStorageManager
import tools.quanta.sdk.util.QuantaLogger

private const val RECORD_SEPARATOR = "\u001E"
private const val UNIT_SEPARATOR = "\u001F"

class EventManager(
        private val localStorageManager: LocalStorageManager,
        private val networkClient: NetworkClient,
        private val context: Context,
        private val xmlResourceId: Int
) {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val logger = QuantaLogger(context, xmlResourceId)
    private val _queue = mutableListOf<EventTask>()
    private var _isProcessing = false
    private val queueMutex = Mutex()

    init {
        loadQueue()
        scope.launch { processQueue() }
    }

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
                    logger.e("Error saving event queue: ${e.message}", e)
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
                        logger.e("Error loading event queue: ${e.message}", e)
                    }
                }
            }
        }
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
                    logger.e("Failed to send event: Could not establish HttpURLConnection.")
                } else {
                    var body = ""
                    body += eventDetails.appId
                    body += "$RECORD_SEPARATOR${eventDetails.time.time / 1000}"
                    body += "$RECORD_SEPARATOR${eventDetails.event}"
                    body += "$RECORD_SEPARATOR${eventDetails.revenue}"
                    body += "$RECORD_SEPARATOR${eventDetails.addedArguments}"
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
                                    localStorageManager.saveData("tools.quanta.ab", responseText)
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
                            logger.e(
                                    "Error parsing response: ${responseParsingException.message}",
                                    responseParsingException
                            )
                        }
                    } else {
                        logger.w("Failed to send event. Response code: $responseCode")
                    }
                }
            } catch (networkOrSetupException: Exception) {
                logger.e(
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
