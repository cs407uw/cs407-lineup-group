package com.cs407.lineup.data

/**
 * Data class representing the AI's analysis of a line image
 */
data class LineAnalysis(
    val peopleCount: Int,
    val lineDensity: LineDensity,
    val venueType: VenueType,
    val environmentFactors: EnvironmentFactors,
    val confidence: Double,
    val analysisNotes: String
)

data class EnvironmentFactors(
    val indoor: Boolean,
    val queueOrganization: QueueOrganization,
    val visibleStaff: Int
)

enum class LineDensity {
    SPARSE,
    MEDIUM,
    DENSE,
    VERY_DENSE
}

enum class VenueType {
    RESTAURANT,
    BAR,
    CAFE,
    OTHER
}

enum class QueueOrganization {
    ORGANIZED,
    SCATTERED,
    UNCLEAR
}
