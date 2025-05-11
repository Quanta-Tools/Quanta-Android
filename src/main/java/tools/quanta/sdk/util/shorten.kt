package tools.quanta.sdk.util

import android.util.Base64
import java.util.UUID

public fun shortenUuid(uuid: UUID): String {
    val uuidBytes = ByteArray(16)
    val bb = java.nio.ByteBuffer.wrap(uuidBytes)
    bb.putLong(uuid.mostSignificantBits)
    bb.putLong(uuid.leastSignificantBits)

    // Using Android's Base64 utility which is available since API 1
    // FLAG_URL_SAFE provides the URL-safe encoding (using - and _ instead of + and /)
    // FLAG_NO_PADDING removes the padding (=) characters
    return Base64.encodeToString(uuidBytes, Base64.URL_SAFE or Base64.NO_PADDING)
}

public fun shorten(anyId: String): String {
    return try {
        val parsedUuid = UUID.fromString(anyId)
        shortenUuid(parsedUuid)
    } catch (e: IllegalArgumentException) {
        anyId
    }
}
