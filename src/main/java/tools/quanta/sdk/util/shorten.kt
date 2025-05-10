import java.util.Base64
import java.util.UUID

public fun shortenUuid(uuid: UUID): String {
    val uuidBytes = ByteArray(16)
    val bb = java.nio.ByteBuffer.wrap(uuidBytes)
    bb.putLong(uuid.mostSignificantBits)
    bb.putLong(uuid.leastSignificantBits)

    return Base64.getUrlEncoder().withoutPadding().encodeToString(uuidBytes)
}

public fun shorten(anyId: String): String {
    return try {
        val parsedUuid = UUID.fromString(anyId)
        shortenUuid(parsedUuid)
    } catch (e: IllegalArgumentException) {
        anyId
    }
}
