package tools.quanta.sdk

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tools.quanta.sdk.model.EventTask
import tools.quanta.sdk.network.NetworkClient
import tools.quanta.sdk.storage.LocalStorageManager

private const val RECORD_SEPARATOR = ""
private const val UNIT_SEPARATOR = "" // Not used in the provided snippet

class EventManager(
        private val localStorageManager: LocalStorageManager,
        private val networkClient: NetworkClient // Assuming NetworkClient has a post method
) {

    // It's good practice to allow injecting a CoroutineScope for testing
    private val scope = CoroutineScope(Dispatchers.IO)

    fun sendEvent(event: EventTask, callback: (Boolean) -> Unit) {
        scope.launch {
            val success =
                    try {
                        val url = "https://analytics-ingress.quanta.tools/ee/"

                        var body = ""
                        body += event.appId
                        body += "$RECORD_SEPARATOR${event.time.time / 1000}"
                        body += "$RECORD_SEPARATOR${event.event}"
                        body += "$RECORD_SEPARATOR${event.revenue}"
                        body += "$RECORD_SEPARATOR${event.addedArguments}"
                        body += "$RECORD_SEPARATOR${event.userData}"
                        event.abLetters?.let { body += "$RECORD_SEPARATOR$it" }

                        val headers = mutableMapOf<String, String>()
                        headers["Content-Type"] = "text/plain"

                        localStorageManager.getString("tools.quanta.ab.version")?.let {
                            headers["X-AB-Version"] = it
                        }

                        // Using HttpURLConnection directly as per NetworkClient's style,
                        // but ideally NetworkClient would handle this.
                        var connection: HttpURLConnection? = null
                        try {
                            val connectionUrl = URL(url)
                            connection = connectionUrl.openConnection() as HttpURLConnection
                            connection.requestMethod = "POST"
                            connection.doOutput = true
                            headers.forEach { (key, value) ->
                                connection.setRequestProperty(key, value)
                            }
                            connection.connectTimeout = 5000 // 5 seconds
                            connection.readTimeout = 5000 // 5 seconds

                            val writer = OutputStreamWriter(connection.outputStream)
                            writer.write(body)
                            writer.flush()
                            writer.close()

                            val responseCode = connection.responseCode
                            if (responseCode in 200..299) {
                                try {
                                    val reader =
                                            BufferedReader(
                                                    InputStreamReader(connection.inputStream)
                                            )
                                    val responseText = reader.readText()
                                    reader.close()

                                    if (responseText.isNotEmpty()) {
                                        localStorageManager.saveData(
                                                "tools.quanta.ab",
                                                responseText
                                        )
                                        // this.setAbJson(responseText); // Need to define how to
                                        // handle this
                                    }

                                    connection.headerFields["X-AB-Version"]?.firstOrNull()?.let {
                                            abVersionHeader ->
                                        localStorageManager.saveData(
                                                "tools.quanta.ab.version",
                                                abVersionHeader
                                        )
                                    }
                                } catch (e: Exception) {
                                    // Ignore parsing errors
                                    println("Error parsing response: ${e.message}")
                                }
                                true
                            } else {
                                println("Failed to send event. Response code: $responseCode")
                                false
                            }
                        } catch (e: Exception) {
                            println("Failed to send event: ${e.message}")
                            false
                        } finally {
                            connection?.disconnect()
                        }
                    } catch (error: Exception) {
                        println("Failed to send event: ${error.message}")
                        false
                    }
            callback(success)
        }
    }

    // Placeholder for debugError, adapt as needed
    private fun debugError(message: String, error: Throwable?) {
        println("$message ${error?.localizedMessage ?: ""}")
    }

    // Placeholder for setAbJson, adapt as needed
    // private fun setAbJson(json: String) {
    //     // Implement your logic to parse and use the AB test JSON
    // }
}
