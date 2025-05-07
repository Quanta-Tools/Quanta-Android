package tools.quanta.sdk.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class NetworkClient {

    suspend fun get(urlString: String): String? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000 // 5 seconds
            connection.readTimeout = 5000    // 5 seconds

            if (connection.responseCode !in 200..299) {
                // Consider logging the error code: connection.responseCode
                return@withContext null
            }

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.readText()
            reader.close()
            return@withContext response
        } catch (e: IOException) {
            // Handle exceptions, e.g., log them
            e.printStackTrace()
            return@withContext null
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun post(urlString: String, jsonData: String): String? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 5000 // 5 seconds
            connection.readTimeout = 5000    // 5 seconds

            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(jsonData)
            writer.flush()
            writer.close()

            if (connection.responseCode !in 200..299) {
                 // Consider logging the error code: connection.responseCode
                return@withContext null
            }

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.readText()
            reader.close()
            return@withContext response
        } catch (e: IOException) {
            // Handle exceptions, e.g., log them
            e.printStackTrace()
            return@withContext null
        } finally {
            connection?.disconnect()
        }
    }
}
