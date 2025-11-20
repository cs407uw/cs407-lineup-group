package com.cs407.lineup.data

/**
 * Data class to hold the response from the RunPod wait time estimation API
 *
 * @param waitTimeMinutes The estimated wait time in minutes
 * @param confidence The confidence level of the estimation (0.0 to 1.0)
 * @param errorMessage Optional error message if the API call fails
 */
data class WaitTimeResult(
    val waitTimeMinutes: Int? = null,
    val confidence: Double? = null,
    val errorMessage: String? = null
) {
    val isSuccess: Boolean
        get() = waitTimeMinutes != null && confidence != null
}
