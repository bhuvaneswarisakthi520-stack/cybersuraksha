package com.example.myapplication.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object EncryptionHelper {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    
    fun generateKey(alias: String) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        
        if (!keyStore.containsAlias(alias)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    fun encrypt(data: ByteArray, alias: String): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        val secretKey = keyStore.getKey(alias, null) as SecretKey
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data)
        
        // Combine IV and encrypted data
        val result = ByteArray(iv.size + encryptedData.size)
        System.arraycopy(iv, 0, result, 0, iv.size)
        System.arraycopy(encryptedData, 0, result, iv.size, encryptedData.size)
        
        return result
    }

    fun decrypt(data: ByteArray, alias: String): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        val secretKey = keyStore.getKey(alias, null) as SecretKey
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = ByteArray(12) // GCM IV size is 12 bytes
        System.arraycopy(data, 0, iv, 0, iv.size)
        val encryptedData = ByteArray(data.size - iv.size)
        System.arraycopy(data, iv.size, encryptedData, 0, encryptedData.size)
        
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        return cipher.doFinal(encryptedData)
    }

    fun hashEvidence(filePath: String): String {
        val file = File(filePath)
        if (!file.exists()) return ""
        
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = file.readBytes()
        val hashBytes = digest.digest(bytes)
        
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
