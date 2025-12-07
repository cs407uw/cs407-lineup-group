package com.cs407.lineup.data

import com.google.firebase.Timestamp

// feedback data for firestore
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

// single ai vs manual comparison for bias calculation
data class BiasEntry(
    val aiEstimate: Int = 0,
    val manualEntry: Int = 0,
    val biasMinutes: Int = 0,
    val timestamp: Timestamp = Timestamp.now()
)

// learned metrics for a venue with rolling bias window
data class VenueMetrics(
    val avgTimePerPerson: Double = 0.0,
    val totalFeedbackCount: Int = 0,
    val lastUpdated: Timestamp = Timestamp.now(),
    val aiBiasMinutes: Double = 0.0,
    val biasConfidence: Double = 0.0,
    val biasDataPoints: Int = 0,
    val recentBiasEntries: List<Map<String, Any>> = emptyList()
) {
    companion object {
        const val MAX_BIAS_ENTRIES = 25
    }
}

// current wait time report
data class WaitTimeData(
    val waitMinutes: Int = 0,
    val reportedAt: Timestamp = Timestamp.now(),
    val expiresAt: Timestamp = Timestamp.now(),
    val source: String = "unknown",
    val reportedBy: String = "",
    val rawAiEstimate: Int? = null
) {
    fun isValid(): Boolean = Timestamp.now().seconds < expiresAt.seconds

    companion object {
        const val SOURCE_AI = "ai"
        const val SOURCE_MANUAL = "manual"
        const val EXPIRATION_MINUTES = 30L
    }
}
