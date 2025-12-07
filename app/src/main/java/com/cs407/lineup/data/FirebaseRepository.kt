package com.cs407.lineup.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

// repo for firestore - save feedback, get metrics, wait times
class FirebaseRepository {

    companion object {
        private const val TAG = "FirebaseRepository"
    }

    private val db = FirebaseFirestore.getInstance()

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

            updateVenueMetrics(venueId)
            true
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "save feedback error", e)
            false
        }
    }

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
            android.util.Log.e("FirebaseRepository", "get metrics error", e)
            null
        }
    }

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

            val avgTimePerPerson = feedbackList.map { feedback ->
                if (feedback.peopleCount > 0) {
                    feedback.actualMinutes.toDouble() / feedback.peopleCount
                } else 0.0
            }.filter { it > 0 }.average()

            val confidenceScore = when (feedbackList.size) {
                in 0..9 -> 0.5
                in 10..29 -> 0.7
                else -> 0.9
            }

            val metrics = VenueMetrics(
                avgTimePerPerson = avgTimePerPerson,
                totalFeedbackCount = feedbackList.size,
                lastUpdated = Timestamp.now(),
                //confidenceScore = confidenceScore
            )

            db.collection("venues")
                .document(venueId)
                .collection("learned_metrics")
                .document("current")
                .set(metrics)
                .await()

        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "update metrics error", e)
        }
    }

    // save wait time from ai or manual entry
    suspend fun saveWaitTime(
        venueId: String,
        venueName: String,
        waitMinutes: Int,
        source: String,
        rawAiEstimate: Int? = null
    ): Boolean {
        return try {
            android.util.Log.d(TAG, "=== SAVING WAIT TIME TO DATABASE ===")
            android.util.Log.d(TAG, "Venue: $venueName (ID: $venueId)")
            android.util.Log.d(TAG, "Wait time: $waitMinutes minutes")
            android.util.Log.d(TAG, "Source: $source")
            if (rawAiEstimate != null) {
                android.util.Log.d(TAG, "Raw AI estimate: $rawAiEstimate minutes")
            }

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
                reportedBy = "",
                rawAiEstimate = rawAiEstimate
            )

            val docPath = "venues/$venueId/current_wait/latest"
            android.util.Log.d(TAG, "Writing to Firestore path: $docPath")

            db.collection("venues")
                .document(venueId)
                .collection("current_wait")
                .document("latest")
                .set(waitTimeData)
                .await()

            if (source == WaitTimeData.SOURCE_MANUAL) {
                calculateAndUpdateBias(venueId, waitMinutes)
            }

            android.util.Log.d(TAG, "✅ SUCCESS: Wait time saved to database!")
            android.util.Log.d(TAG, "Expires at: ${expiresAt.toDate()}")
            android.util.Log.d(TAG, "=== END SAVE WAIT TIME ===")
            true
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ ERROR: Failed to save wait time to database", e)
            false
        }
    }

    // get wait time, null if expired or not found
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
                android.util.Log.d(TAG, "found wait time for $venueId: ${waitTimeData.waitMinutes} min")
                waitTimeData.waitMinutes
            } else if (waitTimeData != null) {
                android.util.Log.d(TAG, "wait time expired for $venueId")
                null
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "get wait time error for $venueId", e)
            null
        }
    }

    // batch fetch for list display
    suspend fun getWaitTimesForVenues(venueIds: List<String>): Map<String, Int?> {
        android.util.Log.d(TAG, "=== FETCHING WAIT TIMES FROM DATABASE ===")
        android.util.Log.d(TAG, "Fetching wait times for ${venueIds.size} venues")

        val results = mutableMapOf<String, Int?>()

        venueIds.chunked(10).forEach { batch ->
            batch.forEach { venueId ->
                results[venueId] = getWaitTime(venueId)
            }
        }

        val venuesWithData = results.count { it.value != null }
        android.util.Log.d(TAG, "Found wait times for $venuesWithData out of ${venueIds.size} venues")
        if (venuesWithData > 0) {
            android.util.Log.d(TAG, "Venues with wait times: ${results.filter { it.value != null }}")
        }
        android.util.Log.d(TAG, "=== END FETCH WAIT TIMES ===")

        return results
    }

    // calculates bias when manual entry follows ai estimate
    private suspend fun calculateAndUpdateBias(venueId: String, manualWaitMinutes: Int) {
        try {
            val doc = db.collection("venues")
                .document(venueId)
                .collection("current_wait")
                .document("latest")
                .get()
                .await()

            val previousData = doc.toObject(WaitTimeData::class.java)

            if (previousData?.source == WaitTimeData.SOURCE_AI && previousData.rawAiEstimate != null) {
                val now = Timestamp.now()
                val timeElapsedMinutes = ((now.seconds - previousData.reportedAt.seconds) / 60).toInt()

                // only compare if ai estimate was within last 30 min
                if (timeElapsedMinutes <= 30) {
                    val estimatedActualWait = manualWaitMinutes + timeElapsedMinutes
                    val bias = previousData.rawAiEstimate - estimatedActualWait

                    val entry = BiasEntry(
                        aiEstimate = previousData.rawAiEstimate,
                        manualEntry = estimatedActualWait,
                        biasMinutes = bias,
                        timestamp = now
                    )

                    android.util.Log.d(TAG,
                        "bias entry: ai=${entry.aiEstimate}, actual=${entry.manualEntry}, bias=${entry.biasMinutes}")

                    addBiasEntry(venueId, entry)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "bias calc error", e)
        }
    }

    // adds entry to rolling window of 25 and recalculates avg bias
    private suspend fun addBiasEntry(venueId: String, entry: BiasEntry) {
        try {
            val metricsDoc = db.collection("venues")
                .document(venueId)
                .collection("learned_metrics")
                .document("current")
                .get()
                .await()

            val currentMetrics = metricsDoc.toObject(VenueMetrics::class.java) ?: VenueMetrics()

            // convert entry to map for firestore
            val entryMap = mapOf(
                "aiEstimate" to entry.aiEstimate,
                "manualEntry" to entry.manualEntry,
                "biasMinutes" to entry.biasMinutes,
                "timestamp" to entry.timestamp
            )

            // keep last 25 entries (rolling window)
            val updatedEntries = (currentMetrics.recentBiasEntries + entryMap)
                .takeLast(VenueMetrics.MAX_BIAS_ENTRIES)

            // calculate avg bias from rolling window
            val avgBias = updatedEntries.mapNotNull {
                (it["biasMinutes"] as? Number)?.toDouble()
            }.average().takeIf { !it.isNaN() } ?: 0.0

            val dataPoints = updatedEntries.size

            // confidence increases with more data points
            val confidence = when (dataPoints) {
                0 -> 0.0
                in 1..4 -> 0.2
                in 5..9 -> 0.4
                in 10..14 -> 0.6
                in 15..19 -> 0.8
                else -> 1.0
            }

            val updatedMetrics = currentMetrics.copy(
                aiBiasMinutes = avgBias,
                biasDataPoints = dataPoints,
                biasConfidence = confidence,
                recentBiasEntries = updatedEntries,
                lastUpdated = Timestamp.now()
            )

            db.collection("venues")
                .document(venueId)
                .collection("learned_metrics")
                .document("current")
                .set(updatedMetrics)
                .await()

            android.util.Log.d(TAG, "bias updated: avg=${"%.1f".format(avgBias)}min, entries=$dataPoints, confidence=${"%.0f".format(confidence * 100)}%")

        } catch (e: Exception) {
            android.util.Log.e(TAG, "add bias entry error", e)
        }
    }

    // get current bias correction for ai estimates
    suspend fun getAiBias(venueId: String): Double {
        return try {
            val metrics = getVenueMetrics(venueId)
            metrics?.aiBiasMinutes ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    // get bias confidence (0.0-1.0) based on data points
    suspend fun getBiasConfidence(venueId: String): Double {
        return try {
            val metrics = getVenueMetrics(venueId)
            metrics?.biasConfidence ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }
}
