package com.cs407.lineup.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.tasks.await

/**
 * Singleton provider for API keys stored in Firebase Remote Config.
 * 
 * Usage:
 *   // Initialize once at app startup (e.g., in MainActivity or Application)
 *   ApiKeyProvider.initialize()
 *   
 *   // Get the key anywhere in the app
 *   val apiKey = ApiKeyProvider.geminiApiKey
 */
object ApiKeyProvider {

    private const val TAG = "ApiKeyProvider"

    // Remote Config parameter names (must match Firebase Console)
    private const val GEMINI_API_KEY_PARAM = "gemini_api_key"
    private const val PLACES_API_KEY_PARAM = "places_api_key"

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        Firebase.remoteConfig.apply {
            // Set minimum fetch interval (0 for debug, 3600 for production)
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600 // 1 hour cache
            }
            setConfigSettingsAsync(configSettings)

            // Set default values (fallback if fetch fails)
            setDefaultsAsync(mapOf(
                GEMINI_API_KEY_PARAM to "",
                PLACES_API_KEY_PARAM to ""
            ))
        }
    }

    /**
     * The Gemini API key from Remote Config.
     * Falls back to BuildConfig if Remote Config is empty.
     */
    val geminiApiKey: String
        get() {
            val remoteKey = remoteConfig.getString(GEMINI_API_KEY_PARAM)
            return if (remoteKey.isNotBlank()) {
                remoteKey
            } else {
                // Fallback to local BuildConfig for development
                com.cs407.lineup.BuildConfig.GEMINI_API_KEY
            }
        }

    /**
     * The Google Places API key from Remote Config.
     * Falls back to BuildConfig MAPS_API_KEY if Remote Config is empty.
     */
    val placesApiKey: String
        get() {
            val remoteKey = remoteConfig.getString(PLACES_API_KEY_PARAM)
            return if (remoteKey.isNotBlank()) {
                remoteKey
            } else {
                // Fallback to local BuildConfig for development
                com.cs407.lineup.BuildConfig.MAPS_API_KEY
            }
        }

    /**
     * Initialize and fetch remote config values.
     * Call this once at app startup.
     */
    suspend fun initialize() {
        try {
            // Fetch and activate remote config
            remoteConfig.fetchAndActivate().await()
            Log.d(TAG, "Remote Config fetched successfully")

            // Log all available keys
            val allKeys = remoteConfig.all.keys
            Log.d(TAG, "All Remote Config keys: $allKeys")

            // Log Gemini key status with first 10 chars
            val geminiKey = remoteConfig.getString(GEMINI_API_KEY_PARAM)
            if (geminiKey.isNotBlank()) {
                Log.d(TAG, "Gemini API key: from Remote Config (${geminiKey.take(10)}...)")
            } else {
                Log.d(TAG, "Gemini API key: using BuildConfig fallback")
            }

            // Log Places key status with first 10 chars
            val placesKey = remoteConfig.getString(PLACES_API_KEY_PARAM)
            if (placesKey.isNotBlank()) {
                Log.d(TAG, "Places API key: from Remote Config (${placesKey.take(10)}...)")
            } else {
                Log.d(TAG, "Places API key: using BuildConfig fallback (MAPS_API_KEY)")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch Remote Config, using defaults", e)
        }
    }
    
    /**
     * Force refresh the config (useful for debugging).
     */
    suspend fun forceRefresh() {
        try {
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0
            }
            remoteConfig.setConfigSettingsAsync(configSettings).await()
            remoteConfig.fetchAndActivate().await()
            Log.d(TAG, "Remote Config force refreshed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to force refresh Remote Config", e)
        }
    }
}

