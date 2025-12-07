package com.cs407.lineup.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.tasks.await

// fetches api keys from firebase remote config
object ApiKeyProvider {

    private const val TAG = "ApiKeyProvider"
    private const val GEMINI_API_KEY_PARAM = "gemini_api_key"
    private const val PLACES_API_KEY_PARAM = "places_api_key"

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        Firebase.remoteConfig.apply {
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(mapOf(
                GEMINI_API_KEY_PARAM to "",
                PLACES_API_KEY_PARAM to ""
            ))
        }
    }

    // falls back to buildconfig if remote is empty
    val geminiApiKey: String
        get() {
            val remoteKey = remoteConfig.getString(GEMINI_API_KEY_PARAM)
            return if (remoteKey.isNotBlank()) remoteKey
            else com.cs407.lineup.BuildConfig.GEMINI_API_KEY
        }

    val placesApiKey: String
        get() {
            val remoteKey = remoteConfig.getString(PLACES_API_KEY_PARAM)
            return if (remoteKey.isNotBlank()) remoteKey
            else com.cs407.lineup.BuildConfig.MAPS_API_KEY
        }

    // call at app startup
    suspend fun initialize() {
        try {
            remoteConfig.fetchAndActivate().await()
            Log.d(TAG, "remote config fetched")
        } catch (e: Exception) {
            Log.w(TAG, "remote config fetch failed", e)
        }
    }

    suspend fun forceRefresh() {
        try {
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0
            }
            remoteConfig.setConfigSettingsAsync(configSettings).await()
            remoteConfig.fetchAndActivate().await()
        } catch (e: Exception) {
            Log.e(TAG, "force refresh failed", e)
        }
    }
}

