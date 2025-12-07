package com.cs407.lineup.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit
import android.util.Base64

/**
 * Repository for handling wait time estimation via Gemini API
 * Uploads an image and receives estimated wait time based on AI analysis
 */
class WaitTimeRepository {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val calculator = WaitTimeCalculator()

    /**
     * Upload an image to the Gemini API and get wait time estimation
     * 
     * @param imageFile The image file to upload
     * @param apiKey The Gemini API key
     * @return WaitTimeResult containing wait time and confidence or error message
     */
    suspend fun uploadImage(
        imageFile: File,
        apiKey: String
    ): WaitTimeResult {
        return withContext(Dispatchers.IO) {
            try {
                // Read and encode the image as base64
                val imageBytes = imageFile.readBytes()
                val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                
                // Build the Gemini API request
                val requestJson = buildGeminiRequest(base64Image)
                
                val requestBody = requestJson.toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())
                
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=$apiKey")
                    .post(requestBody)
                    .build()

                // Execute the request
                val response = client.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    val errorMessage = when (response.code) {
                        429 -> "Rate limit exceeded. Please wait a minute and try again. (Free tier: 10 requests/min)"
                        400 -> "Invalid request. Please check your API key."
                        403 -> "API key invalid or unauthorized."
                        else -> "API error: ${response.code} ${response.message}"
                    }
                    
                    return@withContext WaitTimeResult(
                        errorMessage = errorMessage
                    )
                }

                // Parse the response
                val responseBody = response.body?.string() ?: ""
                val analysis = parseGeminiResponse(responseBody)
                
                if (analysis == null) {
                    return@withContext WaitTimeResult(
                        errorMessage = "Failed to parse AI response"
                    )
                }
                
                // Calculate wait time using the algorithm
                val waitTime = calculator.calculateWaitTime(analysis)
                
                WaitTimeResult(
                    waitTimeMinutes = waitTime,
                    confidence = analysis.confidence
                )
                
            } catch (e: Exception) {
                WaitTimeResult(
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Build the JSON request for Gemini API
     */
    private fun buildGeminiRequest(base64Image: String): JSONObject {
        val prompt = """
            You are a line analysis expert. Analyze the provided image of a venue line/queue and return ONLY valid JSON with the following structure:
            
            {
              "people_count": <integer>,
              "line_density": "<sparse|medium|dense|very_dense>",
              "venue_type": "<restaurant|bar|cafe|other>",
              "environment_factors": {
                "indoor": <boolean>,
                "queue_organization": "<organized|scattered|unclear>",
                "visible_staff": <integer>
              },
              "confidence": <float 0.0-1.0>,
              "analysis_notes": "<brief explanation>"
            }
            
            IMPORTANT RULES:
            1. If you see NO LINE or NO PEOPLE WAITING, set people_count to 0
            2. If people_count is 0, set line_density to "sparse" and confidence high
            3. Only count people who are clearly waiting in a queue/line
            4. Do NOT count staff, passersby, or people already inside
            5. If the image shows an empty venue or no visible queue, people_count MUST be 0
            6. Be accurate with people_count - if unclear, estimate conservatively and lower confidence
            
            Return ONLY the JSON, no other text.
        """.trimIndent()
        
        return JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                        put(JSONObject().apply {
                            put("inline_data", JSONObject().apply {
                                put("mime_type", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                    })
                })
            })
        }
    }
    
    /**
     * Parse the Gemini API response and extract line analysis
     */
    private fun parseGeminiResponse(responseBody: String): LineAnalysis? {
        try {
            val json = JSONObject(responseBody)
            
            // Extract the text response from Gemini
            val candidates = json.getJSONArray("candidates")
            if (candidates.length() == 0) return null
            
            val content = candidates.getJSONObject(0).getJSONObject("content")
            val parts = content.getJSONArray("parts")
            if (parts.length() == 0) return null
            
            var textResponse = parts.getJSONObject(0).getString("text")
            
            // Log the raw response for debugging
            android.util.Log.d("WaitTimeRepository", "Raw AI response: $textResponse")
            
            // Clean up the response - remove markdown code blocks if present
            textResponse = textResponse.trim()
            if (textResponse.startsWith("```json")) {
                textResponse = textResponse.removePrefix("```json").removeSuffix("```").trim()
            } else if (textResponse.startsWith("```")) {
                textResponse = textResponse.removePrefix("```").removeSuffix("```").trim()
            }
            
            // Try to find JSON in the response if there's extra text
            val jsonStart = textResponse.indexOf("{")
            val jsonEnd = textResponse.lastIndexOf("}") + 1
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                textResponse = textResponse.substring(jsonStart, jsonEnd)
            }
            
            // Parse the JSON from the text response
            val analysisJson = JSONObject(textResponse)
            
            val peopleCount = analysisJson.getInt("people_count")
            val lineDensity = when (analysisJson.getString("line_density").lowercase()) {
                "sparse" -> LineDensity.SPARSE
                "medium" -> LineDensity.MEDIUM
                "dense" -> LineDensity.DENSE
                "very_dense" -> LineDensity.VERY_DENSE
                else -> LineDensity.MEDIUM
            }
            val venueType = when (analysisJson.getString("venue_type").lowercase()) {
                "restaurant" -> VenueType.RESTAURANT
                "bar" -> VenueType.BAR
                "cafe" -> VenueType.CAFE
                else -> VenueType.OTHER
            }
            
            val envFactors = analysisJson.getJSONObject("environment_factors")
            val queueOrg = when (envFactors.getString("queue_organization").lowercase()) {
                "organized" -> QueueOrganization.ORGANIZED
                "scattered" -> QueueOrganization.SCATTERED
                else -> QueueOrganization.UNCLEAR
            }
            
            val environmentFactors = EnvironmentFactors(
                indoor = envFactors.getBoolean("indoor"),
                queueOrganization = queueOrg,
                visibleStaff = envFactors.getInt("visible_staff")
            )
            
            return LineAnalysis(
                peopleCount = peopleCount,
                lineDensity = lineDensity,
                venueType = venueType,
                environmentFactors = environmentFactors,
                confidence = analysisJson.getDouble("confidence"),
                analysisNotes = analysisJson.getString("analysis_notes")
            )
            
        } catch (e: Exception) {
            android.util.Log.e("WaitTimeRepository", "Failed to parse response", e)
            e.printStackTrace()
            return null
        }
    }
}
