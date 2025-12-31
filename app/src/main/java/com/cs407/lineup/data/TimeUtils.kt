package com.cs407.lineup.data

import com.google.firebase.Timestamp
import kotlin.math.abs

/**
 * Utility functions for displaying time-related information
 */
object TimeUtils {

    /**
     * Convert a Firebase Timestamp to a human-readable "time ago" string
     * Examples: "2 minutes ago", "1 hour ago", "3 days ago"
     */
    fun getTimeAgo(timestamp: Timestamp): String {
        val now = Timestamp.now()
        val secondsAgo = abs(now.seconds - timestamp.seconds)

        return when {
            secondsAgo < 60 -> "Just now"
            secondsAgo < 120 -> "1 minute ago"
            secondsAgo < 3600 -> "${secondsAgo / 60} minutes ago"
            secondsAgo < 7200 -> "1 hour ago"
            secondsAgo < 86400 -> "${secondsAgo / 3600} hours ago"
            secondsAgo < 172800 -> "1 day ago"
            else -> "${secondsAgo / 86400} days ago"
        }
    }

    /**
     * Get a color indicator based on how fresh the wait time is
     * Green: < 10 min, Yellow: 10-30 min, Orange: 30-60 min, Red: > 60 min
     */
    fun getFreshnessColor(timestamp: Timestamp): android.graphics.Color {
        val now = Timestamp.now()
        val minutesAgo = (now.seconds - timestamp.seconds) / 60

        return when {
            minutesAgo < 10 -> android.graphics.Color.valueOf(0xFF4CAF50.toInt())  // Green
            minutesAgo < 30 -> android.graphics.Color.valueOf(0xFFFFA726.toInt())  // Yellow/Orange
            minutesAgo < 60 -> android.graphics.Color.valueOf(0xFFFF7043.toInt())  // Orange
            else -> android.graphics.Color.valueOf(0xFFEF5350.toInt())            // Red
        }
    }

    /**
     * Check if wait time data is still fresh (< 15 minutes old)
     */
    fun isFresh(timestamp: Timestamp): Boolean {
        val now = Timestamp.now()
        val minutesAgo = (now.seconds - timestamp.seconds) / 60
        return minutesAgo < 15
    }
}
