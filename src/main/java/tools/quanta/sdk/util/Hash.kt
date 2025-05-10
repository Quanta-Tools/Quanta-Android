package tools.quanta.sdk.util

import java.security.MessageDigest

fun stringToNumber(input: String): Int {
    val md = MessageDigest.getInstance("MD5")
    val inputData = input.toByteArray(Charsets.UTF_8)
    val hashBytes = md.digest(inputData)

    // Take the first 4 bytes (prefix(4))
    val prefixBytes = hashBytes.copyOfRange(0, 4)

    // Convert to hex string (map { String(format: "%02x", $0) }.joined())
    val hexString = prefixBytes.joinToString("") { String.format("%02x", it) }

    // Convert hex string (from 4 bytes) to a number, then modulo 100.
    // The hex string is parsed as a positive Long to match Swift's behavior
    // where Int(hex, radix: 16) would parse into a type that can hold the unsigned 32-bit value.
    return (hexString.toLong(radix = 16) % 100).toInt()
}
