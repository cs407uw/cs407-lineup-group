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
    val confidenceScore: Double = 0.0
)
