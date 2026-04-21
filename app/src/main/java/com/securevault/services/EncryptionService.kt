package com.securevault.services

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object EncryptionService {

    private const val PREF_FILE = "secure_vault_prefs"
    private const val KEY_HASH = "master_hash"
    private const val KEY_SALT = "master_salt"
    private const val ITERATIONS = 100_000
    private const val KEY_LENGTH = 256

    private var secretKey: SecretKey? = null

    private fun getPrefs(context: Context) = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context, PREF_FILE, masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
    }

    fun isMasterPasswordSet(context: Context): Boolean {
        return getPrefs(context).contains(KEY_HASH)
    }

    fun setMasterPassword(context: Context, password: String) {
        val salt = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val derived = deriveKey(password, salt)
        val hash = derived.encoded.toHex()
        val prefs = getPrefs(context)
        prefs.edit()
            .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_HASH, hash)
            .apply()
        secretKey = derived
    }

    fun verifyMasterPassword(context: Context, password: String): Boolean {
        val prefs = getPrefs(context)
        val saltStr = prefs.getString(KEY_SALT, null) ?: return false
        val storedHash = prefs.getString(KEY_HASH, null) ?: return false
        val salt = Base64.decode(saltStr, Base64.NO_WRAP)
        val derived = deriveKey(password, salt)
        return if (derived.encoded.toHex() == storedHash) {
            secretKey = derived
            true
        } else false
    }

    fun clearKey() { secretKey = null }

    fun encrypt(plain: String): String {
        val key = secretKey ?: throw IllegalStateException("Not unlocked")
        val iv = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val encB64 = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        return "$ivB64:$encB64"
    }

    fun decrypt(cipher: String): String {
        val key = secretKey ?: throw IllegalStateException("Not unlocked")
        val parts = cipher.split(":")
        if (parts.size != 2) return ""
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val enc = Base64.decode(parts[1], Base64.NO_WRAP)
        val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
        c.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        return String(c.doFinal(enc), Charsets.UTF_8)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }

    // Password strength
    fun checkStrength(password: String): Pair<String, Float> {
        var score = 0
        if (password.length >= 8) score++
        if (password.length >= 12) score++
        if (password.length >= 16) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { "!@#\$%^&*()_+-=[]{}|;:,.<>?".contains(it) }) score++
        return when {
            score <= 2 -> "Weak" to 0.2f
            score <= 4 -> "Fair" to 0.4f
            score <= 5 -> "Good" to 0.65f
            score <= 6 -> "Strong" to 0.85f
            else       -> "Very Strong" to 1.0f
        }
    }

    // Generate password
    fun generatePassword(length: Int, upper: Boolean, lower: Boolean,
                         nums: Boolean, symbols: Boolean): String {
        var chars = ""
        if (upper)   chars += "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        if (lower)   chars += "abcdefghijklmnopqrstuvwxyz"
        if (nums)    chars += "0123456789"
        if (symbols) chars += "!@#\$%^&*()_+-=[]{}|;:,.<>?"
        if (chars.isEmpty()) chars = "abcdefghijklmnopqrstuvwxyz"
        val rnd = SecureRandom()
        return (1..length).map { chars[rnd.nextInt(chars.length)] }.joinToString("")
    }
}
