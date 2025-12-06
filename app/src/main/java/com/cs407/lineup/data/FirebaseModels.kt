package com.cs407.lineup.data

import com.google.firebase.Timestamp

/**
 * Data class representing wait time feedback stored in Firestore
 */
data class WaitTimeFeedback(
    val venueId: String = "",
    val venueName: String = "",
    val estimatedMinutes: Int = 0,
    val actualMinutes: Int = 0,
    val peopleCount: Int = 0,
    val density: String = "",
    val venueType: String = "",
    val queueOrganization: String = "",
    val visibleStaff: Int = 0,
    val timestamp: Timestamp = Timestamp.now(),
    val hourOfDay: Int = 0,
    val dayOfWeek: Int = 0
)

/**
 * Data class representing learned metrics for a venue
 */
data class VenueMetrics(
    val avgTimePerPerson: Double = 0.0,
    val totalFeedbackCount: Int = 0,
    val lastUpdated: Timestamp = Timestamp.now(),
    val confidenceScore: Double = 0.0,
    // Learning algorithm fields for AI bias correction
    val aiBiasMinutes: Double = 0.0,  // Running average of AI overestimate/underestimate
    val biasDataPoints: Int = 0       // Number of data points used to calculate bias
)

/**
 * Data class representing a current wait time report for a venue
 * Used for real-time wait time display in the restaurant list
 */
data class WaitTimeData(
    val waitMinutes: Int = 0,
    val reportedAt: Timestamp = Timestamp.now(),
    val expiresAt: Timestamp = Timestamp.now(),
    val source: String = "unknown",  // "ai" or "manual"
    val reportedBy: String = "",
    val rawAiEstimate: Int? = null   // Original AI estimate before bias correction (for learning)
) {
    /**
     * Check if this wait time data is still valid (not expired)
     */
    fun isValid(): Boolean {
        return Timestamp.now().seconds < expiresAt.seconds
    }

    companion object {
        const val SOURCE_AI = "ai"
        const val SOURCE_MANUAL = "manual"
        const val EXPIRATION_MINUTES = 30L
    }
}
