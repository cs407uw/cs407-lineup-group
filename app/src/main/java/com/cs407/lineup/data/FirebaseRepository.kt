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
}
