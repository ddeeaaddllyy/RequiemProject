package com.application.requiemproject.data.repository

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import com.application.requiemproject.domain.model.*
import com.application.requiemproject.domain.repository.ProviderConfigurationRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Credentials never enter backups, SavedStateHandle, logs, or the APK. */
class EncryptedProviderConfigurationRepository(context: Context) : ProviderConfigurationRepository {
    private val directory = File(context.noBackupFilesDir, "translation-providers")
    private val lock = Any()
    private val gson = Gson()

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }

    override suspend fun read(provider: TranslationProvider): ProviderConfiguration = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val file = AtomicFile(File(directory, "${provider.name}.bin"))
            if (!file.baseFile.exists()) return@synchronized ProviderConfiguration(model = provider.defaultModel)
            try {
                val bytes = file.readFully()
                require(bytes.size > 28 && bytes[0].toInt() == 12)
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(1, 13)))
                cipher.updateAAD(provider.name.toByteArray(Charsets.UTF_8))
                gson.fromJson(String(cipher.doFinal(bytes.copyOfRange(13, bytes.size)), Charsets.UTF_8), ProviderConfiguration::class.java)
            } catch (_: Exception) {
                // Lost keystore entries or damaged credentials require entering a new key.
                ProviderConfiguration(model = provider.defaultModel)
            }
        }
    }

    override suspend fun save(provider: TranslationProvider, configuration: ProviderConfiguration) = withContext(Dispatchers.IO) {
        synchronized(lock) {
            check(directory.isDirectory || directory.mkdirs())
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key())
            cipher.updateAAD(provider.name.toByteArray(Charsets.UTF_8))
            val encrypted = byteArrayOf(cipher.iv.size.toByte()) + cipher.iv + cipher.doFinal(gson.toJson(configuration).toByteArray(Charsets.UTF_8))
            val file = AtomicFile(File(directory, "${provider.name}.bin"))
            val stream = file.startWrite()
            try { stream.write(encrypted); file.finishWrite(stream) }
            catch (error: Exception) { file.failWrite(stream); throw error }
        }
    }

    private companion object { const val KEY_ALIAS = "requiem_translation_providers_v1" }
}
