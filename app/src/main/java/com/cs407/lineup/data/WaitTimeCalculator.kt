package com.cs407.lineup.data

import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Calculator for estimating wait times based on AI line analysis
 */
class WaitTimeCalculator {
    
    /**
     * Calculate estimated wait time in minutes based on line analysis
     * 
     * @param analysis The AI's analysis of the line
     * @return Estimated wait time in minutes
     */
    fun calculateWaitTime(analysis: LineAnalysis): Int {
        // Base time per person based on venue type
        val baseTimePerPerson = when (analysis.venueType) {
            VenueType.RESTAURANT -> 2.5
            VenueType.BAR -> 1.5
            VenueType.CAFE -> 2.0
            VenueType.OTHER -> 2.0
        }
        
        // Density multiplier
        val densityMultiplier = when (analysis.lineDensity) {
            LineDensity.SPARSE -> 0.8
            LineDensity.MEDIUM -> 1.0
            LineDensity.DENSE -> 1.3
            LineDensity.VERY_DENSE -> 1.6
        }
        
        // Organization factor
        val organizationFactor = when (analysis.environmentFactors.queueOrganization) {
            QueueOrganization.ORGANIZED -> 1.0
            QueueOrganization.SCATTERED -> 1.4
            QueueOrganization.UNCLEAR -> 1.2
        }
        
        // Staff adjustment (each staff member reduces wait time by 8%, max 5 staff)
        val staffFactor = 1.0 - (min(analysis.environmentFactors.visibleStaff, 5) * 0.08)
        
        // Calculate final wait time
        val estimatedWaitTime = baseTimePerPerson * 
                                analysis.peopleCount * 
                                densityMultiplier * 
                                organizationFactor * 
                                staffFactor
        
        return estimatedWaitTime.roundToInt()
    }
    
    /**
     * Format the wait time result for display
     * 
     * @param waitTime The calculated wait time in minutes
     * @param confidence The AI's confidence level (0.0 to 1.0)
     * @return Formatted string for display
     */
    fun formatWaitTime(waitTime: Int, confidence: Double): String {
        return if (confidence < 0.7) {
            // Low confidence - show range
            val lowerBound = (waitTime * 0.8).roundToInt()
            val upperBound = (waitTime * 1.2).roundToInt()
            "$lowerBound-$upperBound min (Low Confidence)"
        } else {
            // High confidence - show exact time with percentage
            val confidencePercent = (confidence * 100).roundToInt()
            "$waitTime min (${confidencePercent}% confident)"
        }
    }
}
