package com.saidi.busassistant.data.remote.crypto

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Cryptographic utility for Beijing Real-Time Bus API.
 * Reverse engineered from leavez/fucking-beijing-bus-api.
 * Uses RC4 symmetric stream cipher and MD5 key derivation: key = md5("aibang" + seed).
 */
object BeijingBusCrypto {

    /**
     * Computes lowercase 32-character hexadecimal MD5 hash.
     */
    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Decrypts Base64-encoded RC4 ciphertext.
     * @param cipherBase64 Base64 ciphertext
     * @param keySeed Key seed string (lineId for route details, gt timestamp for real-time telemetry)
     */
    fun decodeRc4(cipherBase64: String?, keySeed: String?): String {
        if (cipherBase64.isNullOrBlank() || keySeed.isNullOrBlank()) return ""
        return try {
            val keyHex = md5("aibang$keySeed")
            val keyBytes = keyHex.toByteArray(Charsets.UTF_8)
            val cipherBytes = Base64.decode(cipherBase64.trim(), Base64.DEFAULT)

            val cipher = Cipher.getInstance("RC4")
            val keySpec = SecretKeySpec(keyBytes, "RC4")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decryptedBytes = cipher.doFinal(cipherBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            // Return raw string if decoding fails or unencrypted
            cipherBase64
        }
    }

    /**
     * Parses full transit route line name, e.g. "1(Sihui Hub-Laoshan Terminus)".
     * Returns Triple(LineNumber, StartStation, EndStation).
     */
    fun parseFullLineName(fullName: String): Triple<String, String, String> {
        val regex = Regex("""^(.+?)\((.+?)-(.+?)\)$""")
        val match = regex.find(fullName.trim())
        return if (match != null && match.groupValues.size >= 4) {
            Triple(match.groupValues[1].trim(), match.groupValues[2].trim(), match.groupValues[3].trim())
        } else {
            Triple(fullName.trim(), "", "")
        }
    }
}
