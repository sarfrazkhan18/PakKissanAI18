package com.example.utils

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Salted PBKDF2 hashing for the farmer's local passcode.
 *
 * The passcode never leaves the device, but storing it in plaintext means a lost or
 * shared phone (very common in rural Pakistan — one device per household) leaks the
 * credential to anyone with file access. PBKDF2WithHmacSHA256 is available on every
 * supported API level (minSdk 24) and needs no extra dependency.
 *
 * Stored format:  pbkdf2$<iterations>$<saltBase64>$<hashBase64>
 */
object PasswordHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16
    private const val PREFIX = "pbkdf2"

    /** Produce a salted hash string safe to persist. */
    fun hash(rawPassword: String): String {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = pbkdf2(rawPassword.toCharArray(), salt, ITERATIONS)
        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashB64 = Base64.encodeToString(hash, Base64.NO_WRAP)
        return "$PREFIX\$$ITERATIONS\$$saltB64\$$hashB64"
    }

    /**
     * Verify a raw passcode against a stored value.
     *
     * Backwards compatible: rows written before hashing was introduced hold the passcode
     * in plaintext. Those are detected by the missing "pbkdf2$" prefix and compared
     * directly, so existing farmers can still log in. Callers should re-save with [hash]
     * on a successful legacy match to upgrade the row (see [isLegacyPlaintext]).
     */
    fun verify(rawPassword: String, stored: String): Boolean {
        if (stored.isEmpty()) return false
        if (!stored.startsWith("$PREFIX\$")) {
            // Legacy plaintext row
            return rawPassword == stored
        }
        val parts = stored.split("$")
        if (parts.size != 4) return false
        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = runCatching { Base64.decode(parts[2], Base64.NO_WRAP) }.getOrNull() ?: return false
        val expected = runCatching { Base64.decode(parts[3], Base64.NO_WRAP) }.getOrNull() ?: return false
        val actual = pbkdf2(rawPassword.toCharArray(), salt, iterations)
        return constantTimeEquals(expected, actual)
    }

    /** True if the stored value is an un-hashed legacy passcode that should be upgraded. */
    fun isLegacyPlaintext(stored: String): Boolean =
        stored.isNotEmpty() && !stored.startsWith("$PREFIX\$")

    private fun pbkdf2(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS)
        return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) result = result or (a[i].toInt() xor b[i].toInt())
        return result == 0
    }
}
