package com.cs407.lineup.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

/**
 * Repository for Firebase Firestore operations
 * Handles saving feedback and retrieving venue metrics
 */
class FirebaseRepository {
    
    private val db = FirebaseFirestore.getInstance()
    
    /**
     * Save wait time feedback to Firestore
     */
    suspend fun saveFeedback(
        venueId: String,
        venueName: String,
        estimatedMinutes: Int,
        actualMinutes: Int,
        analysis: LineAnalysis
    ): Boolean {
        return try {
            val calendar = Calendar.getInstance()
            
            val feedback = WaitTimeFeedback(
                venueId = venueId,
                venueName = venueName,
                estimatedMinutes = estimatedMinutes,
                actualMinutes = actualMinutes,
                peopleCount = analysis.peopleCount,
                density = analysis.lineDensity.name.lowercase(),
                venueType = analysis.venueType.name.lowercase(),
                queueOrganization = analysis.environmentFactors.queueOrganization.name.lowercase(),
                visibleStaff = analysis.environmentFactors.visibleStaff,
                timestamp = Timestamp.now(),
                hourOfDay = calendar.get(Calendar.HOUR_OF_DAY),
                dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            )
            
            db.collection("venues")
                .document(venueId)
                .collection("feedback")
                .add(feedback)
                .await()
            
            // Update venue metrics
            updateVenueMetrics(venueId)
            
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "Error saving feedback", e)
            false
        }
    }
    
    /**
     * Get venue metrics from Firestore
     */
    suspend fun getVenueMetrics(venueId: String): VenueMetrics? {
        return try {
            val doc = db.collection("venues")
                .document(venueId)
                .collection("learned_metrics")
                .document("current")
                .get()
                .await()
            
            doc.toObject(VenueMetrics::class.java)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "Error getting metrics", e)
            null
        }
    }
    
    /**
     * Update venue metrics based on all feedback
     */
    private suspend fun updateVenueMetrics(venueId: String) {
        try {
            val feedbackDocs = db.collection("venues")
                .document(venueId)
                .collection("feedback")
                .get()
                .await()
            
            if (feedbackDocs.isEmpty) return
            
            val feedbackList = feedbackDocs.mapNotNull { 
                it.toObject(WaitTimeFeedback::class.java) 
            }
            
            // Calculate average time per person
            val avgTimePerPerson = feedbackList.map { feedback ->
                if (feedback.peopleCount > 0) {
                    feedback.actualMinutes.toDouble() / feedback.peopleCount
                } else 0.0
            }.filter { it > 0 }.average()
            
            // Calculate confidence score based on feedback count
            val confidenceScore = when (feedbackList.size) {
                in 0..9 -> 0.5
                in 10..29 -> 0.7
                else -> 0.9
            }
            
            val metrics = VenueMetrics(
                avgTimePerPerson = avgTimePerPerson,
                totalFeedbackCount = feedbackList.size,
                lastUpdated = Timestamp.now(),
                confidenceScore = confidenceScore
            )
            
            db.collection("venues")
                .document(venueId)
                .collection("learned_metrics")
                .document("current")
                .set(metrics)
                .await()

        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "Error updating metrics", e)
        }
    }

    // ========== WAIT TIME BACKEND METHODS ==========

    /**
     * Save a wait time report for a venue (from AI or manual entry)
     */
    suspend fun saveWaitTime(
        venueId: String,
        venueName: String,
        waitMinutes: Int,
        source: String,
        rawAiEstimate: Int? = null
    ): Boolean {
        return try {
            val now = Timestamp.now()
            val expiresAt = Timestamp(
                now.seconds + (WaitTimeData.EXPIRATION_MINUTES * 60),
                now.nanoseconds
            )

            val waitTimeData = WaitTimeData(
                waitMinutes = waitMinutes,
                reportedAt = now,
                expiresAt = expiresAt,
                source = source,
                reportedBy = "",  // Could add user ID later
                rawAiEstimate = rawAiEstimate
            )

            // Save to venues/{venueId}/current_wait
            db.collection("venues")
                .document(venueId)
                .collection("current_wait")
                .document("latest")
                .set(waitTimeData)
                .await()

            // If this is a manual entry after an AI entry, calculate bias
            if (source == WaitTimeData.SOURCE_MANUAL) {
                calculateAndUpdateBias(venueId, waitMinutes)
            }

            android.util.Log.d("FirebaseRepository", "Saved wait time: $waitMinutes min for $venueName ($source)")
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "Error saving wait time", e)
            false
        }
    }

    /**
     * Get current wait time for a venue (returns null if expired or not found)
     */
    suspend fun getWaitTime(venueId: String): Int? {
        return try {
            val doc = db.collection("venues")
                .document(venueId)
                .collection("current_wait")
                .document("latest")
                .get()
                .await()

            val waitTimeData = doc.toObject(WaitTimeData::class.java)

            if (waitTimeData != null && waitTimeData.isValid()) {
                waitTimeData.waitMinutes
            } else {
                null  // Expired or not found
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "Error getting wait time for $venueId", e)
            null
        }
    }

    /**
     * Batch fetch wait times for multiple venues (for list display)
     */
    suspend fun getWaitTimesForVenues(venueIds: List<String>): Map<String, Int?> {
        val results = mutableMapOf<String, Int?>()

        // Fetch in parallel batches (Firestore limits to 10 per batch for IN queries)
        venueIds.chunked(10).forEach { batch ->
            batch.forEach { venueId ->
                results[venueId] = getWaitTime(venueId)
            }
        }

        return results
    }

    /**
     * Calculate AI bias when manual entry follows AI estimate
     * Bias = AI estimate - (manual wait + time elapsed)
     */
    private suspend fun calculateAndUpdateBias(venueId: String, manualWaitMinutes: Int) {
        try {
            // Get the previous AI wait time data
            val doc = db.collection("venues")
                .document(venueId)
                .collection("current_wait")
                .document("latest")
                .get()
                .await()

            val previousData = doc.toObject(WaitTimeData::class.java)

            // Only calculate bias if previous entry was AI and we have the raw estimate
            if (previousData?.source == WaitTimeData.SOURCE_AI && previousData.rawAiEstimate != null) {
                val now = Timestamp.now()
                val timeElapsedMinutes = (now.seconds - previousData.reportedAt.seconds) / 60

                // Only calculate if less than 30 minutes have passed
                if (timeElapsedMinutes <= 30) {
                    // Estimated actual wait at AI time = manual wait + elapsed time
                    val estimatedActualWait = manualWaitMinutes + timeElapsedMinutes

                    // Bias = AI estimate - actual (positive = AI overestimated)
                    val bias = previousData.rawAiEstimate - estimatedActualWait

                    android.util.Log.d("FirebaseRepository",
                        "Bias calculation: AI=${previousData.rawAiEstimate}, " +
                        "Manual=$manualWaitMinutes, Elapsed=$timeElapsedMinutes, Bias=$bias")

                    updateVenueBias(venueId, bias.toDouble())
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "Error calculating bias", e)
        }
    }

    /**
     * Update running average of AI bias for a venue
     */
    private suspend fun updateVenueBias(venueId: String, newBias: Double) {
        try {
            val metricsDoc = db.collection("venues")
                .document(venueId)
                .collection("learned_metrics")
                .document("current")
                .get()
                .await()

            val currentMetrics = metricsDoc.toObject(VenueMetrics::class.java) ?: VenueMetrics()

            // Calculate new running average
            val totalPoints = currentMetrics.biasDataPoints + 1
            val newAvgBias = ((currentMetrics.aiBiasMinutes * currentMetrics.biasDataPoints) + newBias) / totalPoints

            val updatedMetrics = currentMetrics.copy(
                aiBiasMinutes = newAvgBias,
                biasDataPoints = totalPoints,
                lastUpdated = Timestamp.now()
            )

            db.collection("venues")
                .document(venueId)
                .collection("learned_metrics")
                .document("current")
                .set(updatedMetrics)
                .await()

            android.util.Log.d("FirebaseRepository",
                "Updated bias for $venueId: avg=$newAvgBias, points=$totalPoints")

        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "Error updating venue bias", e)
        }
    }

    /**
     * Get AI bias correction for a venue (to adjust AI estimates)
     */
    suspend fun getAiBias(venueId: String): Double {
        return try {
            val metrics = getVenueMetrics(venueId)
            metrics?.aiBiasMinutes ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }
}
