package org.openpdf.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun savePassword(documentUri: String, password: String) {
        prefs.edit().putString(passwordKey(documentUri), password).apply()
    }

    fun getPassword(documentUri: String): String? =
        prefs.getString(passwordKey(documentUri), null)

    fun removePassword(documentUri: String) {
        prefs.edit().remove(passwordKey(documentUri)).apply()
    }

    fun hasPassword(documentUri: String): Boolean =
        prefs.contains(passwordKey(documentUri))

    private fun passwordKey(documentUri: String): String = "pwd_$documentUri"

    companion object {
        private const val PREFS_NAME = "openpdf_secure_prefs"
    }
}
