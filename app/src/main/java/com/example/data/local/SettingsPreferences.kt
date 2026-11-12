package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "topmovies_settings")

data class ApiEndpointConfig(
    val baseUrl: String = BuildConfig.TOPMOVIES_API_BASE_URL,
    val apiKey: String = BuildConfig.TOPMOVIES_API_KEY
) {
    val combinedUrl: String
        get() = "${baseUrl.trimEnd('/')}/$apiKey"
}

class SettingsPreferences(private val context: Context) {
    private val keyBaseUrl = stringPreferencesKey("api_base_url")
    private val keyApiKey = stringPreferencesKey("api_key")

    val endpointConfigFlow: Flow<ApiEndpointConfig> = context.dataStore.data.map { prefs ->
        ApiEndpointConfig(
            baseUrl = prefs[keyBaseUrl]?.takeIf { it.isNotBlank() } ?: BuildConfig.TOPMOVIES_API_BASE_URL,
            apiKey = prefs[keyApiKey]?.takeIf { it.isNotBlank() } ?: BuildConfig.TOPMOVIES_API_KEY
        )
    }

    suspend fun getEndpointConfig(): ApiEndpointConfig = endpointConfigFlow.first()

    suspend fun updateEndpointConfig(rawInput: String, overrideKey: String? = null) {
        val parsed = parseEndpointInput(rawInput, overrideKey)
        context.dataStore.edit { prefs ->
            prefs[keyBaseUrl] = parsed.baseUrl
            prefs[keyApiKey] = parsed.apiKey
        }
    }

    suspend fun resetDefaults() {
        context.dataStore.edit { prefs ->
            prefs.remove(keyBaseUrl)
            prefs.remove(keyApiKey)
        }
    }

    companion object {
        fun parseEndpointInput(rawUrl: String, explicitKey: String? = null): ApiEndpointConfig {
            val trimmed = rawUrl.trim().ifEmpty { BuildConfig.TOPMOVIES_API_BASE_URL }
            val normalized = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                trimmed
            } else {
                "https://$trimmed"
            }

            if (!explicitKey.isNullOrBlank()) {
                val base = if (normalized.endsWith("/")) normalized else "$normalized/"
                return ApiEndpointConfig(baseUrl = base, apiKey = explicitKey.trim())
            }

            // Support combined OXOO URLs like https://topmovies.info/rest-api/5ahofo1htud0ak7bkacckwbz
            val marker = "/rest-api/"
            val idx = normalized.indexOf(marker)
            if (idx >= 0) {
                val basePart = normalized.substring(0, idx + marker.length)
                val suffix = normalized.substring(idx + marker.length).trim('/')
                if (suffix.isNotBlank() && !suffix.contains("/")) {
                    return ApiEndpointConfig(baseUrl = basePart, apiKey = suffix)
                }
            }
            val base = if (normalized.endsWith("/")) normalized else "$normalized/"
            return ApiEndpointConfig(baseUrl = base, apiKey = BuildConfig.TOPMOVIES_API_KEY)
        }
    }
}
