package com.cs407.lineup.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Repository for handling wait time estimation via RunPod API
 * Uploads an image and receives estimated wait time and confidence level
 */
class WaitTimeRepository {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Upload an image to the RunPod API and get wait time estimation
     * 
     * @param imageFile The image file to upload
     * @param apiKey The RunPod API key (should be stored in local.properties)
     * @param endpoint The RunPod endpoint URL
     * @return WaitTimeResult containing wait time and confidence or error message
     */
    suspend fun uploadImage(
        imageFile: File,
        apiKey: String,
        endpoint: String = "https://api.runpod.io/v2/endpoint/run"
    ): WaitTimeResult {
        return withContext(Dispatchers.IO) {
            try {
                // Build multipart request body with the image
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "image",
                        imageFile.name,
                        imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                    .build()

                // Build the HTTP request with authorization header
                val request = Request.Builder()
                    .url(endpoint)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .post(requestBody)
                    .build()

                // Execute the request
                val response = client.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    return@withContext WaitTimeResult(
                        errorMessage = "API request failed: ${response.code} ${response.message}"
                    )
                }

                // Parse the JSON response
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)
                
                // Extract wait time and confidence from the response
                // Adjust these field names based on actual RunPod API response format
                val waitTime = json.optInt("wait_time_minutes", -1)
                val confidence = json.optDouble("confidence", -1.0)
                
                if (waitTime == -1 || confidence == -1.0) {
                    return@withContext WaitTimeResult(
                        errorMessage = "Invalid response format from API"
                    )
                }
                
                WaitTimeResult(
                    waitTimeMinutes = waitTime,
                    confidence = confidence
                )
                
            } catch (e: Exception) {
                WaitTimeResult(
                    errorMessage = "Error uploading image: ${e.message}"
                )
            }
        }
    }
}
